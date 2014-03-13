package com.giorgioaresu.batchrenamer;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .add(android.R.id.content, new AboutFragment())
                .commit();
    }

    public static class AboutFragment extends PreferenceFragment {

        public AboutFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences_about);

            // Set app version summary
            String versionName;
            final PackageManager packageManager = getActivity().getPackageManager();
            if (packageManager != null) {
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(getActivity().getPackageName(), 0);
                    versionName = packageInfo.versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    versionName = null;
                }
                findPreference("about_version").setSummary(versionName);
            }
        }
    }

}
