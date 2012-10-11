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

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * An info main class registered as the main class of the jar to display proper usage.
 * 
 * @author Andras Belicza
 */
public class InfoMainClass {
	
	/**
	 * Entry point of the program.
	 * 
	 * <p>Displays an info message about how to start the application.</p>
	 * 
	 * @param arguments used to take arguments from the running environment - not used here
	 */
	public static void main( final String[] arguments ) {
		// Set Look and Feel.
		try {
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		} catch ( final Exception e ) {
		}
		
		final Box starterSriptsInfoBox = Box.createVerticalBox();
		starterSriptsInfoBox.add( createStarterScriptsInfoBoxRow( "Windows:" , "ggtracker-uploader.exe"          ) );
		starterSriptsInfoBox.add( createStarterScriptsInfoBoxRow( "MAC OS-X:", "ggtracker-uploader-os-x.command" ) );
		starterSriptsInfoBox.add( createStarterScriptsInfoBoxRow( "Linux:"   , "ggtracker-uploader-linux.sh"     ) );
		
		JOptionPane.showMessageDialog( null,
				new Object[] {
					"Please run the proper script to start ggtracker uploader!",
					" ",
					starterSriptsInfoBox
				},
				"ggtracker uploader", JOptionPane.WARNING_MESSAGE );
	}
	
	/**
	 * Creates a row for the starter scripts info box.
	 * @param osName         OS name
	 * @param executableName executable name
	 * @return a row for the starter scripts info box
	 */
	private static Box createStarterScriptsInfoBoxRow( final String osName, final String executableName ) {
		final Box row = Box.createHorizontalBox();
		
		final JLabel osNameLabel = new JLabel( osName );
		osNameLabel.setPreferredSize( new Dimension( 80, 0 ) );
		row.add( osNameLabel );
		row.add( new JLabel( executableName ) );
		row.add( Box.createHorizontalGlue() );
		
		return row;
	}
	
}
