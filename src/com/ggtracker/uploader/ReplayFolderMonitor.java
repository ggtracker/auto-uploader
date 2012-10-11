/*
 * ggtracker uploader project
 * 
 * Copyright (c) 2012 ggtracker.com
 * 
 * This software is the property of ggtracker, inc.
 * Copying, modifying, distributing, refactoring without the authors permission
 * is prohibited and protected by Law.
 */
package com.ggtracker.uploader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.jna.platform.FileMonitor;
import com.sun.jna.platform.FileMonitor.FileEvent;
import com.sun.jna.platform.FileMonitor.FileListener;

/**
 * Replay folder monitor.
 * 
 * <p>Starts a new thread and monitors the replay folder for new replays.
 * When new replays are detected, they are uploaded to ggtracker.com.</p> 
 * 
 * @author Andras Belicza
 */
public class ReplayFolderMonitor extends Thread implements FileFilter {
	
	/**
	 * Tells if a request has been made to cancel the execution of the thread.
	 * The {@link #run()} method is responsible to periodically check this variable
	 * whether we are allowed to continue monitoring or we have to return in order to end the thread.
	 */
	private volatile boolean requestedToCancel;
	
	/** Tells if File monitor is supported on the current machine. */
	private static final boolean fileMonitorSupported = Consts.OS == OperatingSystem.WINDOWS;
	
	/** Date and time of the previous archived replay. */
	private volatile long previousReplayDate = System.currentTimeMillis();
	
	/** New replay file reported by the file monitor. */
	private volatile File lastReplayFromMonitor;
	
    /**
     * Creates a new ReplayFolderMonitor.
     */
    public ReplayFolderMonitor() {
		super( "Replay Folder Monitor" );
		
		// Set normal thread priority in case we're started from the EDT (Event Dispatching Thread)...
		setPriority( NORM_PRIORITY );
    }
	
	/**
	 * Requests the cancellation of the execution of the thread.
	 */
	public void requestToCancel() {
		shutdownFileMonitor();
		
		// Volatile variables are synchronized internally, so no need external synchronization here.
		requestedToCancel = true;
	}
	
	/**
	 * Monitoring functionality in a new thread.
	 */
	@Override
	public void run() {
		if ( fileMonitorSupported )
			setupFileMonitor();
		
		final long sleepTime = fileMonitor == null ? 3000 : 1000;
		
		final File replayFolder = new File( Settings.get( Settings.KEY_REPLAY_FOLDER ) ).getAbsoluteFile();
		
		while ( !requestedToCancel )
			try {
    			File lastReplayFile = null;
    			
    			// Check if a new replay was saved
    			if ( fileMonitor == null ) {
    				// Polling
    				lastReplayFile = getLastReplay( replayFolder );
    			}
    			else {
    				if ( lastReplayFromMonitor != null ) {
    					lastReplayFile        = lastReplayFromMonitor;
    					lastReplayFromMonitor = null;
    				}
    			}
    			
    			if ( lastReplayFile != null ) {					
    				// Wait a little, let SC2 finish saving the game...
    				sleep( 1500l );
    				// Check again the date: FileListener is called at creation time which is the current time, lastModified of file is only set after that
    				if ( lastReplayFile.lastModified() > previousReplayDate )
    					uploadNewReplay( lastReplayFile );
    			}
    			
	            sleep( sleepTime );
            } catch ( final Exception e ) {
	            e.printStackTrace();
	            // Do not stop monitoring replay folder
            }
	}
	
	/**
	 * Returns the last replay (one replay) that is newer than the one we last uploaded.
	 * This is part of the polling method.
	 * @param startFolder start folder to start the search in
	 * @return the last replay (one replay) that is after the last check time
	 */
	private File getLastReplay( final File startFolder ) {
		final File[] files = startFolder.listFiles( this );
		if ( files == null )
			return null;
		
		for ( int i = files.length - 1; i >= 0; i-- ) { // Prolly the last is the last, so in order to minimize assignments, go downwards...
			final File file = files[ i ];
			if ( file.isFile() )
				return file;
			else {
				final File lastReplay = getLastReplay( file );
				if ( lastReplay != null )
					return lastReplay;
			}
		}
		
		return null;
	}
	
	// ========================== REPLAY UPLOAD SPECIFICATION CONSTANTS ===========================
	/** Value of the request version parameter. */
	private static final String PARAM_VALUE_REQUEST_VERSION = "1.0";
	
