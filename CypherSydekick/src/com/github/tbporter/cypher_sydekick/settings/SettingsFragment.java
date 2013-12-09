package com.github.tbporter.cypher_sydekick.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.github.tbporter.cypher_sydekick.R;

public class SettingsFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Load the preferences XML
		addPreferencesFromResource(R.xml.preferences);
	}

}
