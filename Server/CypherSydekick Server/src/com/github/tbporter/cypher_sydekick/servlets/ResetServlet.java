package com.github.tbporter.cypher_sydekick.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
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

	/** Dummy salt for password hashing. */
	private static final byte[] PASSWORD_SALT = new byte[] { 0 };
	/** Iteration count for password hashing. */
	private static final int PASSWORD_ITERATIONS = 2;
	/** Key size for password hashing. */
	private static final int PASSWORD_KEYLENGTH = 256;
	/** Hashed password to reset the database. */
	private static final byte[] PASSWORD = new byte[] { 44, 91, -30, -84, 67,
			-37, -116, 68, -12, -54, 53, -36, 83, -7, -39, 48, 3, 12, -105,
			-11, 9, 51, 39, 105, 61, -18, 33, 101, 89, 36, 25, -61 };

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
		boolean retVal = false;

		if ((null != password) && (!password.isEmpty())) {
			try {
				// Create a key object using the entered password and the
				// selected parameters
				final PBEKeySpec keySpec = new PBEKeySpec(
						password.toCharArray(), PASSWORD_SALT,
						PASSWORD_ITERATIONS, PASSWORD_KEYLENGTH);

				// Generate the hash for the entered password
				final SecretKeyFactory keyFactory = SecretKeyFactory
						.getInstance("PBKDF2WithHmacSHA1");
				final SecretKey key = keyFactory.generateSecret(keySpec);
				final byte[] encoded = key.getEncoded();

				// Print the hash of the entered password
				final String encodedString = Arrays.toString(encoded);
				Debug.printMsg(TAG, "Entered password hash size = "
						+ encoded.length + ", contents:\n" + encodedString);

				// Check the hash against the stored hash
				retVal = Arrays.equals(encoded, PASSWORD);

			} catch (Exception e) {
				// Log this and let the password check fail
				final String exceptionType = e.getClass().getName();
				Debug.printError(TAG,
						exceptionType + " in passwordValid: " + e.getMessage());
			}
		}

		return retVal;
	}
}
