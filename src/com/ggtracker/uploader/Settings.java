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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * Settings of the application.
 * 
 * @author Andras Belicza
 */
public class Settings {
	
	/** Path of the settings file to persist settings. */
	public static final File SETTINGS_FILE = new File( Consts.FOLDER_USER_CONTENT, "settings.xml" );
	
	
	/** Replay folder setting.             */
	public static final String KEY_REPLAY_FOLDER             = "replayFolder";
	/** User name setting.                 */
	public static final String KEY_USER_NAME                 = "userName";
	/** Upload key setting.                */
	public static final String KEY_UPLOAD_KEY                = "uploadKey";
	/** Saved with version setting.        */
	public static final String KEY_META_SAVED_WITH_VERSION   = "meta.savedWithVersion";
	/** Save time setting.                 */
	public static final String KEY_META_SAVE_TIME            = "meta.savedTime";
	
	
	/** Default values of settings. */
	private static Properties DEFAULT_PROPERTIES = new Properties();
	static {
		// Specify OS dependant initial replay folder...
		
		File baseReplayFolder = new File( System.getProperty( "user.home" ) );
		switch ( Consts.OS ) {
		case WINDOWS  : baseReplayFolder = new File( baseReplayFolder, Consts.OS.isWindowsXp() ? "My Documents" : "Documents" ); break;
		case MAC_OS_X : baseReplayFolder = new File( baseReplayFolder, "Library/Application Support/Blizzard" ); break;
		default       : baseReplayFolder = new File( baseReplayFolder, "Documents" ); break;
		}
		
		DEFAULT_PROPERTIES.setProperty( KEY_REPLAY_FOLDER, new File( baseReplayFolder, "/StarCraft II/Accounts" ).getAbsolutePath() );
		DEFAULT_PROPERTIES.setProperty( KEY_USER_NAME    , "" );
		DEFAULT_PROPERTIES.setProperty( KEY_UPLOAD_KEY   , "" );
	}
	
	/** Properties storing the settings. */
	private static Properties properties = new Properties( DEFAULT_PROPERTIES );
	
	/**
	 * Sets a setting and automatically saves the modified settings.
	 * 
	 * @param key   key of setting to set
	 * @param value value of setting to set
	 */
	public static void set( final String key, final Object value ) {
		set( key, value, true );
	}
	
	/**
	 * Sets a setting and saves the modified settings if specified.
	 * @param key      key of setting to set
	 * @param value    value of setting to set
	 * @param autoSave tells if the modified settings have to be saved automatically
	 */
	public static void set( final String key, final Object value, final boolean autoSave ) {
		properties.setProperty( key, value.toString() );
		
		if ( autoSave ) {
    		// Save settings immediately
    		saveSettings();
		}
	}
	
	/**
	 * Returns the specified setting as a {@link String}.
	 * @param key key of the setting to return
	 * @return the specified setting as a {@link String}
	 */
	public static String get( final String key ) {
		return properties.getProperty( key );
	}
	
	/**
	 * Returns the specified setting as a {@link Boolean}.
	 * @param key key of the setting to return
	 * @return the specified setting as a {@link Boolean}
	 */
	public static Boolean getBoolean( final String key ) {
		return Boolean.valueOf( properties.getProperty( key ) );
	}
	
	/**
	 * Loads the settings from its persistent file.<br>
	 * If loading fails, errors are silently discarded, the default settings remain.
	 */
	public static void loadSettings() {
		if ( SETTINGS_FILE.exists() )
			try {
				properties.loadFromXML( new FileInputStream( SETTINGS_FILE ) );
			} catch ( final Exception e ) {
				System.err.println( "Failed to load settings!" );
				e.printStackTrace( System.err );
			}
		else
			System.err.println( "Warning: settings file does not exist, the default settings will be used." );
	}
	
	/**
	 * Saves the settings to its persistent file.
	 */
	public static void saveSettings() {
		// First check and create settings folder if not exists
		final File settingsFolder = SETTINGS_FILE.getParentFile();
		if ( !settingsFolder.exists() )
			settingsFolder.mkdirs();
		
		FileOutputStream output = null;
		try {
			
			output = new FileOutputStream( SETTINGS_FILE );
			
			// Set meta data
			set( KEY_META_SAVED_WITH_VERSION, Consts.APP_VERSION        , false );
			set( KEY_META_SAVE_TIME         , System.currentTimeMillis(), false );
			
			properties.storeToXML( output, "This settings file is managed by " + Consts.APP_NAME + " automatically. Do not edit it unless you know what you're doing!" );
			
		} catch ( final Exception e ) {
			System.err.println( "Failed to save settings!" );
			e.printStackTrace( System.err );
		} finally {
			if ( output != null )
				try { output.close(); } catch ( final Exception e ) {}
		}
	}
	
}
