package com.github.tbporter.cypher_sydekick.database;

/**
 * Custom exception thrown by {@link DatabaseManager} methods. This class
 * provides no new functionality, it simply provides a new exception name to
 * identify exceptions thrown by {@link DatabaseManager}.
 * 
 * @author ayelix
 * 
 */
public final class DatabaseManagerException extends Exception {
	public DatabaseManagerException(String message) {
		super(message);
	}
}
