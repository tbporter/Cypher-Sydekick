package com.github.tbporter.cypher_sydekick.activities;

import android.os.Bundle;
import android.os.Debug;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.bls220.cyphersidekick.MySidekick;

public class GameActivity extends AndroidApplication {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
		cfg.useGL20 = true;

		initialize(new MySidekick(Debug.isDebuggerConnected(), this), cfg);
	}
}