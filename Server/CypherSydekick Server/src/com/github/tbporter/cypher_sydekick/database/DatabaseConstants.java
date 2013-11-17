package com.github.tbporter.cypher_sydekick.database;

/**
 * Contains various constants (mostly strings) for database operations.
 * 
 * @author ayelix
 * 
 */
abstract class DatabaseConstants {
	/** Location of the main database. */
	public static final String DB_NAME = "jdbc:sqlite:war/WEB-INF/userlist.db";

	/** Name of the users table. */
	public static final String USERS_TABLE_NAME = "users";
	/** Name of the users table's username column. */
	public static final String USERNAME_COLUMN_LABEL = "username";
	/** Name of the users table's messages column. */
	public static final String MESSAGES_COLUMN_LABEL = "messages";
	/** SQL string to create users table. */
	public static final String USERS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
			+ USERS_TABLE_NAME
			+ " ("
			+ USERNAME_COLUMN_LABEL
			+ " TEXT PRIMARY KEY NOT NULL, "
			+ MESSAGES_COLUMN_LABEL
			+ " TEXT);";

	/** Name of the messages table. */
	public static final String MESSAGES_TABLE_NAME = "messages";
	/** Name of the messages table's sender column. */
	public static final String SENDER_COLUMN_LABEL = "sender";
	/** Name of the messages table's contents column. */
	public static final String CONTENTS_COLUMN_LABEL = "contents";
	/** SQL string to create messages table. */
	public static final String MESSAGES_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ MESSAGES_TABLE_NAME
			+ " ("
			+ SENDER_COLUMN_LABEL
			+ " TEXT NOT NULL, " + CONTENTS_COLUMN_LABEL + " TEXT NOT NULL);";
}
