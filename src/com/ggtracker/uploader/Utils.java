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

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.MessageDigest;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Utility collection.
 * 
 * @author Andras Belicza
 */
public class Utils {
	
	/**
	 * Creates a text field which is bound to a string setting.<br>
	 * The initial value of the text field is taken from the settings. If the text in the text field is changed,
	 * the setting which the text field is bound to is updated automatically when focus of the text field is lost.
	 * 
	 * @param settingKey setting key to bound to
	 * @return the text field bound to the specified string setting
	 */
	public static JTextField createSettingTextField( final String settingKey ) {
		final JTextField textField = new JTextField( Settings.get( settingKey ) );
		
		textField.addFocusListener( new FocusAdapter() {
			public void focusLost( final FocusEvent event ) {
				final String text = textField.getText();
				if ( !text.equals( Settings.get( settingKey ) ) )
					Settings.set( settingKey, text );
			}
		} );
		
		return textField;
	}
	
	/**
	 * Creates a link styled label which opens the specified web page when clicked.
	 * @param text      text of the link
	 * @param targetUrl URL to be opened when clicked
	 */
	public static JLabel createLinkLabel( final String text, final String targetUrl ) {
		final JLabel linkLabel = new JLabel( "<html><a href='#'>v" + text.replace( " ", "&nbsp;" ).replace( "<", "&lt;" ).replace( ">", "&gt;" ) + "</a></html>" );
		
		linkLabel.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
		linkLabel.setToolTipText( targetUrl );
		
		linkLabel.addMouseListener( new MouseAdapter() {
			public void mouseClicked( final MouseEvent event ) {
				showURLInBrowser( targetUrl );
			};
		} );
		
		return linkLabel;
	}
	
