package de.ifgi.wayto_prototype.activities;

import android.app.Activity;
import android.os.Bundle;

import de.ifgi.wayto_prototype.fragments.SettingsFragment;

/**
 * Activity to change the application's settings
 *
 * @author Marius Runde
 */
public class SettingsActivity extends Activity {

    public static final String PREF_KEY_MAP_GOOGLE = "pref_key_map_google";
    public static final String PREF_KEY_MAP_TYPE = "pref_key_map_type";
    public static final String PREF_KEY_COLOURED = "pref_key_coloured";
    public static final String PREF_KEY_METHOD = "pref_key_method";
    public static final String PREF_KEY_METHOD_TYPE = "pref_key_method_type";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the settings fragment as the main content
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
