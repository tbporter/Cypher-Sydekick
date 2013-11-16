package com.github.tbporter.cypher_sydekick;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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

	/** Name of the user table. */
	private static String USER_TABLE_NAME = "users";

	/** Connection to the database, created in {@link #openDatabase()}. */
	private static Connection s_connection;

	/**
	 * Attempts to connect to the database.
	 * 
	 * @throws Exception
	 *             if the connection fails.
	 */
	public static void openDatabase() throws Exception {
		try {
			Class.forName(org.sqlite.JDBC.class.getName());
			s_connection = DriverManager.getConnection(DB_NAME);

		} catch (ClassNotFoundException cnfe) {
			throw new Exception(
					"Cannot open database - unable to load JDBC driver class.");
		} catch (SQLException sqle) {
			throw new Exception("SQL exception while opening database.");
		}
	}

	/**
	 * Creates the table containing users. Database must be open (
	 * {@link #openDatabase()}).
	 * 
	 * @throws Exception
	 *             if table creation fails or database has not been opened.
	 */
	public static void createUserTable() throws Exception {
		if (s_connection != null) {
			try {
				Statement s = s_connection.createStatement();
				String sql = "CREATE TABLE IF NOT EXISTS " + USER_TABLE_NAME
						+ " (id INT PRIMARY KEY NOT NULL,"
						+ " username TEXT NOT NULL)";
				s.executeUpdate(sql);
				s.close();

			} catch (SQLException sqle) {
				throw new Exception("SQL exception during table creation.");
			}

		} else {
			throw new Exception("Database not open.");
		}
	}
}
