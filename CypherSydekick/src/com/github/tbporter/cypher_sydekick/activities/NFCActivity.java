package com.github.tbporter.cypher_sydekick.activities;

import java.nio.charset.Charset;

import com.github.tbporter.cypher_sydekick.R;
import com.github.tbporter.cypher_sydekick.nfc.NFCManager;
import com.github.tbporter.cypher_sydekick.nfc.NFCManagerException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.Toast;

public class NFCActivity extends Activity implements CreateNdefMessageCallback {
	private static String TAG = "NFCActivity";

	/** NFCManager to handle NFC operations. */
	NFCManager m_nfcManager;

	// Views
	private EditText m_nfcEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nfc);

		// Create the NFCManager
		try {
			m_nfcManager = new NFCManager(NfcAdapter.getDefaultAdapter(this),
					this);
		} catch (NFCManagerException nme) {
			Toast.makeText(this, nme.getMessage(),
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		// Notify the user if they have not enabled NFC
		checkAndNotifyNFCEnabled();

		// Set this class as the callback to create an NDEF push message
		m_nfcManager.getNfcAdapter().setNdefPushMessageCallback(this, this);

		// Get views by id
		m_nfcEditText = (EditText) findViewById(R.id.nfcEditText);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Stop any active NFC operations
		stopNFCReceive();
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Start listening for NFC
		startNFCReceive();
	}

	private void startNFCReceive() {
		Log.d(TAG, "NFC receive starting");

		checkAndNotifyNFCEnabled();

		// Create the PendingIntent for NFC read results
		PendingIntent nfcPendingIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, getClass())
						.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		// Enable a foreground dispatch for all tag types
		m_nfcManager.getNfcAdapter().enableForegroundDispatch(this,
				nfcPendingIntent, null, null);
	}

	private void stopNFCReceive() {
		Log.d(TAG, "NFC receive stopping");

		// Disable NFC foreground dispatch
		m_nfcManager.getNfcAdapter().disableForegroundDispatch(this);
	}

	/**
	 * Checks if NFC is enabled and notifies the user if it is not.
	 */
	private void checkAndNotifyNFCEnabled() {
		if (!m_nfcManager.getNfcAdapter().isEnabled()) {
			Toast.makeText(this,
					"NFC disabled, must enable before Android Beam will work.",
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Processes intent for a completed NFC read.
	 */
	@Override
	public void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);

		// Make sure it's an NFC intent that should be handled
		final String action = intent.getAction();
		if (action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {

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
							// Set the EditText contents based on the received
							// String
							final String payloadStr = new String(
									secondRecord.getPayload(),
									NFCManager.NDEF_CHARSET);
							m_nfcEditText.setText(payloadStr);
							Toast.makeText(this,
									"Received a string via Android Beam",
									Toast.LENGTH_LONG).show();

							Log.d(TAG,
									"Received NDEF message with payload string: "
											+ payloadStr);

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

		}

	}

	/**
	 * Checks if an NdefRecord is the type expected for data exchange.
	 * 
	 * @param record
	 *            The record to check.
	 * @return true if the record is the type expected, false if not.
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
		NdefRecord appRecord = NdefRecord
				.createApplicationRecord(getApplicationContext()
						.getPackageName());

		// Create an NDEF record with the string from the EditText
		String text = m_nfcEditText.getText().toString();
		NdefRecord stringRecord = NdefRecord.createMime(
				NFCManager.NDEF_MIME_TYPE,
				text.getBytes(NFCManager.NDEF_CHARSET));

		// Build an NDEF message with the records created above
		NdefMessage msg = new NdefMessage(new NdefRecord[] { appRecord,
				stringRecord });

		// Return the NDEF message to broadcast
		return msg;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nfc, menu);
		return true;
	}

}
