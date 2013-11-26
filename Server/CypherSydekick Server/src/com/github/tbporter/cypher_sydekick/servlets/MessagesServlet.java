package com.github.tbporter.cypher_sydekick.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;

import com.github.tbporter.cypher_sydekick.chat.ChatMessage;
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
	private static final String PARAM_MESSAGE = "message";

	/** Action for sending a message. */
	private static final String ACTION_SEND = "send";
	/** Action for receiving a message. */
	private static final String ACTION_RECEIVE = "receive";

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
		final String message = req.getParameter(PARAM_MESSAGE);

		// Exception handling for validating sender and recipient
		try {

			// Make sure sender exists
			if (DatabaseManager.getUser(sender) != null) {

				// Make sure recipient exists
				if (DatabaseManager.getUser(recipient) != null) {

					// Check the provided action
					switch (action) {
					case ACTION_SEND:
						ChatMessage newMessage = new ChatMessage(recipient, sender, message);
						if(DatabaseManager.addMessage(newMessage)){
							out.write("Sending a message from " + sender + " to "
									+ recipient + ".  Message contents:\n"
									+ message);
							resp.setStatus(STATUS_SUCCESS);
						}
						else{
							out.write("Error: Unable to add new message to the database.");
							// Response status is by default set to STATUS_ERROR
						}
						break;

					case ACTION_RECEIVE:
						ChatMessage receivedMessage = DatabaseManager.getMessage(recipient, sender);
						if(receivedMessage != null){
							out.write("Receiving a message from " + sender + " to "
									+ recipient + ".\n" + receivedMessage.getContents());
							resp.setStatus(STATUS_SUCCESS);
						}
						else{
							out.write("Error: No new messages to receive.");
						}
						break;

					default:
						out.write("Invalid action.");
						break;
					}

				} else {
					out.write("Recipient " + recipient + " does not exist.");
				}

			} else {
				out.write("Sender " + sender + " does not exist.");
			}

		} catch (DatabaseManagerException e) {
			out.write("Internal database error.");
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
