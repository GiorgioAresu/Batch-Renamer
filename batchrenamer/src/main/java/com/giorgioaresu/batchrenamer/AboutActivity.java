package com.giorgioaresu.batchrenamer;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import java.util.Calendar;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .add(android.R.id.content, new AboutFragment())
                .commit();
    }

    public static class AboutFragment extends PreferenceFragment {
        Eula eula = new Eula();

        public AboutFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            final Activity activity = getActivity();

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences_about);

            // Set author summary
            String author = String.format(activity.getString(R.string.about_author_summary), Calendar.getInstance().get(Calendar.YEAR));
            findPreference("about_author").setSummary(author);

            // Set app version summary
            String versionName;
            final PackageManager packageManager = activity.getPackageManager();
            if (packageManager != null) {
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(activity.getPackageName(), 0);
                    versionName = packageInfo.versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    versionName = null;
                }
                findPreference("about_version").setSummary(versionName);
            }

            // Set eula click listener
            findPreference("about_eula").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    eula.show(true, activity);
                    return true;
                }
            });
        }

        @Override
        public void onPause() {
            eula.dismiss();
            super.onPause();
        }
    }
}
