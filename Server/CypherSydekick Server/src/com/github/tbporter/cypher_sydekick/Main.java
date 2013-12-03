package com.github.tbporter.cypher_sydekick;

import java.util.List;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.github.tbporter.cypher_sydekick.chat.ChatMessage;
import com.github.tbporter.cypher_sydekick.database.DatabaseManager;
import com.github.tbporter.cypher_sydekick.database.DatabaseManagerException;
import com.github.tbporter.cypher_sydekick.debugging.Debug;
import com.github.tbporter.cypher_sydekick.servlets.MessagesServlet;
import com.github.tbporter.cypher_sydekick.servlets.ResetServlet;
import com.github.tbporter.cypher_sydekick.servlets.RootServlet;
import com.github.tbporter.cypher_sydekick.servlets.UsersServlet;

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
		context.addServlet(UsersServlet.class, "/users");
		context.addServlet(MessagesServlet.class, "/messages");
		context.addServlet(ResetServlet.class, "/reset");

		// Start the server
		server.setHandler(context);
		try {
			server.start();
		} catch (Exception e) {
			Debug.printError(TAG, "Error starting server: " + e.getMessage());
			return;
		}

		// Open the database and create the tables
		try {
			DatabaseManager.openDatabase();
			DatabaseManager.createUserTable();
			DatabaseManager.createMessagesTable();
		} catch (DatabaseManagerException e) {
			Debug.printError(TAG,
					"DatabaseManagerException while initializing database: "
							+ e.getMessage());
			return;
		}

		try {
			// List all users in the database
			String msg = "All users in database:\n";
			List<String> users = DatabaseManager.getAllUsers();
			for (final String user : users) {
				msg += user + "\n";
			}
			Debug.printMsg(TAG, msg);
		} catch (DatabaseManagerException dme) {
			Debug.printError(
					TAG,
					"DatabaseManagerException while listing users: "
							+ dme.getMessage());
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
