package com.github.tbporter.cypher_sydekick;

import java.nio.charset.Charset;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NFCActivity extends Activity implements CreateNdefMessageCallback {
	private static String TAG = "NFCActivity";

	/** MIME type for messages shared over NFC. */
	private static String NDEF_MIME_TYPE = "application/com.github.tbporter.cypher_sydekick";
	/** Charset used for Strings shared over NFC. */
	private static Charset NDEF_CHARSET = Charset.forName("US-ASCII");

	/** Device's NFC adapter */
	private NfcAdapter m_nfcAdapter;

	// Views
	private EditText m_nfcEditText;
	private Button m_nfcReceiveButton;

	/** States for the NFC broadcast/receive (mutually exclusive) */
	private enum NFCState {
		NFC_INACTIVE, NFC_BROADCASTING, NFC_RECEIVING
	}

	/** Current state for NFC broadcast/receive. */
	private NFCState m_nfcState = NFCState.NFC_INACTIVE;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nfc);

		// Make sure this device has NFC available
		m_nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (null == m_nfcAdapter) {
			Toast.makeText(this, "NFC is not available on this device",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		// Notify the user if they have not enabled NFC
		checkAndNotifyNFCEnabled();

		// Set this class as the callback to create an NDEF push message
		m_nfcAdapter.setNdefPushMessageCallback(this, this);

		// Initialize members
		m_nfcState = NFCState.NFC_INACTIVE;

		// Get views by id
		m_nfcEditText = (EditText) findViewById(R.id.nfcEditText);
		m_nfcReceiveButton = (Button) findViewById(R.id.nfcReceiveButton);

		// Create listener for receive button
		m_nfcReceiveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				receiveButtonClicked();
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Stop any active NFC operations
		stopNFCReceive();
	}

	private void receiveButtonClicked() {
		Log.d(TAG, "Receive button clicked");

		// Check the NFC state to determine what to do
		switch (m_nfcState) {

		// If not broadcasting or receiving, start receiving
		case NFC_INACTIVE:
			startNFCReceive();
			break;

		// If already receiving, stop receiving
		case NFC_RECEIVING:
			stopNFCReceive();
			break;

		// Any other state is an error
		case NFC_BROADCASTING:
		default:
			Log.e(TAG, "Receive button clicked with invalid NFC state: "
					+ m_nfcState);

		} // end switch (m_nfcState)
	}

	private void startNFCReceive() {
		Log.d(TAG, "NFC receive starting");

		checkAndNotifyNFCEnabled();

		// Create the PendingIntent for NFC read results
		PendingIntent nfcPendingIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, getClass())
						.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		// Enable a foreground dispatch for all tag types
		m_nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, null,
				null);

		// Update the NFC state
		m_nfcState = NFCState.NFC_RECEIVING;

		// Disable the text field when receiving
		m_nfcEditText.setEnabled(false);

		// Update receive button text to indicate its new function
		m_nfcReceiveButton.setText(R.string.nfcReceiveButton_text_active);
	}

	private void stopNFCReceive() {
		Log.d(TAG, "NFC receive stopping");

		// Disable NFC foreground dispatch
		m_nfcAdapter.disableForegroundDispatch(this);

		// Update the NFC state
		m_nfcState = NFCState.NFC_INACTIVE;

		// Enable the text field when receiving stops
		m_nfcEditText.setEnabled(true);

		// Update receive button text to indicate its new function
		m_nfcReceiveButton.setText(R.string.nfcReceiveButton_text_initial);
	}

	/**
	 * Checks if NFC is enabled and notifies the user if it is not.
	 */
	private void checkAndNotifyNFCEnabled() {
		if (!m_nfcAdapter.isEnabled()) {
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
					// Get the first record (every NDEF message has at least one
					// record)
					NdefRecord firstRecord = msgs[0].getRecords()[0];

					// Validate the record
					if (checkNdefRecord(firstRecord)) {
						// Set the EditText contents based on the received
						// String
						final String payloadStr = new String(
								firstRecord.getPayload(), NDEF_CHARSET);
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
			if (!NDEF_MIME_TYPE.equals(mime)) {
				retVal = false;
				Log.d(TAG, "Checked NdefRecord with unknown MIME type: " + mime);
				Log.d(TAG,
						"MIME type via old method: "
								+ new String(record.getType(), NDEF_CHARSET));
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

		// Build the NDEF message to broadcast
		String text = m_nfcEditText.getText().toString();
		NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
				NDEF_MIME_TYPE.getBytes(NDEF_CHARSET), new byte[0],
				text.getBytes(NDEF_CHARSET));
		NdefMessage msg = new NdefMessage(new NdefRecord[] { record });

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