package com.quickblox.sample.videochatwebrtcnew.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.quickblox.sample.videochatwebrtcnew.R;


public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}
