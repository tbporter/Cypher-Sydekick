package com.github.tbporter.cypher_sydekick;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import com.github.tbporter.cypher_sydekick.debugging.Debug;

public final class Main {
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

		// Open the database and create the table
		try {
			DatabaseManager.openDatabase();
			DatabaseManager.createUserTable();
		} catch (Exception e) {
			Debug.printError(TAG, e.getMessage());
			return;
		}
		
		// Do some user search/add testing
		try {
			String result;
			result = DatabaseManager.getUser("ayelix");
			if (null != result) {
				Debug.printMsg(TAG, "Found user ayelix");
			} else {
				Debug.printMsg(TAG, "Didn't find user ayelix, adding him");
			}
			
			if (DatabaseManager.addUser("ayelix")) {
				Debug.printMsg(TAG, "Added user ayelix");
			} else {
				Debug.printError(TAG, "Did not add user ayelix");
			}
			
			result = DatabaseManager.getUser("ayelix");
			if (null != result) {
				Debug.printMsg(TAG, "Second search, found user ayelix");
			} else {
				Debug.printError(TAG, "Second search, didn't find user ayelix");
			}
		} catch (Exception e) {
			Debug.printError(TAG, e.getMessage());
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
