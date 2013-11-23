package com.github.tbporter.cypher_sydekick.activities;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.karien.tacobox.MyTacoBox;

public class GameActivity extends AndroidApplication {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
		cfg.useGL20 = true;

		initialize(new MyTacoBox(), cfg);
	}
}