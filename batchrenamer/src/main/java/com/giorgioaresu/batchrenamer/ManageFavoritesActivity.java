package com.giorgioaresu.batchrenamer;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class ManageFavoritesActivity extends Activity {
    JSONObject favorites;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_favorites);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String favs = prefs.getString(MainActivity.PREF_KEY_FAVORITES, null);
        try {
            favorites = (favs != null) ? new JSONObject(favs) : new JSONObject();
        } catch (JSONException e) {
            Debug.logError(getClass(), "Error retrieving favorites");
            Toast.makeText(this, R.string.favorites_loading_error, Toast.LENGTH_SHORT).show();
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.manage_favorites, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        JSONArray names = favorites.names();
        Context c = getBaseContext();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            ArrayList<Rule> rules = new ArrayList<>();
            try {
                JSONArray fav = (JSONArray) favorites.get(names.getString(position));
                for (int i=0; i<fav.length(); ++i) {
                    rules.add(Rule.createFromJSON(c, fav.getJSONObject(i)));
                }
            } catch (JSONException e) {
                Debug.logError(getClass(), "Error getting favorites", e);
                Toast.makeText(c, R.string.favorites_loading_error, Toast.LENGTH_SHORT).show();
            }
            return Rule_ListFragment.withHorizontalPadding.newInstance(rules);
        }

        @Override
        public int getCount() {
            return favorites.length();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            try {
                return names.getString(position);
            } catch (JSONException e) {
                Debug.logError(getClass(), "Error getting favorites name", e);
                Toast.makeText(c, R.string.favorites_loading_error, Toast.LENGTH_SHORT).show();
            }
            return null;
        }
    }
}
