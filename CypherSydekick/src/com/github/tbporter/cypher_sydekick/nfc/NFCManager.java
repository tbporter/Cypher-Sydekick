package com.github.tbporter.cypher_sydekick.nfc;

import java.nio.charset.Charset;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.util.Log;
import android.widget.Toast;

public class NFCManager implements CreateNdefMessageCallback {
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

		// Set this class as the callback to create an NDEF push message for the
		// associated Activity
		m_nfcAdapter.setNdefPushMessageCallback(this, m_activity);
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

	/**
	 * Stops the NFC foreground dispatch - Android Beam will no longer be
	 * received.
	 */
	public void stopNFCReceive() {
		Log.d(TAG, "NFC receive stopping");

		// Disable NFC foreground dispatch
		m_nfcAdapter.disableForegroundDispatch(m_activity);
	}

	/**
	 * Checks if an NdefRecord is the type expected for data exchange.
	 * 
	 * @param record
	 *            The record to check.
	 * @return <tt>true</tt> if the record is the type expected, <tt>false</tt>
	 *         if not.
	 */
	public boolean checkNdefRecord(final NdefRecord record) {
		boolean retVal = true;

		// Requires API 16:
		// Check the MIME type
		final String mime = record.toMimeType();
		if (null != mime) {
			// The record has a MIME type, make sure it's correct
			if (!NFCManager.NDEF_MIME_TYPE.equals(mime)) {
				retVal = false;
				Log.d(TAG, "Checked NdefRecord with unknown MIME type: " + mime);
				Log.d(TAG,
						"MIME type via old method: "
								+ new String(record.getType(),
										NFCManager.NDEF_CHARSET));
			}
		} else {
			// No MIME type found, this is not a valid record
			retVal = false;
			Log.d(TAG, "Checked NdefRecord with null MIME type");
		}

		return retVal;
	}

	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		Log.d(TAG, "Building NDEF message");

		// Create an NDEF Android Application Record so that this app will open
		// when the message is received.
		final NdefRecord appRecord = NdefRecord
				.createApplicationRecord(m_activity.getApplicationContext()
						.getPackageName());

		// Create an NDEF record with the string to transmit
		final String text = "text";
		final NdefRecord stringRecord = NdefRecord.createMime(
				NFCManager.NDEF_MIME_TYPE,
				text.getBytes(NFCManager.NDEF_CHARSET));

		// Build an NDEF message with the records created above
		final NdefMessage msg = new NdefMessage(new NdefRecord[] { appRecord,
				stringRecord });

		// Return the NDEF message to broadcast
		return msg;
	}
}
