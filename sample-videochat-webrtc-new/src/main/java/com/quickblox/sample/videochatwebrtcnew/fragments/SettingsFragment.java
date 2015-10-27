package com.quickblox.sample.videochatwebrtcnew.fragments;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.camera.CameraHelper;
import com.quickblox.sample.videochatwebrtcnew.sharing.CaptureFormat;

import java.util.List;


public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    private ProgressDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        fillResolutions();
    }

    public void fillResolutions(){
        ListPreference videoResolutionPreference = (ListPreference) findPreference(getString(R.string.pref_resolution_key));
        if (videoResolutionPreference.getEntries().length <=1 ) {
            new CameraFormatsAsyncTask().execute();
        }
    }

    private void fillResolutinPreference(List<CaptureFormat> supportedFormats,  ListPreference listPreference){
        CharSequence[] formatEntries = new CharSequence[supportedFormats.size() + 1];
        CharSequence[] formatValues = new CharSequence[supportedFormats.size() + 1 ];
        int i = 0;
        int j = 0;
        formatEntries[i++] = getString(R.string.pref_default_value);
        formatValues[j++] = getString(R.string.pref_resolution_default);
        int k = 0;
        for (CaptureFormat format : supportedFormats) {
            formatEntries[i++] = format.width +"x"+format.height;
            formatValues[j++] = ""+ (k++);
        }
        listPreference.setEntries(formatEntries);
        listPreference.setEntryValues(formatValues);
    }

    class CameraFormatsAsyncTask extends AsyncTask<Void, Void, List<CaptureFormat>>{

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(getActivity(), "", "Loading camera formats...", true);
        }

        @Override
        protected List<CaptureFormat> doInBackground(Void... params) {
            int cameraId = CameraHelper.getIdOfFrontFacingDevice();
            List<CaptureFormat> supportedFormats = null;
            if (cameraId != -1) {
                supportedFormats = CameraHelper.getSupportedFormats(cameraId);
            }
            return supportedFormats;
        }

        @Override
        protected void onPostExecute(List<CaptureFormat> captureFormats) {
            super.onPostExecute(captureFormats);
            if (dialog != null) {
                dialog.dismiss();
            }
            if (captureFormats != null && !captureFormats.isEmpty()) {
                fillResolutinPreference(captureFormats,
                        (ListPreference) findPreference(getString(R.string.pref_resolution_key)));
            }
        }
    }


}
