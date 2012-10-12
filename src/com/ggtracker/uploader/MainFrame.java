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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Main frame of ggtracker uploader.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings( "serial" )
public class MainFrame extends JFrame {
	
	/** Color to display errors. */
	private static final Color COLOR_ERROR = new Color( 215, 150, 150 ); // Red-ish color
	
	/** Successful upload counter. */
	private final AtomicInteger uploadCount = new AtomicInteger();
	/** Failed upload counter.     */
	private final AtomicInteger failedCount = new AtomicInteger();
	
	/** Text field to display the monitored replay folder. */
	private final JTextField replayFolderTextField = new JTextField( Settings.get( Settings.KEY_REPLAY_FOLDER ) );
	
	/** Label to display status.                   */
	private final JLabel statusLabel        = new JLabel();
	/** Label to display successful uploads count. */
	private final JLabel uploadedCountLabel = new JLabel( "0" );
	/** Label to display failed uploads count.     */
	private final JLabel failedCountLabel   = new JLabel( "0");
	
	/**
	 * Creates a new MainFrame.
	 */
	public MainFrame() {
		super( Consts.APP_NAME );
		
		setResizable( false );
		setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
		addWindowListener( new WindowAdapter() {
			@Override
			public void windowClosing( final WindowEvent event ) {
				if ( GgtrackerUploader.trayIcon != null ) {
					GgtrackerUploader.hide();
					dispose();
				} else
					if ( JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog( MainFrame.this, new String[] { "Warning! System tray icon could not be installed.", "Are you sure you want to Quit and Stop Uploading Replays?" }, "Warning!", JOptionPane.YES_NO_OPTION ) )
						GgtrackerUploader.exit();
			}
		} );
		setIconImage( Consts.APP_ICON.getImage() );
		
		buildGUI();
		
		pack();
		
		setLocationRelativeTo( null );
		
		setVisible( true );
	}
	
	/**
	 * Builds the GUI of the main frame.
	 */
	private void buildGUI() {
		( (JPanel) getContentPane() ).setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		
		Box row;
		final int SPACING = 3;
		
		final Box contentBox = Box.createVerticalBox();
		
		// SETTINGS
		
		row = Box.createHorizontalBox();
		row.add( new JLabel( "Replay folder:" ) );
		replayFolderTextField.setEditable( false );
		replayFolderTextField.setColumns( 30 );
		checkReplayFolder();
		row.add( replayFolderTextField );
		final JButton chooseButton = new JButton( "Choose..." );
		chooseButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				// Show folder chooser dialog
				final JFileChooser fileChooser = new JFileChooser( replayFolderTextField.getText() );
				fileChooser.setDialogTitle( "Choose your replay folder" );
				fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
				if ( fileChooser.showOpenDialog( MainFrame.this ) == JFileChooser.APPROVE_OPTION ) {
					// Proceed even if the same folder is selected, he might have created it if it didn't exist for example.
					final String selectedFilePath = fileChooser.getSelectedFile().getAbsolutePath();
					System.out.println( "Replay folder changed to: " + selectedFilePath );
					replayFolderTextField.setText( selectedFilePath );
					Settings.set( Settings.KEY_REPLAY_FOLDER, selectedFilePath );
					checkReplayFolder();
					GgtrackerUploader.initReplayFolderMonitor();
				}
			}
		} );
		row.add( chooseButton );
		contentBox.add( row );
		contentBox.add( Box.createVerticalStrut( SPACING ) );
		row = Box.createHorizontalBox();
		row.add( new JLabel( "User name:" ) );
		row.add( Utils.createSettingTextField( Settings.KEY_USER_NAME ) );
		contentBox.add( row );
		contentBox.add( Box.createVerticalStrut( SPACING ) );
		row = Box.createHorizontalBox();
		row.add( new JLabel( "Upload key:" ) );
		row.add( Utils.createSettingTextField( Settings.KEY_UPLOAD_KEY ) );
		contentBox.add( row );
		Utils.formLayoutBox( contentBox, 1 );
		
		// STATS AND STATUS
		
		contentBox.add( Box.createVerticalStrut( SPACING ) );
		JPanel wrapper = new JPanel( new FlowLayout( FlowLayout.LEFT, 0, SPACING ) );
		wrapper.add( new JLabel( "Status: " ) );
		statusLabel.setOpaque( true );
		wrapper.add( statusLabel );
		wrapper.add( Box.createHorizontalStrut( 15 ) );
		wrapper.add( new JLabel( "Successful uploads: " ) );
		wrapper.add( uploadedCountLabel );
		wrapper.add( Box.createHorizontalStrut( 15 ) );
		wrapper.add( new JLabel( "Failed uploads: " ) );
		wrapper.add( failedCountLabel );
		contentBox.add( wrapper );
		
		// ACTIONS
		
		contentBox.add( Box.createVerticalStrut( SPACING ) );
		wrapper = new JPanel( new FlowLayout( FlowLayout.RIGHT, 0, 0 ) );
		final JButton exitButton = new JButton( "Quit and Stop Uploading Replays" );
		exitButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				GgtrackerUploader.exit();
			}
		} );
		wrapper.add( exitButton );
		wrapper.add( Box.createHorizontalStrut( 3 ) );
		wrapper.add( Utils.createLinkLabel( Consts.APP_VERSION, Consts.URL_HOME_PAGE ) );
		contentBox.add( wrapper );
		
		Utils.alignBox( contentBox, SwingConstants.CENTER );
		getContentPane().add(contentBox, BorderLayout.CENTER );
	}
	
	/**
	 * Checks whether the replay folder specified by the text value of the specified text field exists.<br>
	 * If the folder does not exists, red background will be set to indicate the error, and proper error tool tip text will be set.
	 */
	private void checkReplayFolder() {
		final File replaysFolder = new File( Settings.get( Settings.KEY_REPLAY_FOLDER ) );
		
		if ( !replaysFolder.exists() || !replaysFolder.isDirectory() ) {
			replayFolderTextField.setBackground( COLOR_ERROR );
			replayFolderTextField.setToolTipText( "ERROR: "
				+ ( replaysFolder.exists() ? "This is not a folder but a file!" : "This folder does not exist!" ) );
			
			statusLabel.setBackground( COLOR_ERROR );
			statusLabel.setText( "Error!" );
			statusLabel.setToolTipText( "ERROR: "
				+ ( replaysFolder.exists() ? "Replay folder is not a folder but a file!" : "Replay folder does not exist!" ) );
		}
		else {
			replayFolderTextField.setBackground( null );
			replayFolderTextField.setToolTipText( replaysFolder.getAbsolutePath() );
			
			statusLabel.setBackground( null );
			statusLabel.setText( "OK" );
			statusLabel.setToolTipText( null );
		}
	}
	
	/**
	 * Increments the successful uploads count.
	 */
	public void incUploadCount() {
		uploadCount.incrementAndGet();
		uploadedCountLabel.setText( Integer.toString( uploadCount.get() ) );
	}
	
	/**
	 * Increments the failed uploads count.
	 */
	public void incFailedCount() {
		failedCountLabel.setText( Integer.toString( failedCount.get() ) );
	}
	
	/**
	 * Restores the main frame even if it is minimized to tray.
	 */
	public void restore() {
		// Make main frame visible:
		setVisible( true );
		
		// Now set NORMAL extended state in case window is iconified but not closed
		setExtendedState( Frame.NORMAL );

	}
	
}