	/** Name of the request version parameter. */
	private static final String PARAM_NAME_REQUEST_VERSION = "requestVersion";
	/** Name of the user name parameter.       */
	private static final String PARAM_NAME_USER_NAME       = "userName";
	/** Name of the password parameter.        */
	private static final String PARAM_NAME_PASSWORD        = "password";
	/** Name of the description parameter.     */
	private static final String PARAM_NAME_DESCRIPTION     = "description";
	/** Name of the file name parameter.       */
	private static final String PARAM_NAME_FILE_NAME       = "fileName";
	/** Name of the file size parameter.       */
	private static final String PARAM_NAME_FILE_SIZE       = "fileSize";
	/** Name of the file MD5 parameter.        */
	private static final String PARAM_NAME_FILE_MD5        = "fileMd5";
	/** Name of the file content parameter.    */
	private static final String PARAM_NAME_FILE_CONTENT    = "fileContent";
	
	// ======================= END OF REPLAY UPLOAD SPECIFICATION CONSTANTS =======================
	
	/**
	 * Uploads the new replay.
	 * @param lastReplayFile a new replay to be uploaded
	 */
	private void uploadNewReplay( final File lastReplayFile ) {
		// Store the last modification date now
		// (as it changes at the end of saving, and it will differ in our case if saving finishes during our last sleep,
		//  also in case of file monitor it might be the creation (current) time, lastModified is only set later...)
		previousReplayDate = lastReplayFile.lastModified();
		
		System.out.println( "New replay detected, uploading: " + lastReplayFile.getAbsolutePath() );
		
		// Retry a couple of times if upload fails:
		for ( int attempt = 0; attempt < 3; attempt++ ) {
			if ( attempt > 0 )
				System.out.println( "Retrying upload (" + ( attempt + 1 ) + ")..." );
			
    		HttpPost httpPost = null;
    		try {
    			final String fileMd5 = Utils.calculateFileMd5( lastReplayFile );
    			if ( fileMd5 == null || fileMd5.length() == 0 ) {
    				System.err.println( "MD5 could not be calculated (" + lastReplayFile + ")!" );
    				continue;
    			}
    			final String fileBase64 = Utils.encodeFileBase64( lastReplayFile );
    			if ( fileBase64 == null ) {
    				System.err.println( "Base64 encoding could not be performed (" + lastReplayFile + ")!" );
    				continue;
    			}
    			
    			final Map< String, String > paramsMap = new HashMap< String, String >();
    			paramsMap.put( PARAM_NAME_REQUEST_VERSION, PARAM_VALUE_REQUEST_VERSION );
    			paramsMap.put( PARAM_NAME_FILE_NAME      , lastReplayFile.getName() );
    			paramsMap.put( PARAM_NAME_FILE_SIZE      , Long.toString( lastReplayFile.length() ) );
    			paramsMap.put( PARAM_NAME_DESCRIPTION    , "" ); // Not used
    			paramsMap.put( PARAM_NAME_USER_NAME      , Settings.get( Settings.KEY_USER_NAME ) );
    			paramsMap.put( PARAM_NAME_PASSWORD       , Settings.get( Settings.KEY_UPLOAD_KEY ) );
    			paramsMap.put( PARAM_NAME_FILE_MD5       , fileMd5 );
    			paramsMap.put( PARAM_NAME_FILE_CONTENT   , fileBase64 );
    			
    			httpPost = new HttpPost( "http://ggtracker.com/api/upload", paramsMap );
    			
    			if ( !httpPost.connect() ) {
    				System.out.println( "Failed to connect!" );
    				continue;
    			}
    			if ( !httpPost.doPost() ) {
    				System.out.println( "Failed to send replay!" );
    				continue;
    			}
    			final String response = httpPost.getResponse();
    			if ( response == null ) {
    				System.out.println( "Failed to read server response!" );
    				continue;
    			}
    			
    			try {
    				// Example response:
    				/*
    				 * <?xml version="1.0" encoding="UTF-8"?>
    				 * <uploadResult docVersion="1.0">
    				 *     <errorCode>0</errorCode>
    				 *     <message>Upload OK.</message>
    				 *     <replayUrl>http://some.host.com/replay?id=1234</replayUrl>
    				 * </uploadResult>
    				 */
    				final Document responseDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( new ByteArrayInputStream( response.getBytes( "UTF-8" ) ) );
    				final Element  docElement       = responseDocument.getDocumentElement();
    				final int      errorCode        = Integer.parseInt( ( (Element) docElement.getElementsByTagName( "errorCode" ).item( 0 ) ).getTextContent().trim() );
    				if ( errorCode == 0 ) {
    					final String replayUrl = ( (Element) docElement.getElementsByTagName( "replayUrl" ).item( 0 ) ).getTextContent().trim();
        				System.out.println( "Successful upload, replay URL: " + replayUrl );
    					
    					// All went well, no more retries:
        				if ( GgtrackerUploader.mainFrame != null )
        					GgtrackerUploader.mainFrame.incUploadCount();
        				
    					return;
    				}
    				else {
    					final String message = ( (Element) docElement.getElementsByTagName( "message" ).item( 0 ) ).getTextContent().trim();
        				System.out.println( "Upload reported to have failed, error code:" + errorCode + ", error message: " + message );
        				continue;
    				}
    			} catch ( final Exception e ) {
    				System.out.println( "Failed to parse server response!" );
    				e.printStackTrace();
    				continue;
    			}
    		} finally {
    			if ( httpPost != null )
    				httpPost.close();
    		}
    		
		}
		
		if ( GgtrackerUploader.mainFrame != null )
			GgtrackerUploader.mainFrame.incFailedCount();
	}
	
