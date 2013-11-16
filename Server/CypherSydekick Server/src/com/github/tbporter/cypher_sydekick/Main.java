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

		// Setup complete, join this thread to the server
		try {
			server.join();
		} catch (Exception e) {
			Debug.printError(TAG, "Error joining server.");
			return;
		}
	}

}
