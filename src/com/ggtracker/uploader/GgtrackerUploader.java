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

import java.awt.AWTException;

import java.net.URLClassLoader;

import java.awt.Frame;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import javax.swing.UIManager;

import java.net.URLClassLoader;
/**
 * This is the main class of ggtracker uploader.
 * 
 * <p>Application starts in development mode if the <code>"dev-mode"</code> environment variable is present (see {@link Consts#DEV_MODE} for details).
 * You can achieve this by passing the <code>-Ddev-mode</code> VM argument to the <code>java.exe</code> or <code>javaw.exe</code> process.</p>
 * 
 * @author Andras Belicza
 */
public class GgtrackerUploader {
	
	// Public (application wide) Object repository: 
	
	/** Reference to the replay folder monitor. */
	public static ReplayFolderMonitor replayFolderMonitor;
	
	/** Reference to the tray icon. */
	public static TrayIcon            trayIcon;
	
	/** Reference to the main frame. */
	public static MainFrame           mainFrame;
	
	/** Reference to the popup menu. */
	public static PopupMenu			  popupMenu;
	
	// End of Public Object repository
	
	
	/**
	 * This is the entry point of ggtracker uploader.
	 * 
	 * @param arguments arguments passed on to us
	 */
	public static void main( final String[] arguments ) {
		// Set Look and Feel.
		try {
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		} catch ( final Exception e ) {
			e.printStackTrace();
		}
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        
        URL[] urls = ((URLClassLoader)cl).getURLs();
 
        for(URL url: urls){
        	System.out.println(url.getFile());
        }
		System.out.println(GgtrackerUploader.class.getClassLoader().getClass().getName());
		// Check running instances and active it if there's one
		 InstanceMonitor.checkRunningInstance( arguments );
		
		// Init logging
		Log.init();
		
		// Load settings:
		Settings.loadSettings();
		
		// Initialize replay folder monitor
		initReplayFolderMonitor();
		
		// Install tray icon
		setupTrayIcon();
		
		// Create and display main frame
		mainFrame = new MainFrame();
	}
	
	/**
	 * Initializes the replay folder monitor.<br>
	 * If a monitor is already running, it will be stopped.
	 */
	public static synchronized void initReplayFolderMonitor() {
		if ( replayFolderMonitor != null )
			replayFolderMonitor.requestToCancel();
		
		replayFolderMonitor = new ReplayFolderMonitor();
		replayFolderMonitor.start();
	}
	
	/**
	 * Sets up the tray icon.
	 */
	private static void setupTrayIcon() {
		if ( !SystemTray.isSupported() ) {
			System.out.println( "System tray is not supported!" );
			return;
		}
		
		
		try {
			Enumeration<URL> urls = Consts.class.getClassLoader().getResources("");
			while(urls.hasMoreElements()) {
				System.out.println(urls.nextElement());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		trayIcon = new TrayIcon( Consts.APP_ICON.getImage(), Consts.APP_NAME + " is running." );
		trayIcon.setImageAutoSize( true );
		
		try {
	        SystemTray.getSystemTray().add( trayIcon );
        } catch ( final AWTException ae ) {
			System.out.println( "Failed to install system tray icon!" );
	        ae.printStackTrace();
	        trayIcon = null;
        }
		
		popupMenu = new PopupMenu();
		final MenuItem quitMenuItem = new MenuItem("Quit and Stop Uploading");
		quitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed( final ActionEvent event) {
				exit();
			}
		});
		
		final MenuItem showHideMenuItem = new MenuItem("Minimize to Tray");
		showHideMenuItem.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				if (!mainFrame.isVisible()) {
					show();
				} else {
					hide();
				}
			}
		} );
		
		
		popupMenu.add( showHideMenuItem );
		popupMenu.add( quitMenuItem );
		trayIcon.setPopupMenu( popupMenu );
		
		// Windows properly passes left click to the tray icon, MAC OS-X does not!
		// So this listener probably will only work on windows for now. Need OS-X Solution!
		trayIcon.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked( final MouseEvent event ) {
				if (event.getButton() == MouseEvent.BUTTON1) {
    				if (!mainFrame.isVisible()) {
    					show();
    				} else {
    					hide();
    				}
				}
			}
		});
	}
	
	public static void hide() {
		mainFrame.setVisible(false);
		popupMenu.getItem(0).setLabel("Restore Window");
	}
	
	public static void show() {
		// http://stackoverflow.com/a/387365
		java.awt.EventQueue.invokeLater(new Runnable() {
		    @Override
		    public void run() {
				mainFrame.restore();
		    	mainFrame.toFront();
		    	mainFrame.repaint();
		    }
		});
		popupMenu.getItem(0).setLabel("Minimize to Tray");
	}
	
	/**
	 * Exits the application.
	 */
	public static void exit() {
		// Wait for the folder monitor to shut down properly so if an upload is in progress, we will not interrupt it.
		replayFolderMonitor.shutdown();
		
		System.exit( 0 );
	}
	
}
