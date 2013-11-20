package com.github.tbporter.cypher_sydekick;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.Toast;

public class NFCActivity extends Activity {
	private static String TAG = "NFCActivity";

	/** NFCManager to handle NFC transactions. */
	private NFCManager m_nfcManager;

	// Views
	private EditText m_nfcEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nfc);
		
		Log.d(TAG, "onCreate()");

		// Create the NFCManager to handle NFC transactions
		try {
			m_nfcManager = new NFCManager(this);
		} catch (NFCManagerException nme) {
			Toast.makeText(this, nme.getMessage(), Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		// Notify the user if they have not enabled NFC
		m_nfcManager.checkAndNotifyNFCEnabled();

		// Get views by id
		m_nfcEditText = (EditText) findViewById(R.id.nfcEditText);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause()");

		// Stop any active NFC operations
		m_nfcManager.stopNFCReceive();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume()");

		// Start listening for NFC
		m_nfcManager.startNFCReceive();
	}

	/**
	 * Processes intent for a completed NFC read.
	 */
	@Override
	public void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);

		// Make sure it's an NFC intent that should be handled
		if (m_nfcManager.willHandleIntent(intent)) {
			UserInfo newUser = m_nfcManager.parseIntent(intent);
			Log.d(TAG,
					"Received new user information: " + newUser.getUsername()
							+ ", " + newUser.getPublicKey());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nfc, menu);
		return true;
	}

}
