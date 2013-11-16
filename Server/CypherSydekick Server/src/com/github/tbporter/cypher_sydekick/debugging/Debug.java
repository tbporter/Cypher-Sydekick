package com.github.tbporter.cypher_sydekick.debugging;

/**
 * Provides convenience methods for debug output.
 * 
 * @author ayelix
 * 
 */
public final class Debug {
	/**
	 * Prints an error message to stderr of the format "{tag} : {msg}"
	 * 
	 * @param tag
	 *            Tag for this message, generally a class name.
	 * @param msg
	 *            Error message to print.
	 */
	public static void printError(final String tag, final String msg) {
		System.err.println(tag + " : " + msg);
	}
}
