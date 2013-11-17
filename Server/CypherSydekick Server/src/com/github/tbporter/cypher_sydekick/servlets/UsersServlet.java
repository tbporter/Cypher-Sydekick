package com.github.tbporter.cypher_sydekick.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;

import com.github.tbporter.cypher_sydekick.database.DatabaseManager;
import com.github.tbporter.cypher_sydekick.database.DatabaseManagerException;

/**
 * Handles requests to add new users.
 * 
 * @author ayelix
 * 
 */
public class UsersServlet extends HttpServlet {
	/** Parameter key for username. */
	private static final String PARAM_USERNAME = "username";

	/** Default error code sent for general errors. */
	private static final int STATUS_ERROR = HttpStatus.INTERNAL_SERVER_ERROR_500;
	/** Default success code sent for seccessful operations. */
	private static final int STATUS_SUCCESS = HttpStatus.OK_200;

	/**
	 * Accepts new users based on parameters provided with the request.
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		PrintWriter out = resp.getWriter();

		// Set the error code (assume there will be an error)
		resp.setStatus(STATUS_ERROR);

		// Read parameters from the request
		final String username = req.getParameter(PARAM_USERNAME);

		// Make sure parameters were provided
		if (null != username) {
			// Add the user
			try {
				if (DatabaseManager.addUser(username)) {

					// If addUser returns true, the user was added
					resp.setStatus(STATUS_SUCCESS);
					out.write("User " + username + " added successfully.");

				} else {

					// If addUser returns false, the user already exists
					out.write("User " + username + " already exists");

				}

			} catch (DatabaseManagerException e) {
				out.write("Internal database error.");
			}

		} else {
			out.write("No username provided with request.");
		}
	}

	/**
	 * Forwards requests to doGet().
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		doGet(req, resp);
	}

}
