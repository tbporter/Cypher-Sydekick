package com.github.tbporter.cypher_sydekick.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.github.tbporter.cypher_sydekick.R;
import com.github.tbporter.cypher_sydekick.nfc.NFCManager;
import com.github.tbporter.cypher_sydekick.nfc.NFCManagerException;
import com.github.tbporter.cypher_sydekick.users.UserInfo;
import com.github.tbporter.cypher_sydekick.chat.*;

public class ChatClientActivity extends Activity {

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] mFriendsArray;

	private ChatFragment chatFragment_ = new ChatFragment();

	/** NFCManager to handle NFC operations. */
	private NFCManager m_nfcManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_client);

		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.content_frame, chatFragment_).commit();

		mTitle = getResources().getString(R.string.chatclient_title);
		mDrawerTitle = getResources().getString(R.string.drawer_title);
		mFriendsArray = new String[] { "user1", "user2", "user3", "user4",
				"user5", "user6", "user7", "user8", "user9", "user10",
				"user11", "user12", "user13", "user14" };
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		// set up the drawer's list view with items and click listener
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, mFriendsArray));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

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
		// TODO: Send real user information
		m_nfcManager.setCurrentUser(new UserInfo("username", "key"));

		// Notify the user if they have not enabled NFC
		checkAndNotifyNFCEnabled();

		/*
		 * if (savedInstanceState == null) { selectItem(0); }
		 */
		
		// Drawer is initially open
		mDrawerLayout.openDrawer(mDrawerList);
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
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.chat_client, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		// menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_add_person:
			// Show a dialog explaining how to add a friend
			final AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Adding new friend");
			alert.setMessage("Bump phones together and press Android Beam button to share public key pairs and add friend.");
			alert.setNegativeButton("Dismiss", null);
			final AlertDialog helpDialog = alert.create();
			helpDialog.show();

			return true;
		case R.id.action_game:
			// Open the game activity
			Intent gameIntent = new Intent(this, GameActivity.class);
			startActivity(gameIntent);

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* The click listener for ListView in the navigation drawer */
	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}

	private void selectItem(int position) {
		mDrawerList.setItemChecked(position, true);
		mDrawerLayout.closeDrawer(mDrawerList);
		setTitle(mDrawerList.getItemAtPosition(position).toString());
		// TODO here is where a new user is selected to chat with
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	/** Fragment for the Chat **/
	public static class ChatFragment extends Fragment {
		private ListView conversationListView_;
		private ArrayList<ConversationItem> conversationItems_ = new ArrayList<ConversationItem>();
		private ImageButton sendButton_;
		private EditText messageField_;
		
		
		public ChatFragment() {
			// Empty constructor required for fragment subclasses
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_chat, container,
					false);
			
			// Setup the adapter for the conversation list view			
			conversationListView_ = (ListView) rootView.findViewById(R.id.listView_conversation);
			ConversationAdapter newAdapter = new ConversationAdapter(getActivity(), conversationItems_);
			conversationListView_.setAdapter(newAdapter);
			
			sendButton_ = (ImageButton) rootView.findViewById(R.id.btn_sendMessage);
			messageField_ = (EditText) rootView.findViewById(R.id.editText_message);

			sendButton_.setOnClickListener(new View.OnClickListener() {
	             public void onClick(View v) {
	            	ConversationItem newItem = new ConversationItem();
	     			newItem.setMessage(messageField_.getText().toString());
	     			newItem.setSubtitle("My Username");
	     			newItem.setIcon(R.drawable.ic_action_person);
	     			conversationItems_.add(newItem);
	     			
	     			// TODO Here is where we should fire the AsyncTaskto send the message
	     			messageField_.setText("");
	             }
	        });
			
			return rootView;
		}

	}

}
