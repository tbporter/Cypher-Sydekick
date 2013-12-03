package com.github.tbporter.cypher_sydekick.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.github.tbporter.cypher_sydekick.chat.ChatMessage;
import com.github.tbporter.cypher_sydekick.debugging.Debug;

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
					"SQL exception while opening database: "
							+ sqle.getMessage());
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
	 *             if table creation fails
	 */
	public static void createUserTable() throws DatabaseManagerException {
		createTable(DatabaseConstants.USERS_TABLE_CREATE);
	}

	/**
	 * Creates the table containing messages. Database must be open (
	 * {@link #openDatabase()}).
	 * 
	 * @throws DatabaseManagerException
	 *             if table creation fails
	 */
	public static void createMessagesTable() throws DatabaseManagerException {
		createTable(DatabaseConstants.MESSAGES_CREATE_TABLE);
	}

	/**
	 * Drops all tables in the database. Database must be open (
	 * {@link #openDatabase()}). <b>Tables are not recreated after they are
	 * dropped.</b>
	 * 
	 * @throws DatabaseManagerException
	 *             if table dropping fails
	 */
	public static void dropAllTables() throws DatabaseManagerException {
		if (isDatabaseOpen()) {
			try {
				Statement s = s_connection.createStatement();
				s.executeUpdate(DatabaseConstants.USERS_TABLE_DROP);
				s.executeUpdate(DatabaseConstants.MESSAGES_TABLE_DROP);
				s.close();

			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw new DatabaseManagerException(
						"SQL exception during table deletion.");
			}

		} else {
			throw new DatabaseManagerException("Database not open.");
		}
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
				
				result.close();
				ps.close();

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

			// Search the table for user(s) with the given name
			try {
				Statement s = s_connection.createStatement();
				ResultSet results = s.executeQuery(sql);

				// Convert the ResultSet to a list
				while (results.next()) {
					retVal.add(results
							.getString(DatabaseConstants.USERNAME_COLUMN_LABEL));
				}
				
				results.close();
				s.close();

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
			} catch (DatabaseManagerException dme) {
				throw new DatabaseManagerException(
						"Error while checking if user exists, message: "
								+ dme.getMessage());
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
	 * Gets the next message from the given sender to the given receiver. <br>
	 * <b>The message is deleted once it is retrieved with this method.</b>
	 * 
	 * @param receiver
	 *            username of receiver
	 * @param sender
	 *            username of sender
	 * @return The next ChatMessage from <tt>sender</tt> to <tt>receiver</tt>,
	 *         or <tt>null</tt> if none available.
	 * @throws DatabaseManagerException
	 *             if users do not exist or if the search fails
	 */
	public static ChatMessage getMessage(final String receiver,
			final String sender) throws DatabaseManagerException {
		ChatMessage msg = null;

		if (isDatabaseOpen()) {
			String searchResult = null;

			// Make sure receiver exists
			try {
				searchResult = getUser(receiver);
			} catch (DatabaseManagerException dme) {
				throw new DatabaseManagerException(
						"Error while checking if user exists, message: "
								+ dme.getMessage());
			}
			if (null == searchResult) {
				throw new DatabaseManagerException("Receiver " + receiver
						+ " does not exist.");
			}

			// Make sure sender exists
			try {
				searchResult = getUser(sender);
			} catch (DatabaseManagerException dme) {
				throw new DatabaseManagerException(
						"Error while checking if user exists, message: "
								+ dme.getMessage());
			}
			if (null == searchResult) {
				throw new DatabaseManagerException("Sender " + sender
						+ " does not exist.");
			}

			// Search the table for a row with matching receiver and sender
			try {
				final String sql = "SELECT ROWID,* FROM "
						+ DatabaseConstants.MESSAGES_TABLE_NAME + " WHERE "
						+ DatabaseConstants.RECEIVER_COLUMN_LABEL + "=? AND "
						+ DatabaseConstants.SENDER_COLUMN_LABEL + "=? LIMIT 1;";
				ResultSet result;
				PreparedStatement ps = s_connection.prepareStatement(sql);
				ps.setString(1, receiver);
				ps.setString(2, sender);
				result = ps.executeQuery();

				// Prepare to return the first result
				if (result.next()) {
					final String msgReceiver = result
							.getString(DatabaseConstants.RECEIVER_COLUMN_LABEL);
					final String msgSender = result
							.getString(DatabaseConstants.SENDER_COLUMN_LABEL);
					final String msgContents = result
							.getString(DatabaseConstants.CONTENTS_COLUMN_LABEL);
					msg = new ChatMessage(msgReceiver, msgSender, msgContents);

					// Delete the message now that it has been received
					final int msgID = result.getInt("ROWID");
					try {
						deleteMessage(msgID);
					} catch (DatabaseManagerException dme) {
						throw new DatabaseManagerException(
								"Error while deleting message, exception message: "
										+ dme.getMessage());
					}

				} else {
					// No results found
					msg = null;
				}
				
				result.close();
				ps.close();

			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw new DatabaseManagerException(
						"SQL exception during user search.");
			}

		} else {
			throw new DatabaseManagerException("Database not open.");
		}

		return msg;
	}

	/**
	 * Adds the given message to the messages table.
	 * 
	 * @param msg
	 *            ChatMessage to add
	 * @return <tt>true</tt> if the message was added, <tt>false</tt> if not.
	 * @throws DatabaseManagerException
	 *             if the add fails
	 */
	public static boolean addMessage(final ChatMessage msg)
			throws DatabaseManagerException {
		boolean retVal = false;

		if (isDatabaseOpen()) {

			// Add the message to the table
			try {
				final String sql = "INSERT INTO "
						+ DatabaseConstants.MESSAGES_TABLE_NAME
						+ " (receiver, sender, contents) VALUES (?, ?, ?);";
				PreparedStatement ps = s_connection.prepareStatement(sql);
				ps.setString(1, msg.getReceiver());
				ps.setString(2, msg.getSender());
				ps.setString(3, msg.getContents());
				ps.execute();
				ps.close();
				retVal = true;

			} catch (SQLException sqle) {
				throw new DatabaseManagerException(
						"SQL exception during message insertion.");
			}

		} else {
			throw new DatabaseManagerException("Database not open.");
		}

		return retVal;
	}

	/**
	 * Deletes the message with the given ROWID. Users should never need to know
	 * row IDs, so this is private.
	 * 
	 * @param rowId
	 *            ROWID for the message to delete.
	 * @throws DatabaseManagerException
	 *             if the delete fails.
	 */
	private static void deleteMessage(final int rowId)
			throws DatabaseManagerException {
		if (isDatabaseOpen()) {

			// Add the message to the table
			try {
				final String sql = "DELETE FROM "
						+ DatabaseConstants.MESSAGES_TABLE_NAME
						+ " WHERE ROWID=?;";
				PreparedStatement ps = s_connection.prepareStatement(sql);
				ps.setInt(1, rowId);
				ps.execute();
				ps.close();

			} catch (SQLException sqle) {
				throw new DatabaseManagerException(
						"SQL exception during message deletion.");
			}

		} else {
			throw new DatabaseManagerException("Database not open.");
		}
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
