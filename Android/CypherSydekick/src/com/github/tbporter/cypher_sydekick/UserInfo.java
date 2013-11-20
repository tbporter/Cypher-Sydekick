package com.github.tbporter.cypher_sydekick;

import java.io.Serializable;

public class UserInfo implements Serializable {
	/** This user's username. */
	private final String m_username;
	/** This user's public encryption key. */
	private final String m_publicKey;

	/**
	 * Creates a user with the given information.
	 * 
	 * @param username
	 *            Username for the user.
	 * @param publicKey
	 *            Public key for the user.
	 */
	public UserInfo(final String username, final String publicKey) {
		m_username = username;
		m_publicKey = publicKey;
	}
	
	public String getUsername() {
		return m_username;
	}
	
	public String getPublicKey() {
		return m_publicKey;
	}
}
