package com.github.tbporter.cypher_sydekick.nfc;

import java.nio.charset.Charset;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.github.tbporter.cypher_sydekick.users.UserInfo;

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
	 * Checks if the given Intent is one that this manager will handle.
	 * 
	 * @param intent
	 *            The Intent to check.
	 * @return <tt>true</tt> if the Intent can be handled, <tt>false</tt> if not
	 *         (i.e. because its Action is not an NFC Action).
	 */
	public boolean willHandleIntent(final Intent intent) {
		boolean retVal = false;

		// Check if it's an NFC intent that should be handled
		final String action = intent.getAction();
		if (action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
			retVal = true;
		}

		return retVal;
	}

	/**
	 * Parses user information from the given Intent.
	 * 
	 * @param intent
	 *            The Intent to handle.
	 * @return The UserInfo included in the NDEF message or <tt>null</tt> if the
	 *         Intent could not be parsed.
	 */
	public UserInfo handleIntent(final Intent intent) {
		UserInfo retVal = null;

		// Get the NDEF messages from the intent
		NdefMessage[] msgs = new NdefMessage[0];
		Parcelable[] rawMsgs = intent
				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		if (rawMsgs != null) {
			msgs = new NdefMessage[rawMsgs.length];
			for (int i = 0; i < rawMsgs.length; i++) {
				msgs[i] = (NdefMessage) rawMsgs[i];
			}

			// Process the first message
			if (1 == msgs.length) {
				final NdefRecord[] records = msgs[0].getRecords();

				// There should be 2 records
				if (2 == records.length) {

					// Get the second record
					final NdefRecord secondRecord = records[1];

					// Validate the record
					if (checkNdefRecord(secondRecord)) {
						// Display a Toast with the received string
						final String username = new String(
								secondRecord.getPayload(),
								NFCManager.NDEF_CHARSET);
						Toast.makeText(
								m_activity,
								"Received a string via Android Beam:\n"
										+ username, Toast.LENGTH_LONG).show();

						Log.d(TAG,
								"Received NDEF message with payload string: "
										+ username);

						// Build the UserInfo object to return
						retVal = new UserInfo(username, null);

					} else {
						Log.d(TAG, "Received invalid NDEF message");
					}

				} else {
					Log.d(TAG, "Received " + records.length
							+ " NDEF record(s), expecting exactly 2");
				}

			} else {
				Log.d(TAG, "Received " + msgs.length
						+ " NDEF messages, expecting exactly 1");
			}

		} else {
			Log.d(TAG, "Received NFC intent with no contents.");
		}

		return retVal;
	}

	/**
	 * Checks if an NdefRecord is the type expected for data exchange.
	 * 
	 * @param record
	 *            The record to check.
	 * @return <tt>true</tt> if the record is the type expected, <tt>false</tt>
	 *         if not.
	 */
	private boolean checkNdefRecord(final NdefRecord record) {
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
