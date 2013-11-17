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

	/** Connection to the database, created in {@link #openDatabase()}. */
	private static Connection s_connection;

	/**
	 * Attempts to connect to the database.
	 * 
	 * @throws DatabaseManagerException
	 *             if the connection fails.
	 */
	public static void openDatabase() throws DatabaseManagerException {
		try {
			Class.forName(org.sqlite.JDBC.class.getName());
			s_connection = DriverManager
					.getConnection(DatabaseConstants.DB_NAME);

		} catch (ClassNotFoundException cnfe) {
			throw new DatabaseManagerException(
					"Cannot open database - unable to load JDBC driver class.");
		} catch (SQLException sqle) {
			throw new DatabaseManagerException(
					"SQL exception while opening database.");
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
	 * @throws DatabaseManagerException
	 *             if table creation fails or database has not been opened.
	 */
	public static void createUserTable() throws DatabaseManagerException {
		createTable(DatabaseConstants.USERS_TABLE_CREATE);
	}

	/**
	 * Creates the table containing messages. Database must be open (
	 * {@link #openDatabase()}).
	 * 
	 * @throws DatabaseManagerException
	 *             if table creation fails or database has not been opened.
	 */
	public static void createMessagesTable() throws DatabaseManagerException {
		createTable(DatabaseConstants.MESSAGES_CREATE_TABLE);
	}

	/**
	 * Retrieves user information for the given username. Database must be open
	 * ( {@link #openDatabase()}).
	 * 
	 * @param username
	 *            username to find.
	 * @return <tt>username</tt> if the user is found, <tt>null</tt> if not.
	 * @throws DatabaseManagerException
	 *             if the search fails
	 */
	public static String getUser(final String username)
			throws DatabaseManagerException {
		String retVal = null;

		if (isDatabaseOpen()) {

			final String sql = "SELECT * FROM "
					+ DatabaseConstants.USERS_TABLE_NAME + " WHERE "
					+ DatabaseConstants.USERNAME_COLUMN_LABEL + "=?;";
			ResultSet result;

			// Search the table for user(s) with the given name
			try {
				PreparedStatement ps = s_connection.prepareStatement(sql);
				ps.setString(1, username);
				result = ps.executeQuery();

				// Return the first result
				if (result.next()) {
					retVal = result
							.getString(DatabaseConstants.USERNAME_COLUMN_LABEL);
				} else {
					// No results found
					retVal = null;
				}

			} catch (SQLException sqle) {
				throw new DatabaseManagerException(
						"SQL exception during user search.");
			}

		} else {
			throw new DatabaseManagerException("Database not open.");
		}

		return retVal;
	}

	/**
	 * Returns a list containing all users in the user table.
	 * 
	 * @return List<String> containing all users.
	 * @throws DatabaseManagerException
	 *             if the search fails
	 */
	public static List<String> getAllUsers() throws DatabaseManagerException {
		List<String> retVal = new ArrayList<String>();

		if (isDatabaseOpen()) {

			final String sql = "SELECT * FROM "
					+ DatabaseConstants.USERS_TABLE_NAME + ";";
			ResultSet results;

			// Search the table for user(s) with the given name
			try {
				Statement s = s_connection.createStatement();
				results = s.executeQuery(sql);

				// Convert the ResultSet to a list
				while (results.next()) {
					retVal.add(results
							.getString(DatabaseConstants.USERNAME_COLUMN_LABEL));
				}

			} catch (SQLException sqle) {
				throw new DatabaseManagerException(
						"SQL exception during user search.");
			}

		} else {
			throw new DatabaseManagerException("Database not open.");
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
	 * @throws DatabaseManagerException
	 *             if the add fails
	 */
	public static boolean addUser(final String username)
			throws DatabaseManagerException {
		boolean retVal = false;

		if (isDatabaseOpen()) {

			// Check if the user already exists
			String searchResult = null;
			try {
				searchResult = getUser(username);
			} catch (DatabaseManagerException e) {
				throw new DatabaseManagerException(
						"Error while checking if user exists, message: "
								+ e.getMessage());
			}

			if (null == searchResult) {
				// User does not already exist, add the user
				final String sql = "INSERT INTO "
						+ DatabaseConstants.USERS_TABLE_NAME
						+ " (username) VALUES (?);";
				try {
					PreparedStatement ps = s_connection.prepareStatement(sql);
					ps.setString(1, username);
					ps.execute();
					ps.close();
					retVal = true;

				} catch (SQLException sqle) {
					throw new DatabaseManagerException(
							"SQL exception during user insertion.");
				}

			} else {
				// User already exists
				retVal = false;
			}

		} else {
			throw new DatabaseManagerException("Database not open.");
		}

		return retVal;
	}

	/**
	 * Creates a table using the given SQL statement string.
	 * 
	 * @param tableCreateSqlString
	 *            SQL statement string to execute in order to create the table.
	 * @throws DatabaseManagerException
	 *             if table creation fails or database has not been opened.
	 */
	private static void createTable(final String tableCreateSqlString)
			throws DatabaseManagerException {
		if (isDatabaseOpen()) {
			try {
				Statement s = s_connection.createStatement();
				s.executeUpdate(tableCreateSqlString);
				s.close();

			} catch (SQLException sqle) {
				throw new DatabaseManagerException(
						"SQL exception during table creation.");
			}

		} else {
			throw new DatabaseManagerException("Database not open.");
		}
	}
}
