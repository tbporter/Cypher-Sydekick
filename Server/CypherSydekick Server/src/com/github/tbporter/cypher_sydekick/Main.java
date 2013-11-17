package com.github.tbporter.cypher_sydekick;

import java.util.List;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.github.tbporter.cypher_sydekick.database.DatabaseManager;
import com.github.tbporter.cypher_sydekick.database.DatabaseManagerException;
import com.github.tbporter.cypher_sydekick.debugging.Debug;
import com.github.tbporter.cypher_sydekick.servlets.AddServlet;
import com.github.tbporter.cypher_sydekick.servlets.RootServlet;

/**
 * Configures and starts the server and database.
 * 
 * @author ayelix
 * 
 */
public final class Main {
	private static final String TAG = "Main";

	/**
	 * Configures and starts the server and database.
	 * 
	 * @param args
	 *            command-line arguments
	 */
	public static void main(String[] args) {
		// Set up file serving via DefaultServlet
		final Server server = new Server(8080);
		final ServletContextHandler context = new ServletContextHandler(
				ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		context.setResourceBase("./war/res");
		context.setWelcomeFiles(new String[] { "index.html" });
		context.setClassLoader(Thread.currentThread().getContextClassLoader());
		context.addServlet(DefaultServlet.class, "/");

		// Set up custom servlets
		context.addServlet(RootServlet.class, ""); // Empty string maps to
													// *exactly* root
		context.addServlet(AddServlet.class, "/users");

		// Start the server
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
		} catch (DatabaseManagerException e) {
			Debug.printError(TAG, e.getMessage());
			return;
		}

		// Do some user search/add testing
		try {
			// List all users in the database
			String msg = "All users in database:\n";
			final List<String> users = DatabaseManager.getAllUsers();
			for (final String user : users) {
				msg += user + "\n";
			}
			Debug.printMsg(TAG, msg);

			String result;
			final String newUser = "cj123";

			// See if the user is there already
			result = DatabaseManager.getUser(newUser);
			if (null != result) {
				Debug.printMsg(TAG, "Found user " + newUser);
			} else {
				Debug.printMsg(TAG, "Didn't find user " + newUser
						+ ", adding user");
			}

			// See if we can add the user
			if (DatabaseManager.addUser(newUser)) {
				Debug.printMsg(TAG, "Added user " + newUser);
			} else {
				Debug.printMsg(TAG, "Did not add user " + newUser);
			}

			// See if the user is there now
			result = DatabaseManager.getUser(newUser);
			if (null != result) {
				Debug.printMsg(TAG, "Second search, found user " + newUser);
			} else {
				Debug.printError(TAG, "Second search, didn't find user "
						+ newUser);
			}
		} catch (DatabaseManagerException e) {
			Debug.printError(TAG, "Database exception: " + e.getMessage());
		}

		// Setup complete, join this thread to the server
		try {
			server.join();
		} catch (InterruptedException e) {
			Debug.printError(TAG, "Server interrupted.");
			return;
		}
	}

}
