package com.github.tbporter.cypher_sydekick.nfc;

import java.nio.charset.Charset;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.util.Log;
import android.widget.Toast;

public class NFCManager {
	private static final String TAG = "NFCManager";

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

	/**
	 * Returns the NfcAdapter associated with this manager. This method should
	 * be removed once all NFC operations are moved into this class.
	 * 
	 * @return NfcAdapter associated with this manager.
	 */
	public NfcAdapter getNfcAdapter() {
		// TODO: remove this method.
		return m_nfcAdapter;
	}

	/**
	 * Checks if NFC is enabled for the adapter associated with this manager.
	 * 
	 * @return <tt>true</tt> if NFC is enabled, <tt>false</tt> if not.
	 */
	public boolean isNFCEnabled() {
		return m_nfcAdapter.isEnabled();
	}

	/**
	 * Starts the NFC foreground dispatch to enable receipt via Android Beam.
	 */
	public void startNFCReceive() {
		Log.d(TAG, "NFC receive starting");

		// Create the PendingIntent for NFC read results
		PendingIntent nfcPendingIntent = PendingIntent.getActivity(m_activity,
				0, new Intent(m_activity, m_activity.getClass())
						.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		// Enable a foreground dispatch for all tag types
		m_nfcAdapter.enableForegroundDispatch(m_activity, nfcPendingIntent,
				null, null);
	}
}
