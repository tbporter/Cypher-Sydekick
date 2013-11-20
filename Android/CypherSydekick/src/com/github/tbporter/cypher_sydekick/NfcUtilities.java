package com.github.tbporter.cypher_sydekick;

import java.nio.charset.Charset;

import android.content.Context;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

public class NfcUtilities {
	/** MIME type for messages shared over NFC. */
	public static String NDEF_MIME_TYPE = "application/com.github.tbporter.cypher_sydekick";
	/** Charset used for Strings shared over NFC. */
	public static Charset NDEF_CHARSET = Charset.forName("US-ASCII");

	/**
	 * Creates an NDEF message with the given user's information. The provided
	 * Context is used to determine the application's package name.
	 * 
	 * @param user
	 *            UserInfo to use when building the message.
	 * @param c
	 *            Context to use for determining package name.
	 * @return NdefMessage containing the user's information ready to send to
	 *         another device.
	 */
	public static NdefMessage createNdefMessage(UserInfo user, Context c) {
		// Create an NDEF Android Application Record so that this app will open
		// when the message is received.
		NdefRecord appRecord = NdefRecord.createApplicationRecord(c
				.getPackageName());

		// Create an NDEF record with the given UserInfo
		String text = user.getUsername();
		NdefRecord stringRecord = NdefRecord.createMime(NDEF_MIME_TYPE,
				text.getBytes(NDEF_CHARSET));

		// Build an NDEF message with the records created above
		NdefMessage msg = new NdefMessage(new NdefRecord[] { appRecord,
				stringRecord });

		// Return the NDEF message to broadcast
		return msg;
	}
}
