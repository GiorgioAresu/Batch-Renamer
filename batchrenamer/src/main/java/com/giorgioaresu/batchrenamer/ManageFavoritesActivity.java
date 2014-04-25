package com.giorgioaresu.batchrenamer;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class ManageFavoritesActivity extends Activity {
    JSONArray favorites;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections.
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
            favorites = (favs != null) ? new JSONArray(favs) : new JSONArray();
        } catch (JSONException e) {
            favorites = new JSONArray();
            Debug.logError(getClass(), "Error retrieving favorites");
            Toast.makeText(this, R.string.favorites_loading_error, Toast.LENGTH_SHORT).show();
            finish();
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
        switch (item.getItemId()) {
            case R.id.action_favoritesManage_operations_new:
                return true;
            case R.id.action_favoritesManage_operations_remove:
                int index = mViewPager.getCurrentItem();
                String title = mSectionsPagerAdapter.getPageTitle(mViewPager.getCurrentItem()).toString();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    favorites.remove(index);
                } else {
                    try {
                        favorites = JSONUtil.remove(favorites, index);
                    } catch (JSONException e) {
                        Debug.logError(getClass(), "Error removing favorite", e);
                    }
                }
                mSectionsPagerAdapter.notifyDataSetChanged();

                Toast.makeText(this, String.format("%s removed", title), Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * A {@link FragmentStatePagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        final Context c = getBaseContext();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public int getItemPosition(Object item) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            ArrayList<Rule> rules = new ArrayList<>();
            String name = "";
            try {
                JSONObject item = favorites.getJSONObject(position);
                name = item.getString(MainActivity.FAVORITE_KEY_TITLE);
                JSONArray jRules = item.getJSONArray(MainActivity.FAVORITE_KEY_RULES);
                for (int i=0; i<jRules.length(); ++i) {
                    rules.add(Rule.createFromJSON(c, jRules.getJSONObject(i)));
                }
            } catch (JSONException e) {
                Debug.logError(getClass(), "Error getting favorites", e);
                Toast.makeText(c, R.string.favorites_loading_error, Toast.LENGTH_SHORT).show();
            }
            Rule_ListFragment.withHorizontalPadding fragment = Rule_ListFragment.withHorizontalPadding.newInstance(rules);
            fragment.setTitle(name);
            return fragment;
        }

        @Override
        public int getCount() {
            return favorites.length();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            try {
                JSONObject item = favorites.getJSONObject(position);
                return item.getString(MainActivity.FAVORITE_KEY_TITLE);
            } catch (JSONException e) {
                Debug.logError(getClass(), "Error getting favorites name", e);
                Toast.makeText(c, R.string.favorites_loading_error, Toast.LENGTH_SHORT).show();
            }
            return null;
        }
    }
}
