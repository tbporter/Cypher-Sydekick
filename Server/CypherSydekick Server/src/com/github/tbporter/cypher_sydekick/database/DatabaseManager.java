package com.github.tbporter.cypher_sydekick.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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
	private static String USERNAME_COLUMN_LABEL = "username";

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
	 * Checks if the database has been opened (via {@link #openDatabase()}).
	 * 
	 * @return <tt>true</tt> if the database is open, <tt>false</tt> if not.
	 */
	public static boolean isDatabaseOpen() {
		return (s_connection != null);
	}

	/**
	 * Creates the table containing users. Database must be open (
	 * {@link #openDatabase()}).
	 * 
	 * @throws Exception
	 *             if table creation fails or database has not been opened.
	 */
	public static void createUserTable() throws Exception {
		if (isDatabaseOpen()) {
			try {
				Statement s = s_connection.createStatement();
				final String sql = "CREATE TABLE IF NOT EXISTS "
						+ USER_TABLE_NAME + " (" + USERNAME_COLUMN_LABEL
						+ " TEXT PRIMARY KEY NOT NULL);";
				s.executeUpdate(sql);
				s.close();

			} catch (SQLException sqle) {
				throw new Exception("SQL exception during table creation.");
			}

		} else {
			throw new Exception("Database not open.");
		}
	}

	/**
	 * Retrieves user information for the given username. Database must be open
	 * ( {@link #openDatabase()}).
	 * 
	 * @param username
	 *            username to find.
	 * @return <tt>username</tt> if the user is found, <tt>null</tt> if not.
	 * @throws Exception
	 *             if the search fails
	 */
	public static String getUser(final String username) throws Exception {
		String retVal = null;

		if (isDatabaseOpen()) {

			final String sql = "SELECT * FROM " + USER_TABLE_NAME + " WHERE "
					+ USERNAME_COLUMN_LABEL + "=?;";
			ResultSet result;

			// Search the table for user(s) with the given name
			try {
				PreparedStatement ps = s_connection.prepareStatement(sql);
				ps.setString(1, username);
				result = ps.executeQuery();
			} catch (SQLException sqle) {
				throw new Exception("SQL exception during user search.");
			}

			// Return the first result
			if (result.next()) {
				retVal = result.getString(USERNAME_COLUMN_LABEL);
			} else {
				// No results found
				retVal = null;
			}

		} else {
			throw new Exception("Database not open.");
		}

		return retVal;
	}

	/**
	 * Returns a list containing all users in the user table.
	 * 
	 * @return List<String> containing all users.
	 * @throws Exception
	 *             if the search fails
	 */
	public static List<String> getAllUsers() throws Exception {
		List<String> retVal = new ArrayList<String>();

		if (isDatabaseOpen()) {

			final String sql = "SELECT * FROM " + USER_TABLE_NAME + ";";
			ResultSet results;

			// Search the table for user(s) with the given name
			try {
				Statement s = s_connection.createStatement();
				results = s.executeQuery(sql);
			} catch (SQLException sqle) {
				throw new Exception("SQL exception during user search.");
			}

			// Convert the ResultSet to a list
			while (results.next()) {
				retVal.add(results.getString(USERNAME_COLUMN_LABEL));
			}

		} else {
			throw new Exception("Database not open.");
		}

		return retVal;
	}

	/**
	 * Adds a user with the given username to the user table. Database must be
	 * open ( {@link #openDatabase()}).
	 * 
	 * @param username
	 *            username to add
	 * @return <tt>true</tt> if the user is added, <tt>false</tt> if not
	 *         (because the user already exists).
	 * @throws Exception
	 *             if the add fails
	 */
	public static boolean addUser(final String username) throws Exception {
		boolean retVal = false;

		if (isDatabaseOpen()) {

			// Check if the user already exists
			String searchResult = null;
			try {
				searchResult = getUser(username);
			} catch (Exception e) {
				throw new Exception(
						"Error while checking if user exists, message: "
								+ e.getMessage());
			}

			if (null == searchResult) {
				// User does not already exist, add the user
				final String sql = "INSERT INTO " + USER_TABLE_NAME
						+ " (username) VALUES (?);";
				try {
					PreparedStatement ps = s_connection.prepareStatement(sql);
					ps.setString(1, username);
					ps.execute();
					ps.close();
					retVal = true;

				} catch (SQLException sqle) {
					throw new Exception("SQL exception during user insertion.");
				}

			} else {
				// User already exists
				retVal = false;
			}

		} else {
			throw new Exception("Database not open.");
		}

		return retVal;
	}
}
