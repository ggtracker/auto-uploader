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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility class to perform an HTTP POST.
 * 
 * <p>Parameters are sent as if they would be part of an HTML form. The content-type (<code>"Content-Type"</code> request property)
 * of the request will be set to <code>"application/x-www-form-urlencoded;charset"</code>.</p>
 * 
 * @author Andras Belicza
 */
public class HttpPost {
	
	/** Charset to be used. */
	private static final String DEFAULT_CHARSET = "UTF-8";
	
	/** Map of parameters to be sent.           */
	private final Map< String, String > paramsMap;
	/** URL string to post to.                  */
	private final String                urlString;
	/** Optional additional request properties. */
	private Map< String, String >       requestPropertyMap;
	
	/** HttpUrlConnection to perform the POST.  */
	private HttpURLConnection           httpUrlConnection;
	
	/**
	 * Creates a new HttpPost.
	 * @param urlString URL string to post to
	 * @param paramsMap map of parameters to be sent
	 */
	public HttpPost( final String urlString, final Map< String, String > paramsMap ) {
		this.paramsMap = paramsMap;
		this.urlString = urlString;
	}
	
	/**
	 * Sets a request property.
	 * 
	 * <p>The properties will be passed to the underlying {@link HttpURLConnection}
	 * before it's <code>connect()</code> method is called.</p>
	 * 
	 * <p>It must be called before {@link #connect()}.</p>
	 * 
	 * @param key   the property key
	 * @param value the property value
	 */
	public void setRequestProperty( final String key, final String value ) {
		if ( requestPropertyMap == null )
			requestPropertyMap = new HashMap< String, String >();
		
		requestPropertyMap.put( key, value );
	}
	
	/**
	 * Connects to the provided URL.
	 * 
	 * @return true if connection was successful; false otherwise
	 */
	public boolean connect() {
		try {
			httpUrlConnection = (HttpURLConnection) new URL( urlString ).openConnection();
			
			httpUrlConnection.setDoOutput( true );
			
			if ( requestPropertyMap != null )
				for ( final Entry< String, String > entry : requestPropertyMap.entrySet() )
					httpUrlConnection.setRequestProperty( entry.getKey(), entry.getValue() );
			
			httpUrlConnection.setRequestProperty( "Accept-Charset", DEFAULT_CHARSET );
			httpUrlConnection.setRequestProperty( "Content-Type"  , "application/x-www-form-urlencoded;charset=" + DEFAULT_CHARSET );
			
			httpUrlConnection.connect();
		} catch ( final IOException ie ) {
			ie.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Posts the parameters to the server.
	 * 
	 * <p>The parameters will be encoded using UTF-8 charset.</p>
	 * 
	 * <p>Can only be called if {@link #connect()} returned <code>true</code>.</p>
	 * 
	 * @return true if the operation was successful; false otherwise
	 */
	public boolean doPost() {
		OutputStream output = null;
		
		try {
			output = httpUrlConnection.getOutputStream();
			
			final StringBuilder paramsBuilder = new StringBuilder();
			for ( final Entry< String, String > entry : paramsMap.entrySet() ) {
				if ( paramsBuilder.length() > 0 )
					paramsBuilder.append( '&' );
				paramsBuilder.append( entry.getKey() ).append( '=' ).append( URLEncoder.encode( entry.getValue(), DEFAULT_CHARSET ) );
			}
			
			output.write( paramsBuilder.toString().getBytes( DEFAULT_CHARSET ) );
			output.flush();
			
		} catch ( final IOException ie ) {
			ie.printStackTrace();
			return false;
		} finally {
			if ( output != null )
				try { output.close(); } catch ( final IOException ie ) {}
		}
		
		return true;
	}
	
	/**
	 * Returns the HTTP response code of the server.
	 * 
	 * <p>Can only be called if {@link #doPost()} returned <code>true</code>.</p>
	 * 
	 * @return the HTTP response code of the server
	 */
	public int getServerResponseCode() {
		try {
			return httpUrlConnection.getResponseCode();
		} catch ( final IOException ie ) {
			ie.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * Gets the response from the server.
	 * 
	 * <p>Can only be called if {@link #doPost()} returned <code>true</code>.</p>
	 * 
	 * @return the server response, or <code>null</code> if error occurred
	 */
	public String getResponse() {
		InputStream input = null;
		try {
			input = httpUrlConnection.getInputStream();
			final int status = httpUrlConnection.getResponseCode();
			if ( status == HttpURLConnection.HTTP_OK ) {
				
				String responseCharset = DEFAULT_CHARSET;
				final String contentType = httpUrlConnection.getHeaderField( "Content-Type" );
				if ( contentType != null ) {
					for ( final String token : contentType.replace( " ", "" ).split( ";" ) ) {
						if ( token.startsWith( "charset=" ) ) {
							responseCharset = token.split( "=", 2 )[ 1 ];
							break;
						}
					}
				}
				
				final BufferedReader reader = new BufferedReader( new InputStreamReader( input, responseCharset ) );
				final StringBuilder responseBuilder = new StringBuilder();
				final char[] buffer = new char[ 64 ];
				int charsRead;
				while ( ( charsRead = reader.read( buffer ) ) > 0 )
					responseBuilder.append( buffer, 0, charsRead );
				
				return responseBuilder.toString();
			}
		} catch ( final IOException ie ) {
			ie.printStackTrace();
		} finally {
			if ( input != null )
				try { input.close(); } catch ( final IOException ie ) {}
		}
		
		return null;
	}
	
	/**
	 * Closes this HttpPost, releases all allocated resources.
	 */
	public void close() {
		if ( httpUrlConnection != null )
			httpUrlConnection.disconnect();
	}
	
}
