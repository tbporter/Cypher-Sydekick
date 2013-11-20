package com.github.tbporter.cypher_sydekick;

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

public class NFCManager implements CreateNdefMessageCallback {
	private static String TAG = "NFCManager";

	/** MIME type for messages shared over NFC. */
	public static String NDEF_MIME_TYPE = "application/com.github.tbporter.cypher_sydekick";
	/** Charset used for Strings shared over NFC. */
	public static Charset NDEF_CHARSET = Charset.forName("US-ASCII");

	/** Device's NFC adapter */
	private final NfcAdapter m_nfcAdapter;

	/** Activity which this manager is tied to. */
	private final Activity m_callingActivity;

	/** User whose information will be shared over NFC. */
	private UserInfo m_activeUser;

	/**
	 * Creates a new NFCManager which will be associated with the given
	 * Activity. The Activity is used to get Application Context.
	 * 
	 * @param callingActivity
	 *            activity with which this NFCManager will be associated.
	 * @throws NFCManagerException
	 *             if NFC is not available on this device.
	 */
	public NFCManager(final Activity callingActivity)
			throws NFCManagerException {
		m_callingActivity = callingActivity;

		m_nfcAdapter = NfcAdapter.getDefaultAdapter(m_callingActivity
				.getApplicationContext());

		// Make sure this device has NFC available
		if (!isNfcAvailable()) {
			throw new NFCManagerException("NFC not available on this device.");
		}

		// Set this class as the callback to create an NDEF push message
		m_nfcAdapter.setNdefPushMessageCallback(this, callingActivity);
	}

	/**
	 * Sets the user whose information will be shared when Android Beam is
	 * triggered.
	 * 
	 * @param user
	 *            UserInfo for the active user.
	 */
	public void setActiveUser(UserInfo user) {
		m_activeUser = user;
	}

	/**
	 * Checks if the device has NFC available.
	 * 
	 * @return <tt>true</tt> if NFC is available, <tt>false</tt> if not.
	 */
	private boolean isNfcAvailable() {
		return (null != m_nfcAdapter);
	}

	/**
	 * Starts the foreground dispatch which enables the calling Activity to
	 * receive NFC messages.
	 */
	public void startNFCReceive() {
		Log.d(TAG, "NFC receive starting");

		checkAndNotifyNFCEnabled();

		// Create the PendingIntent for NFC read results
		PendingIntent nfcPendingIntent = PendingIntent.getActivity(
				m_callingActivity, 0, new Intent(m_callingActivity,
						m_callingActivity.getClass())
						.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		// Enable a foreground dispatch for all tag types
		m_nfcAdapter.enableForegroundDispatch(m_callingActivity,
				nfcPendingIntent, null, null);
	}

	/**
	 * Stops the foreground dispatch which enables the calling Activity to
	 * receive NFC messages.
	 */
	public void stopNFCReceive() {
		Log.d(TAG, "NFC receive stopping");

		// Disable NFC foreground dispatch
		m_nfcAdapter.disableForegroundDispatch(m_callingActivity);
	}

	/**
	 * Checks if this manager will process the given Intent. The action is
	 * checked to verify it is one known to this manager.
	 * 
	 * @param intent
	 *            Intent to check
	 * @return <tt>true</tt> if this manager will handle the intent,
	 *         <tt>false</tt> if not.
	 */
	public boolean willHandleIntent(final Intent intent) {
		final String action = intent.getAction();
		return (action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED));
	}

	/**
	 * Parses the NDEF message from the given Intent. The Intent should be
	 * checked first with {@link #willHandleIntent(Intent)} to ensure this
	 * manager will parse it.
	 * 
	 * @param intent
	 *            Intent to parse
	 * @return UserInfo parsed from the Intent, or <tt>null</tt> if parsing
	 *         fails.
	 */
	public UserInfo parseIntent(final Intent intent) {
		UserInfo retVal = null;
		String username;
		String publicKey;

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
					if (NFCManager.checkNdefRecord(secondRecord)) {
						// Set the EditText contents based on the received
						// String
						username = new String(secondRecord.getPayload(),
								NFCManager.NDEF_CHARSET);
						Toast.makeText(m_callingActivity,
								"Received a string via Android Beam",
								Toast.LENGTH_LONG).show();

						Log.d(TAG,
								"Received NDEF message with payload string: "
										+ username);

						retVal = new UserInfo(username, "no key");

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
	private static boolean checkNdefRecord(final NdefRecord record) {
		boolean retVal = true;

		// Requires API 16:
		// Check the MIME type
		final String mime = record.toMimeType();
		if (null != mime) {
			// The record has a MIME type, make sure it's correct
			if (!NFCManager.NDEF_MIME_TYPE.equals(mime)) {
				retVal = false;
				Log.d(TAG, "Checked NdefRecord with unknown MIME type: " + mime);
			}
		} else {
			// No MIME type found, this is not a valid record
			retVal = false;
			Log.d(TAG, "Checked NdefRecord with null MIME type");
		}

		return retVal;
	}

	/**
	 * Checks if NFC is enabled and notifies the user if it is not.
	 */
	public void checkAndNotifyNFCEnabled() {
		if (!m_nfcAdapter.isEnabled()) {
			Toast.makeText(m_callingActivity,
					"NFC disabled, must enable before Android Beam will work.",
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Creates an NDEF message with the active user's information.
	 */
	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		Log.d(TAG, "Building NDEF message");

		// Create an NDEF Android Application Record so that this app will open
		// when the message is received.
		final NdefRecord appRecord = NdefRecord
				.createApplicationRecord(m_callingActivity
						.getApplicationContext().getPackageName());

		// Create an NDEF record with the given UserInfo
		final String text = m_activeUser.getUsername();
		final NdefRecord stringRecord = NdefRecord.createMime(NDEF_MIME_TYPE,
				text.getBytes(NDEF_CHARSET));

		// Build an NDEF message with the records created above
		final NdefMessage msg = new NdefMessage(new NdefRecord[] { appRecord,
				stringRecord });

		// Return the NDEF message to broadcast
		return msg;
	}
}
