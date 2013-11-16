package com.github.tbporter.cypher_sydekick;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import com.github.tbporter.cypher_sydekick.debugging.Debug;

public final class Main {
	/** Location of the main database. */
	private static String DB_NAME = "jdbc:sqlite:war/WEB-INF/userlist.db";
	private static final String TAG = "Main";

	public static void main(String[] args) {
		// Start the server
		Server server = new Server(8080);
		WebAppContext context = new WebAppContext();
		context.setWar("war");
		context.setContextPath("/");
		server.setHandler(context);
		try {
			server.start();
		} catch (Exception e) {
			Debug.printError(TAG, "Error starting server.");
			return;
		}

		// Open the database
		try {
			Class.forName(org.sqlite.JDBC.class.getName());
			Connection c = DriverManager.getConnection(DB_NAME);

		} catch (ClassNotFoundException cnfe) {
			Debug.printError(TAG, "Unable to find JDBC class.");
			return;

		} catch (SQLException sqle) {
			Debug.printError(TAG, "SQL exception while opening database.");
			sqle.printStackTrace();
			return;
		}

		// Setup complete, join this thread to the server
		try {
			server.join();
		} catch (Exception e) {
			Debug.printError(TAG, "Error joining server.");
			return;
		}
	}

}
