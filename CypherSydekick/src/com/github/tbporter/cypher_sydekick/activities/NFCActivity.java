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

public class NFCActivity extends Activity {
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
			Toast.makeText(this, nme.getMessage(), Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		// Notify the user if they have not enabled NFC
		checkAndNotifyNFCEnabled();

		// Get views by id
		m_nfcEditText = (EditText) findViewById(R.id.nfcEditText);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Stop listening for NFC messages
		m_nfcManager.stopNFCReceive();
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Start listening for NFC messages
		checkAndNotifyNFCEnabled();
		m_nfcManager.startNFCReceive();
	}

	/**
	 * Checks if NFC is enabled and notifies the user if it is not.
	 */
	private void checkAndNotifyNFCEnabled() {
		if (!m_nfcManager.isNFCEnabled()) {
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

		// Pass the intent to the NFCManager and see if it handles it
		if (m_nfcManager.willHandleIntent(intent)) {
			final String receivedStr = m_nfcManager.handleIntent(intent);
			Toast.makeText(this,
					"NFCActivity received string:\n" + receivedStr,
					Toast.LENGTH_LONG).show();
		}
		// If the NFCManager doesn't handle this intent, do nothing with it
		else {
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nfc, menu);
		return true;
	}

}
