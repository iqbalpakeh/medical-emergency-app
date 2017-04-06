package com.progremastudio.emergencymedicalteam.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.progremastudio.emergencymedicalteam.R;

public class AppSettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "app-settings";

    public static final String KEY_LATITUDE = "key_latitude";
    public static final String KEY_LONGITUDE = "key_longitude";
    public static final String KEY_MAP_TYPE = "key_map_type";
    public static final String KEY_TBM_EMERGENCY_CONTACT = "key_tbm_emergency_contact";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        resetSummary(KEY_LATITUDE);
        resetSummary(KEY_LONGITUDE);
        resetSummary(KEY_MAP_TYPE);
        resetSummary(KEY_TBM_EMERGENCY_CONTACT);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "key = " + key);
        Preference connectionPref = findPreference(key);
        connectionPref.setSummary(sharedPreferences.getString(key, ""));
    }

    private void resetSummary(String key) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String keyValue = sharedPref.getString(key, "");
        Preference preference = findPreference(key);
        preference.setSummary(keyValue);
    }

}
