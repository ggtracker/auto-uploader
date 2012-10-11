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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The Log Manager controls the console redirection to a log file and manages old log files.
 * 
 * @author Andras Belicza
 */
public class Log {
	
	/** Name of the log file name. */
	private static final String LOG_FILE_NAME = "system_messages";
	/** The log file.              */
	private static final File   LOG_FILE      = new File( Consts.FOLDER_LOGS, LOG_FILE_NAME + Consts.LOG_FILE_EXT );
	
	/**
	 * No need to instantiate this class.
	 */
	private Log() {
	}
	
	/**
	 * Initializes the log manager.
	 */
	public static void init() {
		// First check and create the log folder if not exists
		if ( !Consts.FOLDER_LOGS.exists() )
			Consts.FOLDER_LOGS.mkdirs();
		
		try {
			// Backup (rename) last log file
			if ( LOG_FILE.exists() && LOG_FILE.length() > 0 )
				LOG_FILE.renameTo( new File( Consts.FOLDER_LOGS, LOG_FILE_NAME + new SimpleDateFormat( " yyyy-MM-dd HH-mm-ss" ).format( new Date( LOG_FILE.lastModified() ) ) + Consts.LOG_FILE_EXT ) );
			
			// Delete old log files
			final File[] logFiles = Consts.FOLDER_LOGS.listFiles();
			if ( logFiles != null ) {
				final long oldestAllowed = System.currentTimeMillis() - Consts.DAYS_TO_KEEP_LOG_FILES * 24l * 60 * 60 * 1000;
				for ( final File logFile : logFiles )
					if ( logFile.lastModified() < oldestAllowed )
						logFile.delete();
			}
			
			// Create new log file
			if ( !Consts.DEV_MODE ) {
				final PrintStream logStream = new PrintStream( new FileOutputStream( LOG_FILE ), true );
				System.setOut( logStream );
				System.setErr( logStream );
			}
		} catch ( final FileNotFoundException fnfe ) {
			fnfe.printStackTrace();
		}
	}
	
}
