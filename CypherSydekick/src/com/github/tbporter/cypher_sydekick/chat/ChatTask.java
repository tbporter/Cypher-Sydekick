package com.github.tbporter.cypher_sydekick.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

import com.github.tbporter.cypher_sydekick.crypt.Crypt;

import android.os.AsyncTask;

public class ChatTask extends AsyncTask<String, Void, String> {
	static final String SERVER_URL_DEFAULT = "http://ayelix.dnsdynamic.com/";
	private ArrayList<ConversationItem> activityConversationItems_;
	private String sender_;
	private String recipient_;
	
	
	// Might have to add an instance of the Crypt class if we want to do encryption and decription in this class
	public ChatTask(){
		
	}
	
	// Only need to make one instance of this class since the conversation items list array ref remains the same
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
		String url;
		try {
			if(params[0].equals("send-message")){	// send
				String message;
				// Encryption is done here
				byte[] encryptedBytes = Crypt.encrypt(params[4].getBytes(), params[3].getBytes());
				message = new String(encryptedBytes, Charset.forName("US-ASCII"));
				url = SERVER_URL_DEFAULT + "messages?action=send&sender="+params[1] + "&recipient="+params[2] + "&message="+message;
				HttpURLConnection connection = (HttpURLConnection)(new URL(url).openConnection());
	            connection.setRequestMethod("GET");
	            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.114 Safari/537.36");
	    		connection.connect();
	    		String content = getContents(connection);
	    		connection.disconnect();
			}
			else if(params[0].equals("receive-message")){	// receive
				
			}
			else{	// add user
				url = SERVER_URL_DEFAULT + "users?username=" + params[0];
				HttpURLConnection connection = (HttpURLConnection)(new URL(url).openConnection());
	            connection.setRequestMethod("GET");
	            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.114 Safari/537.36");
	    		connection.connect();
	    		String content = getContents(connection);
	    		connection.disconnect();
			}
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		        e.printStackTrace();
		}

		return null;
	}

	@Override
	protected void onPostExecute(String result) {

	}
	
	private String getContents(HttpURLConnection connection) throws IOException {
        BufferedReader inReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        StringBuilder htmlStringBuilder = new StringBuilder();
        String htmlBuffer;
        while((htmlBuffer = inReader.readLine()) != null){
            htmlStringBuilder.append(htmlBuffer);
        }

        return htmlStringBuilder.toString();
    }
}
