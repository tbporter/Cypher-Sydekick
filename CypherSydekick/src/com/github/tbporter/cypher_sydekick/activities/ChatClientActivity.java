package com.github.tbporter.cypher_sydekick.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Base64;
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
import com.github.tbporter.cypher_sydekick.chat.ChatTask;
import com.github.tbporter.cypher_sydekick.chat.ConversationAdapter;
import com.github.tbporter.cypher_sydekick.chat.ConversationItem;
import com.github.tbporter.cypher_sydekick.crypt.Crypt;
import com.github.tbporter.cypher_sydekick.database.UserKeyDOA;
import com.github.tbporter.cypher_sydekick.nfc.NFCManager;
import com.github.tbporter.cypher_sydekick.nfc.NFCManagerException;
import com.github.tbporter.cypher_sydekick.users.UserInfo;

public class ChatClientActivity extends Activity {
	static final String USERNAME_FILE = "cypher-sidekick-username";
	private String username_ = "";
	private String pubKeyString_ = "";

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private ArrayList<String> mFriendsArray = new ArrayList<String>();
	private ArrayAdapter<String> mDrawerAdapter;

	private ChatFragment chatFragment_ = new ChatFragment();

	private UserKeyDOA userKeyDatabase_;

	/** NFCManager to handle NFC operations. */
	private NFCManager m_nfcManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_client);

		// Check to show the loginDialog or not
		File usernameFile = getFileStreamPath(USERNAME_FILE);
		if (usernameFile.exists()) {
			byte[] usernameBytes = new byte[(int) usernameFile.length()];
			try {
				FileInputStream in = openFileInput(USERNAME_FILE);
				in.read(usernameBytes);
				Crypt.init(ChatClientActivity.this.getBaseContext());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			username_ = new String(Base64.decode(usernameBytes, Base64.DEFAULT));
			pubKeyString_ = Base64.encodeToString(Crypt.getPublicKey(),
					Base64.DEFAULT);

			Toast.makeText(this, "Hello " + username_, Toast.LENGTH_LONG)
					.show();
		} else {
			showLoginDialog();
		}

		// TODO: init database
		userKeyDatabase_ = new UserKeyDOA(this);
		userKeyDatabase_.open();

		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.content_frame, chatFragment_).commit();

		mTitle = getResources().getString(R.string.chatclient_title);
		mDrawerTitle = getResources().getString(R.string.drawer_title);
		/*
		 * mFriendsArray = new String[] { "user1", "user2", "user3", "user4",
		 * "user5", "user6", "user7", "user8", "user9", "user10", "user11",
		 * "user12", "user13", "user14" };
		 */
		mFriendsArray = userKeyDatabase_.getAllUsers();

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		// set up the drawer's list view with items and click listener
		mDrawerAdapter = new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, mFriendsArray);
		mDrawerList.setAdapter(mDrawerAdapter);
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
		m_nfcManager.setCurrentUser(new UserInfo(username_, pubKeyString_));

		// Notify the user if they have not enabled NFC
		checkAndNotifyNFCEnabled();

		/*
		 * if (savedInstanceState == null) { selectItem(0); }
		 */

		// Drawer is initially open
		mDrawerLayout.openDrawer(mDrawerList);
	}

	// Method that shows the login dialog
	private void showLoginDialog() {
		final EditText usernameInput = new EditText(this);
		usernameInput.setHint("Username");
		final AlertDialog.Builder newUserAlert = new AlertDialog.Builder(this);
		newUserAlert.setTitle("Create a new username");
		newUserAlert.setView(usernameInput);
		newUserAlert.setPositiveButton("Create",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// TODO Write code for what to do after create is
						// pressed
						Runnable runnable = new Runnable() {

							@Override
							public void run() {
								try {
									Crypt.init(ChatClientActivity.this
											.getBaseContext());

									username_ = usernameInput.getText()
											.toString();

									// Create username file with the username
									FileOutputStream out = openFileOutput(
											USERNAME_FILE, Context.MODE_PRIVATE);
									out.write(Base64.encode(
											username_.getBytes(),
											Base64.DEFAULT));

									out.close();

									new ChatTask().execute(username_);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

								byte[] pubKeyBytes = Crypt.getPublicKey();
								pubKeyString_ = Base64.encodeToString(
										pubKeyBytes, Base64.DEFAULT);
								// Set the active user whose info should be
								// shared over NFC
								m_nfcManager.setCurrentUser(new UserInfo(
										username_, pubKeyString_));

								userKeyDatabase_.deleteAllUsers();
								mFriendsArray.clear();
							}
						};
						new Thread(runnable).start();
					}
				});
		final AlertDialog newUserDialog = newUserAlert.create();
		newUserDialog.show();
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

			// Add the new user to the database
			userKeyDatabase_.createUser(receivedUser.getUsername(),
					receivedUser.getPublicKey());

			// Add the new user to the drawer
			if (!mFriendsArray.contains(receivedUser.getUsername())) {
				mFriendsArray.add(receivedUser.getUsername());
				mDrawerAdapter.notifyDataSetChanged();
			}

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
		case R.id.action_newuser:
			showLoginDialog();
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
		String newUser = mDrawerList.getItemAtPosition(position).toString();
		setTitle(newUser);

		// Here is where a new user is selected to chat with
		chatFragment_.setRecipient(newUser,
				userKeyDatabase_.getKeyViaUsername(newUser));
		chatFragment_.setMyUsername(username_);
		chatFragment_.setContext(getBaseContext());
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
		// Pass any configuration change to the drawer toggles
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	/** Fragment for the Chat **/
	public static class ChatFragment extends Fragment {
		private ListView conversationListView_;
		ConversationAdapter convAdapter_;
		private ArrayList<ConversationItem> conversationItems_ = new ArrayList<ConversationItem>();
		private ImageButton sendButton_;
		private EditText messageField_;

		private String myUsername_;
		private String recipientUsername_, recipientPubKey_;
		private Context context_;

		public ChatFragment() {
			// Empty constructor required for fragment subclasses
		}

		public void setContext(Context c) {
			context_ = c;
		}

		public void setMyUsername(String username) {
			myUsername_ = username;
		}

		public void setRecipient(String username, String key) {
			recipientUsername_ = username;
			recipientPubKey_ = key;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_chat, container,
					false);

			myUsername_ = "";
			recipientUsername_ = "";

			// Setup the adapter for the conversation list view
			conversationListView_ = (ListView) rootView
					.findViewById(R.id.listView_conversation);
			convAdapter_ = new ConversationAdapter(getActivity(),
					conversationItems_);
			conversationListView_.setAdapter(convAdapter_);

			sendButton_ = (ImageButton) rootView
					.findViewById(R.id.btn_sendMessage);
			messageField_ = (EditText) rootView
					.findViewById(R.id.editText_message);

			sendButton_.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					ConversationItem newItem = new ConversationItem();
					newItem.setMessage(messageField_.getText().toString());
					newItem.setSubtitle("Sent from " + myUsername_);
					newItem.setIcon(R.drawable.ic_action_person);
					conversationItems_.add(newItem);
					// TODO Here is where we should fire the AsyncTaskto send
					// the message
					new ChatTask().execute("send-message", myUsername_,
							recipientUsername_, recipientPubKey_, messageField_
									.getText().toString());

					messageField_.setText("");
				}
			});
			callAsynchronousTask();

			return rootView;
		}

		public void callAsynchronousTask() {
			final Handler handler = new Handler();
			Timer timer = new Timer();
			TimerTask doAsynchronousTask = new TimerTask() {
				@Override
				public void run() {
					handler.post(new Runnable() {
						public void run() {
							try {
								// PerformBackgroundTask this class is the class
								// that extends AsynchTask
								// Toast.makeText(getActivity(),
								// "Checked for new Message",
								// Toast.LENGTH_SHORT).show();
								new ChatTask(convAdapter_, conversationItems_,
										context_).execute("receive-message",
										myUsername_, recipientUsername_);
							} catch (Exception e) {
								// TODO Auto-generated catch block
							}
						}
					});
				}
			};
			timer.schedule(doAsynchronousTask, 0, 3000); // execute in every
															// 1000 ms
		}

	}

}
