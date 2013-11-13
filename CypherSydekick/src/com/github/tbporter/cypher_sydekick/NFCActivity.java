package com.github.tbporter.cypher_sydekick;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class NFCActivity extends Activity {
	private static String TAG = "NFCActivity";

	// Views
	private EditText m_nfcEditText;
	private Button m_nfcBroadcastButton;
	private Button m_nfcReceiveButton;

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

	private void startNFCBroadcast() {
		Log.d(TAG, "NFC broadcast starting");

		// TODO: Start the broadcast

		// Update the NFC state
		m_nfcState = NFCState.NFC_BROADCASTING;

		// Disable the text field and receive button while broadcast is in
		// progress
		m_nfcEditText.setEnabled(false);
		m_nfcReceiveButton.setEnabled(false);

		// Update broadcast button text to indicate its new function
		m_nfcBroadcastButton.setText(R.string.nfcBroadcastButton_text_active);
	}

	private void stopNFCBroadcast() {
		Log.d(TAG, "NFC broadcast stopping");

		// TODO: Stop the broadcast

		// Update the NFC state
		m_nfcState = NFCState.NFC_INACTIVE;

		// Enable the text field and receive button when broadcast is stopped
		m_nfcEditText.setEnabled(true);
		m_nfcReceiveButton.setEnabled(true);

		// Update broadcast button text to indicate its new function
		m_nfcBroadcastButton.setText(R.string.nfcBroadcastButton_text_initial);
	}
	
	private void startNFCReceive() {
		Log.d(TAG, "NFC receive starting");
		
		// TODO: Start receiving
		
		// Update the NFC state
		m_nfcState = NFCState.NFC_RECEIVING;
		
		// Disable the text field and broadcast button when receiving
		m_nfcEditText.setEnabled(false);
		m_nfcBroadcastButton.setEnabled(false);
		
		// Update receive button text to indicate its new function
		m_nfcReceiveButton.setText(R.string.nfcReceiveButton_text_active);
	}
	
	private void stopNFCReceive() {
		Log.d(TAG, "NFC receive stopping");
		
		// TODO: Stop receiving
		
		// Update the NFC state
		m_nfcState = NFCState.NFC_INACTIVE;
		
		// Enable the text field and broadcast button when receiving stops
		m_nfcEditText.setEnabled(true);
		m_nfcBroadcastButton.setEnabled(true);
		
		// Update receive button text to indicate its new function
		m_nfcReceiveButton.setText(R.string.nfcReceiveButton_text_initial);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nfc, menu);
		return true;
	}

}
