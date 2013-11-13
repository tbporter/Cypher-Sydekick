package com.github.tbporter.cypher_sydekick;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NFCActivity extends Activity {
	private static String TAG = "NFCActivity";

	// Views
	private EditText m_nfcEditText;
	private Button m_nfcBroadcastButton;
	private Button m_nfcReceiveButton;

	// Strings
	private String m_nfcBroadcastButton_text_initial;
	private String m_nfcBroadcastButton_text_active;

	// States for the NFC broadcast/receive (mutually exclusive)
	private enum NFCState {
		NFC_INACTIVE, NFC_BROADCASTING, NFC_RECEIVING
	}

	private NFCState m_nfcState = NFCState.NFC_INACTIVE;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nfc);

		// Initialize members
		m_nfcState = NFCState.NFC_INACTIVE;

		// Get views by id
		m_nfcEditText = (EditText) findViewById(R.id.nfcEditText);
		m_nfcBroadcastButton = (Button) findViewById(R.id.nfcBroadcastButton);
		m_nfcReceiveButton = (Button) findViewById(R.id.nfcReceiveButton);

		// Get strings by id
		m_nfcBroadcastButton_text_initial = getString(R.string.nfcBroadcastButton_text_initial);
		m_nfcBroadcastButton_text_active = getString(R.string.nfcBroadcastButton_text_active);

		// Create listener for broadcast button
		m_nfcBroadcastButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				broadcastButtonClicked();
			}
		});

		// Create listener for receive button
		m_nfcReceiveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				receiveButtonClicked();
			}
		});
	}

	private void broadcastButtonClicked() {
		Toast.makeText(this,
				"Broadcast button clicked, text:\n" + m_nfcEditText.getText(),
				Toast.LENGTH_SHORT).show();
		Log.d(TAG, "Broadcast button clicked, text: " + m_nfcEditText.getText());

		// Check the NFC state to determine what to do
		switch (m_nfcState) {
		
		// If not broadcasting or receiving, start the broadcast
		case NFC_INACTIVE:
			startNFCBroadcast();
			break;

		// If already broadcasting, stop the broadcast
		case NFC_BROADCASTING:
			stopNFCBroadcast();
			break;

		// Any other state is an error
		case NFC_RECEIVING:
		default:
			Log.e(TAG, "Broadcast button clicked with invalid NFC state: "
					+ m_nfcState);
			
		} // end switch (m_nfcState)
	}

	private void receiveButtonClicked() {
		Toast.makeText(this, "Receive button clicked", Toast.LENGTH_SHORT)
				.show();
		Log.d(TAG, "Receive button clicked");
	}

	private void startNFCBroadcast() {
		Toast.makeText(this, "NFC broadcast started", Toast.LENGTH_SHORT)
				.show();
		Log.d(TAG, "NFC broadcast started");

		// TODO: Start the broadcast
		
		// Update the NFC state
		m_nfcState = NFCState.NFC_BROADCASTING;

		// Disable the text field and receive button while broadcast is in
		// progress
		m_nfcEditText.setEnabled(false);
		m_nfcReceiveButton.setEnabled(false);

		// Update broadcast button text to indicate its new function
		m_nfcBroadcastButton.setText(m_nfcBroadcastButton_text_active);
	}

	private void stopNFCBroadcast() {
		Toast.makeText(this, "NFC broadcast stopped", Toast.LENGTH_SHORT)
				.show();
		Log.d(TAG, "NFC broadcast stopped");

		// TODO: Stop the broadcast
		
		// Update the NFC state
		m_nfcState = NFCState.NFC_INACTIVE;

		// Enable the text field and receive button when broadcast is stopped
		m_nfcEditText.setEnabled(true);
		m_nfcReceiveButton.setEnabled(true);

		// Update broadcast button text to indicate its new function
		m_nfcBroadcastButton.setText(m_nfcBroadcastButton_text_initial);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nfc, menu);
		return true;
	}

}
