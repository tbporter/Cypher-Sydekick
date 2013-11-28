package com.github.tbporter.cypher_sydekick.crypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class KeyDatabaseManager {
	private static Connection connection_;

	public KeyDatabaseManager() {
		try {
			Class.forName("org.sqlite.JDBC"); // Table name is "friendsList.db"
			connection_ = DriverManager
					.getConnection("jdbc:sqlite:friendsList.db");
			// System.out.println("Opened database");
			createTable(connection_);

			// Test variables. Change these to whatever you would like to test
			// functionality
			int id = 4;
			int getId;
			String name = "Jane";
			String key = "FEED9090";

			// Test functions. All the functions perform as expected. You can
			// start with a sample data
			// table, or add in entries yourself, or make a table of your own
			// with your own SQLite editor
			// (just make sure the table layout is identical)
			// These functions are mainly for making changes to the table
			// createTable(c);
			// makeSampleData(c);
			// addFriend(c, name, key);
			// removeFriend(c, id);
			// updateFriendName(c, id, newName);
			// unblockUser(c, id);

			// Functions below are mainly for searching relevant information in
			// the database
			// These two return Array lists of the friends, and you will
			// probably need to store their
			// Ids as well since you can have friends with identical names. All
			// Ids in the array are
			// respective to their friend array and ordered in an expected
			// manner
			/*
			 * ArrayList<ArrayList<Object>> friendList = getFriendList(c);
			 * ArrayList<ArrayList<Object>> idList = getIdList(c);
			 * System.out.println(friendList); System.out.println(idList);
			 */
			// Function to find an id via a unique keysign. findKey returns an
			// int of 0 if there was no
			// such result, otherwise it returns what (should be) the 8 digit
			// hex key of your friend
			/*
			 * getId = findKey(c, key);
			 * System.out.print("The id retrieved for the keysign " + key +
			 * " is "); if(getId == 0) { System.out.println("nonexistant"); }
			 * else { System.out.println(getId); }
			 */

		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	public static void createTable(Connection c) throws SQLException {
		Statement stmt = c.createStatement();
		String sql = "CREATE TABLE IF NOT EXISTS class"
				+ "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
				+ " name TEXT NOT NULL, " + " key BLOB NOT NULL, "
				+ " blocked INT NOT NULL)";
		stmt.executeUpdate(sql);
		stmt.close();
		System.out.println("Created table");
	}

	public static void makeSampleData(Connection c) throws SQLException {
		Statement stmt = c.createStatement();
		String sql = "INSERT INTO class (id,name,key,blocked) "
				+ "VALUES (1, 'Ben', 'C01BAFC3', 0);";
		stmt.executeUpdate(sql);
		sql = "INSERT INTO class (id,name,key,blocked) "
				+ "VALUES (2, 'Teja', 'FA4E723A', 0);";
		stmt.executeUpdate(sql);
		sql = "INSERT INTO class (id,name,key,blocked) "
				+ "VALUES (3, 'Alex', '008DB4A1', 0);";
		stmt.executeUpdate(sql);
		stmt.close();
	}

	public static void addFriend(Connection c, String name, byte[] key)
			throws SQLException {
		Statement stmt = c.createStatement();
		if (findKey(c, key) != 0) {
			System.err.println("Error: Couldn't add friend " + name
					+ " because that friend already exists");
		} else {
			String sql = "INSERT INTO class (name,key,blocked) " + "VALUES ('"
					+ name + "', '" + key + "', 0);";
			stmt.executeUpdate(sql);
		}
		stmt.close();
	}

	public static void updateFriendName(Connection c, int id, String newName)
			throws SQLException {
		PreparedStatement stmt = c
				.prepareStatement("UPDATE class Set name=? WHERE id=?");
		stmt.setString(1, newName);
		stmt.setInt(2, id);
		stmt.executeUpdate();
		stmt.close();
	}

	public static void removeFriend(Connection c, int id) throws SQLException {
		PreparedStatement stmt = c
				.prepareStatement("DELETE FROM class WHERE id=?");
		stmt.setInt(1, id);
		stmt.executeUpdate();
		stmt.close();
	}

	public static void blockUser(Connection c, int id) throws SQLException {
		PreparedStatement stmt = c
				.prepareStatement("UPDATE class Set blocked=? WHERE id=?");
		stmt.setInt(1, 1);
		stmt.setInt(2, id);
		stmt.executeUpdate();
		stmt.close();
	}

	public static void unblockUser(Connection c, int id) throws SQLException {
		PreparedStatement stmt = c
				.prepareStatement("UPDATE class Set blocked=? WHERE id=?");
		stmt.setInt(1, 0);
		stmt.setInt(2, id);
		stmt.executeUpdate();
		stmt.close();
	}

	public static ArrayList<ArrayList<Object>> getFriendList(Connection c)
			throws SQLException {
		Statement getData = c.createStatement();
		ArrayList<ArrayList<Object>> nameList = null;

		ResultSet nameRS = getData.executeQuery("SELECT name FROM class;");
		try {
			nameList = ResultsToArray(nameRS);
		} catch (Exception e) {
			// Turned this message off for now. Condition here is table is empty
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
		getData.close();

		return nameList;
	}

	public static ArrayList<ArrayList<Object>> getIdList(Connection c)
			throws SQLException {
		Statement getData = c.createStatement();
		ArrayList<ArrayList<Object>> idList = null;

		ResultSet idRS = getData.executeQuery("SELECT id FROM class;");
		try {
			idList = ResultsToArray(idRS);
		} catch (Exception e) {
			// Turned this message off for now. Condition here is table is empty
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
		getData.close();

		return idList;
	}

	public static int findKey(Connection c, byte[] key) throws SQLException {
		Statement getData = c.createStatement();
		int retrievedData = 0;

		ResultSet rs = getData.executeQuery("SELECT id FROM class WHERE key='"
				+ key + "';");
		try {
			retrievedData = rs.getInt("id");
		} catch (Exception e) {
			// Turned this message off for now because it pops up each time when
			// adding a friend...
			// System.err.println(e.getClass().getName() + ": " +
			// e.getMessage());
		}
		getData.close();

		return retrievedData;
	}
	
	// TODO: validate that this works and implement it to return the key in bytes
	public static byte[] getKey(Connection c, String username){
		
		int retrievedData = 0;
		byte[] key = null;
		
		try {
			Statement getData = c.createStatement();
			ResultSet rs = getData.executeQuery("SELECT id FROM class WHERE name='"
					+ username + "';");
			retrievedData = rs.getInt("id");
			key = rs.getBytes("name");
			getData.close();
		} catch (Exception e) {
			// Turned this message off for now because it pops up each time when
			// adding a friend...
			// System.err.println(e.getClass().getName() + ": " +
			// e.getMessage());
		}
		

		return key;
	}

	public static ArrayList<ArrayList<Object>> ResultsToArray(ResultSet rs)
			throws SQLException {
		ResultSetMetaData metaData = rs.getMetaData();
		int columns = metaData.getColumnCount();

		ArrayList<ArrayList<Object>> al = new ArrayList<ArrayList<Object>>();

		while (rs.next()) {
			ArrayList<Object> record = new ArrayList<Object>();

			for (int i = 1; i <= columns; i++) {
				String value = rs.getString(i);
				record.add(value);
			}
			al.add(record);
		}
		return al;
	}
}