package com.github.tbporter.cypher_sydekick;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Handles all database-related operations (opening database, managing tables,
 * etc.).
 * 
 * @author ayelix
 * 
 */
public final class DatabaseManager {
	private static final String TAG = "DatabaseManager";

	/** Location of the main database. */
	private static String DB_NAME = "jdbc:sqlite:war/WEB-INF/userlist.db";

	/**
	 * Attempts to connect to the database.
	 * 
	 * @throws Exception
	 *             if the connection fails.
	 */
	public static void openDatabase() throws Exception {
		try {
			Class.forName(org.sqlite.JDBC.class.getName());
			Connection c = DriverManager.getConnection(DB_NAME);

		} catch (ClassNotFoundException cnfe) {
			throw new Exception(
					"Cannot open database - unable to load JDBC driver class.");
		} catch (SQLException sqle) {
			throw new Exception("SQL exception while opening database.");
		}
	}
}
