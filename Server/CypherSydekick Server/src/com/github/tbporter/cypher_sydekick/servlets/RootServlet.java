package com.github.tbporter.cypher_sydekick.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.tbporter.cypher_sydekick.database.DatabaseManager;
import com.github.tbporter.cypher_sydekick.database.DatabaseManagerException;

/**
 * Serves a diagnostic page for viewing and adding users.
 * 
 * @author ayelix
 * 
 */
public class RootServlet extends HttpServlet {
	/** Header for HTML pages. */
	private static final String HTML_HEAD = "<!DOCTYPE html>\n<html>\n<body><center>Cypher Sydekick Webserver Utility</center>\n";
	/** Footer for HTML pages. */
	private static final String HTML_FOOT = "\n</body>\n</html>";

	/** HTML string to start the table. */
	private static final String HTML_TABLE_HEAD = "<table border=\"1\" cellpadding=\"15\" align=\"center\"><tr><td>/users</td><td>/messages</td></tr><tr>\n";
	/** HTML string to complete the table. */
	private static final String HTML_TABLE_FOOT = "</tr></table>\n";

	/** HTML string for "Delete Users" form. */
	private static final String HTML_DELETE_USERS_FORM = "<form action=\"reset\" method=\"post\">\nPassword: <input type=\"text\" name=\"password\"> <input type=\"submit\" value=\"Reset database\">\n</form>\n";

	/** HTML string for "Add User" form. */
	private static final String HTML_ADD_USER_FORM = "<form action=\"users\" method=\"post\">\nUsername: <input type=\"text\" name=\"username\">\n<input type=\"submit\" value=\"Add User\">\n</form>\n";

	/** HTML string for the Messages form. */
	private static final String HTML_MESSAGES_FORM = "<center>\n<form action=\"messages\" method=\"post\">\n"
			+ "Sender: <input type=\"text\" name=\"sender\">\n   "
			+ "Recipient: <input type=\"text\" name=\"recipient\">\n"
			+ "<br>Message:"
			+ "<br><textarea name=\"message\" rows=\"5\" cols=\"30\"></textarea>\n"
			+ "<br><input type=\"submit\" name=\"action\" value=\"send\">\n"
			+ "<input type=\"submit\" name=\"action\" value=\"receive\">\n</form>\n</center>\n";

	/**
	 * Writes the "add user" form and list of users.
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();

		out.write(HTML_HEAD);
		
		out.write("<center>");
		out.write(HTML_DELETE_USERS_FORM);
		out.write("</center>");

		out.write(HTML_TABLE_HEAD);

		// Place the add user form at the top
		out.write("<td>"); // Create a new column
		out.write(HTML_ADD_USER_FORM);

		// Print all current users below the form
		out.write("<b>Current users:</b><br>\n");
		try {
			final List<String> users = DatabaseManager.getAllUsers();
			for (final String user : users) {
				out.write(user + "<br>\n");
			}

		} catch (DatabaseManagerException e) {
			out.write("<i>Unable to read user list.</i><br>\n");
		}
		out.write("</td>"); // End column

		out.write("<td>"); // Create a new column
		out.write(HTML_MESSAGES_FORM);
		out.write("</td>"); // End column

		out.write(HTML_TABLE_FOOT);

		out.write(HTML_FOOT);
	}

	/**
	 * Forwards request to doGet()
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		doGet(req, resp);
	}

}