	/**
	 * Opens the web page specified by the URL in the system's default browser.
	 * @param url URL to be opened
	 */
	public static void showURLInBrowser( final String url ) {
		try {
			if ( Desktop.isDesktopSupported() )
				try {
					Desktop.getDesktop().browse( new URI( url ) );
					return;
				} catch ( final Exception e ) {
				}
			
			// Desktop failed, try our own method
			String[] cmdArray = null;
			if ( Consts.OS == OperatingSystem.WINDOWS ) {
				cmdArray = new String[] { "rundll32", "url.dll,FileProtocolHandler", url };
			}
			else {
				// Linux
				final String[] browsers = { "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
				for ( final String browser : browsers )
					if ( Runtime.getRuntime().exec( new String[] { "which", browser } ).waitFor() == 0 ) {
						cmdArray = new String[] { browser, url };
						break;
					}
			}
			
			if ( cmdArray != null )
				Runtime.getRuntime().exec( cmdArray );
		} catch ( final Exception e ) {
		}
	}
	
	/**
	 * Creates a "form style" layout: same width for components that are in the same column.<br>
	 * The elements of the box must be instances of {@link Box} in order for this to work.
	 * @param box     reference to the box whose content to be aligned
	 * @param columns number of columns to align
	 */
	public static void formLayoutBox( final Box box, final int columns ) {
		Box row;
		for ( int column = 0; column < columns; column++ ) {
			int maxWidth = 0;
			for ( int rowIndex = box.getComponentCount() - 1; rowIndex >= 0; rowIndex-- )
				if ( box.getComponent( rowIndex ) instanceof Box ) {
					row = (Box) box.getComponent( rowIndex );
					if ( row.getComponentCount() > column )
						maxWidth = Math.max( maxWidth, row.getComponent( column ).getPreferredSize().width );
				}
			
			for ( int rowIndex = box.getComponentCount() - 1; rowIndex >= 0; rowIndex-- )
				if ( box.getComponent( rowIndex ) instanceof Box ) {
					row = (Box) box.getComponent( rowIndex );
					if ( row.getComponentCount() > column )
						row.getComponent( column ).setPreferredSize( new Dimension( maxWidth, row.getComponent( column ).getPreferredSize().height ) );
				}
		}
	}
	
	/**
	 * Makes the box left or center aligned.
	 * 
	 * @param box       box to be aligned
	 * @param alignment one of {@link SwingConstants#LEFT} or {@link SwingConstants#CENTER}
	 * 
	 * @throws IllegalArgumentException if alignment is neither {@link SwingConstants#LEFT} nor {@link SwingConstants#CENTER}
	 */
	public static void alignBox( final Box box, final int alignment ) {
		final float alignmentX = alignment == SwingConstants.LEFT ? 0f : alignment == SwingConstants.CENTER ? 0.5f : -1;
		
		if ( alignmentX < 0 )
			throw new IllegalArgumentException();
		
		for ( int i = box.getComponentCount() - 1; i >= 0; i-- )
			( (JComponent) box.getComponent( i ) ).setAlignmentX( alignmentX );
	}
	
	/**
	 * Calculates the MD5 digest of a file.
	 * @param file file whose MD5 digest to be calculated
	 * @return the calculated MD5 digest of the file
	 */
	public static String calculateFileMd5( final File file ) {
		FileInputStream input = null;
		try {
			final MessageDigest md = MessageDigest.getInstance( "MD5" );
			
			input = new FileInputStream( file );
			final byte[] buffer = new byte[ 16*1024 ];
			
			int bytesRead;
			while ( ( bytesRead = input.read( buffer ) ) > 0 )
				md.update( buffer, 0, bytesRead );
			
			return convertToHexString( md.digest() );
		}
		catch ( final Exception e ) {
			return "";
		}
		finally {
			if ( input != null )
				try { input.close(); } catch ( final IOException ie ) {}
		}
	}
	
	/** Digits used in the hexadecimal representation. */
	public static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	
	/**
	 * Converts the specified data to hex string.
	 * @param data data to be converted
	 * @return the specified data converted to hex string
	 */
	public static String convertToHexString( final byte[] data ) {
		final StringBuilder hexBuilder = new StringBuilder( data.length << 1 );
		
		for ( final byte b : data )
			hexBuilder.append( HEX_DIGITS[ ( b & 0xff ) >> 4 ] ).append( HEX_DIGITS[ b & 0x0f ] );
		
		return hexBuilder.toString();
	}
	
	/** Symbols used in the base64 format. */
	private static char[] BASE64_SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
	/** Base64 padding character.          */
	private static char   BASE64_PADDING = '=';
	
	/**
	 * Returns the base64 encoded form of the specified file.
	 * @param file file to be encoded
	 * @return the base64 encoded form of the specified file; or <code>null</code> if some error occurs
	 */
	public static String encodeFileBase64( final File file ) {
		if ( !file.exists() )
			return null;
		
		// 3 bytes results in 4: charCount = RoundUp( size / 3 ) * 4
		int bytesLeft = (int) file.length();
		final char[] encoded = new char[ ( (bytesLeft+2) / 3 ) * 4 ];
		
		InputStream input = null;
		int charPos = 0;
		try {
			input = new FileInputStream( file );
			while ( bytesLeft > 0 ) {
				final int byte1 = input.read();
				final int byte2 = bytesLeft > 1 ? input.read() : 0;
				final int byte3 = bytesLeft > 2 ? input.read() : 0;
				
				encoded[ charPos++ ] = BASE64_SYMBOLS[ byte1 >> 2 ];
				encoded[ charPos++ ] = BASE64_SYMBOLS[ ( byte1 & 0x03 ) << 4 | ( byte2 & 0xf0 ) >> 4 ];
				
				if ( bytesLeft > 1 ) {
					encoded[ charPos++ ] = BASE64_SYMBOLS[ ( byte2 & 0x0f ) << 2 | ( byte3 & 0xc0 ) >> 6 ];
					
					if ( bytesLeft > 2 )
						encoded[ charPos++ ] = BASE64_SYMBOLS[ byte3 &0x3f ];
					else
						// 1 padding byte
						encoded[ charPos++ ] = BASE64_PADDING;
				}
				else {
					// 2 padding bytes
					encoded[ charPos++ ] = BASE64_PADDING;
					encoded[ charPos++ ] = BASE64_PADDING;
				}
				
				bytesLeft -= 3;
			}
		} catch ( final Exception e ) {
			e.printStackTrace();
			return null;
		}
		finally {
			if ( input != null )
				try { input.close(); } catch ( final IOException ie ) {}
		}
		
		return new String( encoded );
	}
	
}
