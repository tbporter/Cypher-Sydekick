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
 * @author tejachil
 * 
 */
public class MessagesServlet extends HttpServlet {
	/** Parameter keys message API. */
	private static final String PARAM_ACTION = "action";
	private static final String PARAM_RECIPIENT = "recipient";
	private static final String PARAM_SENDER = "sender";
	

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
		final String action = req.getParameter(PARAM_ACTION);
		final String sender = req.getParameter(PARAM_SENDER);
		final String recipient = req.getParameter(PARAM_RECIPIENT);
		
		boolean validParams = true; // Assume params are valid first
		
		// Exception handling for validating sender and recipient
		try {
			if(DatabaseManager.getUser(sender) == null){
				out.write("Sender " + sender + " does not exist.");
				validParams = false;
			}
			if(DatabaseManager.getUser(recipient) == null){
				out.write("Recipient " + sender + " does not exist.");
				validParams = false;
			}
		} catch (DatabaseManagerException e) {
			out.write("Internal database error.");
		}
		
		// Make sure parameters were provided
		if (action == "send" && validParams) {


		} 
		else if (action == "receive" && validParams){
			
		}
		else {
			out.write("Invalid action.");
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
