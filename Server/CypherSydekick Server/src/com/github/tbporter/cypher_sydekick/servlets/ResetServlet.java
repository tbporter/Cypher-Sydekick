package com.github.tbporter.cypher_sydekick.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;

import com.github.tbporter.cypher_sydekick.database.DatabaseManager;
import com.github.tbporter.cypher_sydekick.database.DatabaseManagerException;
import com.github.tbporter.cypher_sydekick.debugging.Debug;

public class ResetServlet extends HttpServlet {
	private static final String TAG = "ResetServlet";
	
	/** Parameter key for reset password. */
	private static final String PARAM_PASSWORD = "password";

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
		final String password = req.getParameter(PARAM_PASSWORD);

		// Make sure parameters were provided
		if ((null != password) && !password.isEmpty()) {
			if (passwordValid(password)) {
				try {
					// Drop all tables then re-create them
					DatabaseManager.dropAllTables();
					Debug.printMsg(TAG, "Dropped all tables.");
					DatabaseManager.createUserTable();
					Debug.printMsg(TAG, "Created users table.");
					DatabaseManager.createMessagesTable();
					Debug.printMsg(TAG, "Created messages table.");
					
					// Indicate success
					resp.setStatus(STATUS_SUCCESS);
					out.write("Database reset.");
					
				} catch (DatabaseManagerException e) {
					out.write("Internal database error.");
				}

			} else {
				out.write("Incorrect password.");
			}

		} else {
			out.write("No password provided with request.");
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

	/**
	 * Checks if a password is valid for resetting the database.
	 * 
	 * @param password
	 *            Provided password to be checked.
	 * @return <tt>true</tt> if the password is valid, <tt>false</tt> if not.
	 */
	private boolean passwordValid(final String password) {
		boolean retVal = true;

		return retVal;
	}

}
