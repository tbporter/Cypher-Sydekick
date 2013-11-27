package com.github.tbporter.cypher_sydekick.chat;

import java.util.ArrayList;

import android.os.AsyncTask;

public class ChatTask extends AsyncTask<String, Void, String> {

	private ArrayList<ConversationItem> activityConversationItems_;
	private String sender_;
	private String recipient_;
	
	// Only need to make one instance of this class since the conversation items list array ref remains the same
	// Might have to add an instance of the Crypt class if we want to do encryption and decription in this class
	public ChatTask(ArrayList<ConversationItem> convItems){
		activityConversationItems_ = convItems;
	}
	
	// The two following methods are called when chatting with a new user is initiated
	public void setSender(String sender){
		sender_ = sender;
	}
	
	public void setRecipient(String recipient){
		recipient_ = recipient;
	}
	
	@Override
	protected void onPreExecute() {

	}

	@Override
	protected String doInBackground(String... params) {
		// TODO Auto-generated method stub
		
		if(params[0].equals("send")){	// send
			
		}
		else{	// receive
			
		}
		return null;
	}

	@Override
	protected void onPostExecute(String result) {

	}
}
