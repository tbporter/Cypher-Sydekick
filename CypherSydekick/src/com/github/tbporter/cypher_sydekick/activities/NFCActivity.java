package com.github.tbporter.cypher_sydekick.activities;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import com.github.tbporter.cypher_sydekick.R;
import com.github.tbporter.cypher_sydekick.nfc.NFCManager;
import com.github.tbporter.cypher_sydekick.nfc.NFCManagerException;
import com.github.tbporter.cypher_sydekick.users.UserInfo;

public class NFCActivity extends Activity {
	private static String TAG = "NFCActivity";

	/** NFCManager to handle NFC operations. */
	NFCManager m_nfcManager;

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
		
		// Set the active user whose info should be shared over NFC
		// TODO: Send a real user
		m_nfcManager.setCurrentUser(new UserInfo("username", "key"));

		// Notify the user if they have not enabled NFC
		checkAndNotifyNFCEnabled();
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
			// Parse the intent with the NFCManager
			final UserInfo receivedUser = m_nfcManager.handleIntent(intent);

			// Make sure the UserInfo was parsed successfully
			if (null != receivedUser) {
				Toast.makeText(
						this,
						"Received user information:\n"
								+ receivedUser.toString(), Toast.LENGTH_LONG)
						.show();
			} else {
				Toast.makeText(this, "Error receiving user information",
						Toast.LENGTH_LONG).show();
			}

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
