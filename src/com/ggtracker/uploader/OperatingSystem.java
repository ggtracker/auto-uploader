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

/**
 * Operating systems.
 * 
 * @author Andras Belicza
 */
public enum OperatingSystem {
	
	/** Windows.  */
	WINDOWS,
	/** Unix.     */
	UNIX,
	/** MAC OS-X. */
	MAC_OS_X,
	/** Other.    */
	OTHER;
	
	
	/**
	 * Detects and returns the running operating system.
	 * @return the running operating system
	 */
	public static OperatingSystem detect() {
		final String osName = System.getProperty( "os.name" ).toLowerCase();
		
		if ( osName.indexOf( "win" ) >= 0 )
			return OperatingSystem.WINDOWS;
		else if ( osName.indexOf( "mac" ) >= 0 )
			return OperatingSystem.MAC_OS_X;
		else if ( osName.indexOf( "nix" ) >= 0 || osName.indexOf( "nux" ) >= 0 )
			return OperatingSystem.UNIX;
		
		return OperatingSystem.OTHER;
	}
	
	/**
	 * Tells if the running operating system is Windows XP.
	 * @return true if the running operating system is Windows XP; false otherwise
	 */
	public boolean isWindowsXp() {
		return "Windows XP".equals( System.getProperty( "os.name" ) );
	}
	
}