	/**
	 * Shuts down this thread.<br>
	 * First calls {@link #requestToCancel()} and then waits for this thread to close by calling {@link #join()}.
	 */
	public void shutdown() {
		requestToCancel();
		
		try {
			join();
		} catch ( final InterruptedException ie ) {
			ie.printStackTrace();
		}
	}
	
	/**
	 * An IO file filter that accepts all directories and SC2Replay files that are newer than the one we last archived.
	 * @param pathname the abstract pathname to be tested
	 * @return true if the pathname denotes a directory or an SC2Replay file that is newer than the one we last archived
	 */
	@Override
	public boolean accept( final File pathname ) {
		return pathname.isDirectory() || 
			pathname.lastModified() > previousReplayDate && pathname.getName().toLowerCase().endsWith( ".sc2replay" );
	}
	
	// ================ FILE MONITOR IMPLEMENTATION =========================================================
	
	/** Reference to the file monitor.  */
	private FileMonitor  fileMonitor;
	/** Watched folder.                 */
	private File         watchedFolder;
	/** Reference to the file listener. */
	private FileListener fileListener;
	
	/**
	 * Initializes the file monitor.
	 * @return true if the file monitor is initialized properly; false if some error occurred
	 */
	private boolean setupFileMonitor() {
		synchronized ( ReplayFolderMonitor.class ) {
			if ( fileMonitor == null ) {
				fileMonitor     = FileMonitor.getInstance();
				watchedFolder   = null;
			}
			
			try {
				// Add watches
				final File replayFolder = new File( Settings.get( Settings.KEY_REPLAY_FOLDER ) ).getAbsoluteFile();
				if ( !replayFolder.exists() )
					throw new IOException( "Replay folder does not exist: " + replayFolder );
				if ( !replayFolder.isDirectory() )
					throw new IOException( "Replay folder is not a folder but a file: " + replayFolder );
				
				// Note: FILE_DELETED mask has to be specified too in order to receive FILE_CREATED events. Bug?
				fileMonitor.addWatch( replayFolder, FileMonitor.FILE_CREATED | FileMonitor.FILE_DELETED, true );
				watchedFolder = replayFolder;
				
				fileMonitor.addFileListener( fileListener = new FileListener() {
					@Override
					public void fileChanged( final FileEvent event ) {
						if ( event.getType() == FileMonitor.FILE_CREATED ) {
							final File file = event.getFile();
							if ( file.isFile() && accept( file ) )
								lastReplayFromMonitor = file;
						}
					}
				} );
				
				return true;
			} catch ( final IOException ie ) {
				System.out.println( "Failed to setup File monitor, reverting to polling..." );
				ie.printStackTrace();
				
				shutdownFileMonitor();
				return false;
			}
		}
	}
	
	/**
	 * Shuts down the file monitor.
	 */
	private void shutdownFileMonitor() {
		synchronized ( ReplayFolderMonitor.class ) {
			if ( fileMonitor != null ) {
				if ( fileListener != null )
					fileMonitor.removeFileListener( fileListener );
				fileListener = null;
				
				fileMonitor.removeWatch( watchedFolder );
				
				fileMonitor.dispose();
				
				fileMonitor   = null;
				watchedFolder = null;
			}
		}
	}
	
	// ================ END OF FILE MONITOR IMPLEMENTATION ==================================================
	
}
