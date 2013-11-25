package com.github.tbporter.cypher_sydekick.users;

/**
 * Holds all required information about a given user.
 * 
 * @author ayelix
 * 
 */
public class UserInfo {
	private final String m_username;
	private final String m_publicKey;

	/**
	 * Creates a new UserInfo object with the given username and public key.
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
