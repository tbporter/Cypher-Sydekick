package com.github.tbporter.cypher_sydekick;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NFCActivity extends Activity {
	private EditText m_nfcEditText;
	private Button m_nfcBroadcastButton;
	private Button m_nfcReceiveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        
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
    	Toast.makeText(this, "Broadcast button clicked, text:\n" + m_nfcEditText.getText(), Toast.LENGTH_SHORT).show();
    }
    
    private void receiveButtonClicked() {
    	Toast.makeText(this, "Receive button clicked", Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nfc, menu);
        return true;
    }
    
}
