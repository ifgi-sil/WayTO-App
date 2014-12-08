package de.ifgi.wayto_prototype.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import de.ifgi.wayto_prototype.R;

/**
 * Fragment to display the settings
 *
 * @author Marius Runde
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
