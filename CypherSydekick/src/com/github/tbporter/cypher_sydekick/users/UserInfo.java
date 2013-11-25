package com.github.tbporter.cypher_sydekick.users;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.util.Base64;
import android.util.Log;

/**
 * Holds all required information about a given user.
 * 
 * @author ayelix
 * 
 */
public class UserInfo implements Serializable {
	private static final long serialVersionUID = 4925018898616501085L;
	private static final String TAG = "UserInfo";

	private final String m_username;
	private final String m_publicKey;

	/**
	 * Creates a new UserInfo object with the given username and public key.
	 */
	public UserInfo(final String username, final String publicKey) {
		m_username = username;
		m_publicKey = publicKey;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName()).append(" Object: username=")
				.append(m_username).append(", publicKey=").append(m_publicKey);
		return sb.toString();
	}

	/**
	 * Serializes the UserInfo object into a Base64 encoded string.
	 * 
	 * @return Base64 encoded String containing the serialized representation of
	 *         this object, or <tt>null</tt> if an error occurs.
	 */
	public String serializeToString() {
		String retVal = null;

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);
			oos.close();
			retVal = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
		} catch (IOException ioe) {
			// Just return null to indicate error
		}

		return retVal;
	}

	/**
	 * Deserializes a UserInfo object from a Base64 encoded string.
	 * 
	 * @param serializedString
	 *            Base64 encoded String containing the serialized representation
	 *            of a UserInfo object.
	 * @return Deserialized UserInfo object, or <tt>null</tt> if an error
	 *         occurs.
	 */
	public static UserInfo deserializeFromString(final String serializedString) {
		UserInfo retVal = null;

		try {
			final byte[] decodedBytes = Base64.decode(serializedString,
					Base64.DEFAULT);
			ByteArrayInputStream bais = new ByteArrayInputStream(decodedBytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			retVal = (UserInfo) ois.readObject();
		} catch (IOException ioe) {
			// Just return null to indicate error
		} catch (ClassNotFoundException cnfe) {
			// This exception type might indicate an error with serialization
			// (mismatched classes on client and server or something), so log
			// it.
			Log.e(TAG,
					"ClassNotFoundException in deserializeFromString, message: "
							+ cnfe.getMessage() + "\nMy serialVersionUID = "
							+ serialVersionUID);
			// Return null to indicate error
		}

		return retVal;
	}

	public String getUsername() {
		return m_username;
	}

	public String getPublicKey() {
		return m_publicKey;
	}

}
