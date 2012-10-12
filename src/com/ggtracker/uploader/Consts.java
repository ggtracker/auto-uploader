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

import javax.swing.ImageIcon;

/**
 * Application wide constants.
 * 
 * @author Andras Belicza
 */
public class Consts {
	
	/** Application name.         */
	public static final String APP_NAME         = "ggtracker uploader";
	/** Application version.      */
	public static final String APP_VERSION      = "1.0";
	/** Application release date. */
	public static final String APP_RELEASE_DATE = "2012-10-10";
	
	/** Running operating system. */
	public static final OperatingSystem OS = OperatingSystem.detect();
	
	/** Tells if we're in development mode.
	 * In developer mode logs are not redirected to the log file but printed on the console. */
	public static final boolean DEV_MODE = System.getProperty( "dev-mode" ) != null;
	
	/** Application icon. Used as the main frame icon image and as the tray icon image. */
	public static final ImageIcon APP_ICON = new ImageIcon( Consts.class.getResource("resources/gg.png") );
	
	/** Home page URL.                                                        */
	public static final String URL_HOME_PAGE          = "http://ggtracker.com/uploader";
	
	/** Root folder to store all user content (like settings, logs).          */
	public static final File   FOLDER_USER_CONTENT    = new File( System.getProperty( "user.home" ), "ggtracker uploader" );
	/** Logs folder.                                                          */
	public static final File   FOLDER_LOGS            = new File( FOLDER_USER_CONTENT, "Logs" );
	
	/** Extension of the log files.                                           */
	public static final String LOG_FILE_EXT           = ".log";
	
	/** Days to keep log files for (older files get auto-deleted on startup). */
	public static final int    DAYS_TO_KEEP_LOG_FILES = 14;
	
}
