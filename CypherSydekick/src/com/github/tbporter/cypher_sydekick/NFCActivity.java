package com.github.tbporter.cypher_sydekick;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;

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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nfc, menu);
        return true;
    }
    
}
