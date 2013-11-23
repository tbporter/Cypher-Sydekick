package com.github.tbporter.cypher_sydekick.nfc;

import java.nio.charset.Charset;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.widget.Toast;

public class NFCManager {

	/** MIME type for messages shared over NFC. */
	public static final String NDEF_MIME_TYPE = "application/com.github.tbporter.cypher_sydekick";
	/** Charset used for Strings shared over NFC. */
	public static final Charset NDEF_CHARSET = Charset.forName("US-ASCII");

	/** NFC adapter this manager uses */
	private final NfcAdapter m_nfcAdapter;

	/** Activity this manager is associated with. */
	private final Activity m_activity;

	/**
	 * Creates a new NFCManager using the given NfcAdapter and associated with
	 * the given Activity.
	 * 
	 * @param nfcAdapter
	 *            NfcAdapter this manager will use.
	 * @param activity
	 *            Activity this manager is associated with.
	 * @throws NFCManagerException
	 *             If the given NfcAdapter is not valid (i.e. NFC is not
	 *             available).
	 */
	public NFCManager(final NfcAdapter nfcAdapter, final Activity activity)
			throws NFCManagerException {
		m_nfcAdapter = nfcAdapter;
		m_activity = activity;

		// Make sure NFC is available
		if (null == m_nfcAdapter) {
			throw new NFCManagerException("NFC is not available.");
		}
	}

	public NfcAdapter getNfcAdapter() {
		return m_nfcAdapter;
	}
}
