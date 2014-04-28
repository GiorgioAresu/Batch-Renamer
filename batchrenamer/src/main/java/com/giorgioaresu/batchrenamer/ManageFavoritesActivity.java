package com.giorgioaresu.batchrenamer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;


public class ManageFavoritesActivity extends Activity {
    JSONArray favorites;
    JSONException exception;
    int userChoiceOnImportingDuplicate;
    static final int REPLACE = 2;
    static final int SKIP = 1;
    static final int RENAME = 0;
    static final int INVALID = -1;

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
    protected void onPause() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String favs = favorites.toString();
        prefs.edit().putString(MainActivity.PREF_KEY_FAVORITES, favs).apply();
        super.onPause();
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
        final int index = mViewPager.getCurrentItem();

        switch (item.getItemId()) {
            case R.id.action_favoritesManage_operations_rename:
                renameFavorite(index);
                return true;
            case R.id.action_favoritesManage_operations_new:
                renameFavorite(-1);
                return true;
            case R.id.action_favoritesManage_operations_remove:
                removeFavorite(index);
                return true;
            case R.id.action_favoritesManage_operations_import:
                // TODO: Ask for a file
                String filename = Application.EXTERNAL_FOLDER + "- ALL -" + ".json";
                java.io.File file = new java.io.File(Environment.getExternalStorageDirectory(), filename);
                String readJSON = loadJSONFromFile(file);
                if (readJSON == null) {
                    Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
                } else {
                    // Try reading it as a single rule
                    try {
                        JSONObject object = new JSONObject(readJSON);
                        exception = null;
                        putCheckingDuplicates(favorites, object, false);
                        if (exception!=null) throw exception;
                        Toast.makeText(this, R.string.action_favoritesManage_operations_importDone, Toast.LENGTH_SHORT).show();
                        return true;
                    } catch (JSONException e) {
                        Debug.logError(getClass(), "Error reading file content as JSONObject", e);
                    }

                    // Try reading it as a set of rules
                    try {
                        JSONArray array = new JSONArray(readJSON);
                        exception = null;
                        userChoiceOnImportingDuplicate = INVALID;
                        for (int i=0; i<array.length(); ++i) {
                            putCheckingDuplicates(favorites, array.getJSONObject(i), true);
                            if (exception!=null) {
                                // Notify adapter of changes made till now and throw exception
                                mSectionsPagerAdapter.notifyDataSetChanged();
                                throw exception;
                            }
                        }
                        mSectionsPagerAdapter.notifyDataSetChanged();
                        Toast.makeText(this, R.string.action_favoritesManage_operations_importDone, Toast.LENGTH_SHORT).show();
                        return true;
                    } catch (JSONException e) {
                        Debug.logError(getClass(), "Error reading file content as JSONArray", e);
                    }

                    // Both failed, warn user
                    Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_favoritesManage_operations_export:
                exportFavorite(index);
                return true;
            case R.id.action_favoritesManage_operations_exportAll:
                exportAllFavorites();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Rename a favorite or create a new one
     * @param index if -1 will create a new one,
     *              otherwise will rename the current one
     */
    private void renameFavorite(final int index) {
        final Context context = this;

        // Create an EditText view to get user input
        final EditText input = new EditText(this);

        // Ask for a label and a confirm if already present
        AlertDialog.Builder alert = new AlertDialog.Builder(context)
                .setTitle(R.string.action_favoritesAddLabel)
                .setView(input)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int whichButton) {
                        final String label = input.getText().toString();
                        if (label != "") {
                            try {
                                if (favorites != null && JSONUtil.has(favorites, MainActivity.FAVORITE_KEY_TITLE, label)) {
                                    AlertDialog.Builder replaceDialog = new AlertDialog.Builder(context)
                                            .setMessage(R.string.action_favoritesAddReplaceLabel)
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int i) {
                                                    if (index == -1) {
                                                        addFav(label);
                                                    } else {
                                                        renameFav(label, index);
                                                    }
                                                }
                                            })
                                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int i) {
                                                    dialog.cancel();
                                                }
                                            });
                                    replaceDialog.show();
                                } else {
                                    if (index == -1) {
                                        addFav(label);
                                    } else {
                                        renameFav(label, index);
                                    }
                                }
                            } catch (JSONException e) {
                                Debug.logError(getClass(), "Error checking label", e);
                            }
                        } else {
                            Toast.makeText(context, R.string.action_favoritesAddLabelInvalid, Toast.LENGTH_SHORT).show();
                        }
                    }

                    /**
                     * Add a favorite. If an element has the same name replaces it
                     * @param label name of the favorite
                     */
                    private void addFav(String label) {
                        renameFavoriteHelper(label, null);
                    }

                    /**
                     * Rename a favorite. If an element has the same name replaces it
                     * @param label name of the favorite
                     * @param index index of the element renamed
                     */
                    private void renameFav(String label, int index) {
                        try {
                            JSONObject obj = favorites.getJSONObject(index);
                            if (obj != null) {
                                renameFavoriteHelper(label, obj);
                            } else {
                                Debug.logError(getClass(), "Error getting favorite to rename, object is null");
                                Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Debug.logError(getClass(), "Error getting favorite to rename", e);
                            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        }
                    }


                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
        alert.show();
    }

    /**
     * Rename a favorite or add a new empty one. If an element has the same name replaces it
     * @param label name of the favorite
     * @param favorite favorite to be renamed. If null, a new empty
     *                 favorite will be added
     */
    private void renameFavoriteHelper(String label, JSONObject favorite) {
        try {
            if (favorite == null) {
                favorite = new JSONObject();
                favorite.put(MainActivity.FAVORITE_KEY_RULES, new JSONArray());
            }
            favorite.put(MainActivity.FAVORITE_KEY_TITLE, label);
            int index = JSONUtil.indexOf(favorites, MainActivity.FAVORITE_KEY_TITLE, label);
            if (index == JSONUtil.POSITION_INVALID) {
                favorites.put(favorite);
            } else {
                JSONUtil.replace(favorites, index, favorite);
            }
            mSectionsPagerAdapter.notifyDataSetChanged();
            Toast.makeText(this, String.format(getString(R.string.action_favoritesAdded), label), Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            Debug.logError(getClass(), "Error adding or renaming a favorite", e);
        }
    }

    /**
     * Remove an element from favorites
     * @param index index of the element to be removed
     */
    private void removeFavorite(int index) {
        String title = mSectionsPagerAdapter.getPageTitle(mViewPager.getCurrentItem()).toString();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            showUndobarForRemovedFavorite(index);
            favorites.remove(index);
            mSectionsPagerAdapter.notifyDataSetChanged();
            Toast.makeText(this, String.format("%s removed", title), Toast.LENGTH_SHORT).show();
        } else {
            try {
                showUndobarForRemovedFavorite(index);
                favorites = JSONUtil.remove(favorites, index);
                mSectionsPagerAdapter.notifyDataSetChanged();
                Toast.makeText(this, String.format("%s removed", title), Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                Debug.logError(getClass(), "Error removing favorite", e);
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Try to show undobar to allow favorite restore. It doesn't give info whether
     * any errors occurred but it tries to handle them gracefully
     * @param index index of the element being removed
     */
    private void showUndobarForRemovedFavorite(final int index) {
        // TODO WHAT HAPPENS WHEN REMOVE ALL FAVORITES?
        try {
            JSONObject objectBeingRemoved = favorites.getJSONObject(index);
            final Context context = getApplicationContext();
            Bundle bundle = new Bundle();
            bundle.putString("FAVORITE", objectBeingRemoved.toString());
            bundle.putInt("INDEX", index);
            new UndoBarController(findViewById(R.id.undobar), new UndoBarController.UndoListener() {
                @Override
                public void onUndo(Parcelable token) {
                    Bundle b = (Bundle) token;
                    try {
                        JSONObject obj = new JSONObject(b.getString("FAVORITE"));
                        favorites.put(index, obj);
                        mSectionsPagerAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Debug.logError(getClass(), "Error restoring favorite", e);
                        Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                    }
                }
            }).showUndoBar(false, String.format(getString(R.string.action_favoritesManage_operations_removeDone), objectBeingRemoved.getString(MainActivity.FAVORITE_KEY_TITLE)), bundle);
        } catch (JSONException e) {
            Debug.logError(getClass(), "Error showing undobar", e);
            Toast.makeText(this, R.string.done, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Export a favorite to a file
     * @param index index of the element to be exported
     */
    private void exportFavorite(int index) {
        try {
            String title = mSectionsPagerAdapter.getPageTitle(mViewPager.getCurrentItem()).toString();
            String filename = Application.EXTERNAL_FOLDER + title + ".json";
            exportToFile(filename, favorites.getJSONObject(index).toString(4));
        } catch (JSONException e) {
            Debug.logError(getClass(), "Error reading favorite to export", e);
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Export all favorites to a file
     */
    private void exportAllFavorites() {
        try {
            String filename = Application.EXTERNAL_FOLDER + "- ALL -.json";
            exportToFile(filename, favorites.toString(4));
        } catch (JSONException e) {
            Debug.logError(getClass(), "Error reading favorites to export", e);
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Helper method to save favorites to a file. Creates parent folder
     * when needed
     * @param filename filename to be used (relative to SD)
     * @param what String to be written
     */
    private void exportToFile(String filename, String what) {
        java.io.File file = new java.io.File(Environment.getExternalStorageDirectory(), filename);
        // Create parent folders if they don't exist
        file.getParentFile().mkdirs();
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(file, false);
        } catch (Exception e) {
            Debug.logError(getClass(), "Error exporting favorites", e);
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            outputStream.write(what.getBytes());
            outputStream.close();
        } catch (Exception e) {
            Debug.logError(getClass(), "Error exporting favorites", e);
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            return;
        }
        String message = String.format(getString(R.string.action_favoritesManage_operations_exportDone), filename);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Put object in array asking user what to do in case of duplicate names. If any
     * exception occurs during dialogs you'll find it in exception
     *
     * @param array array to add object to
     * @param object object to be added to array
     * @param b if true and userChoiceOnImportingDuplicate != INVALID, that choice
     *          will be repeated.
     *          if true and userChoiceOnImportingDuplicate = INVALID the dialog will
     *          show "don't ask again" option in case of found duplicates
     *          if false the dialog won't show the "don't task again" option
     */
    private void putCheckingDuplicates(final JSONArray array, final JSONObject object, boolean b) throws JSONException {
        final String favName =  object.getString(MainActivity.FAVORITE_KEY_TITLE);
        final int indexOfDuplicate = JSONUtil.indexOf(array, MainActivity.FAVORITE_KEY_TITLE, favName);

        if (indexOfDuplicate == JSONUtil.POSITION_INVALID) {
            array.put(object);
        } else {
            if (b && userChoiceOnImportingDuplicate != INVALID) {
                switch (userChoiceOnImportingDuplicate) {
                    case REPLACE:
                        JSONUtil.replace(array, indexOfDuplicate, object);
                        mSectionsPagerAdapter.notifyDataSetChanged();
                        break;
                    case RENAME:
                        String newName;
                        int counter = 2;
                        do {
                            newName = String.format("%s (%d)", favName, counter++);
                        } while (JSONUtil.has(favorites, MainActivity.FAVORITE_KEY_TITLE, newName));
                        object.put(MainActivity.FAVORITE_KEY_TITLE, newName);
                        favorites.put(object);
                        mSectionsPagerAdapter.notifyDataSetChanged();
                        break;
                    default:
                        // Skip, do nothing
                }
            } else {
                final CheckBox dontAsk = new CheckBox(this);

                // Ask for a label and a confirm if already present
                AlertDialog.Builder alert = new AlertDialog.Builder(this)
                        .setMessage(String.format(getString(R.string.action_favoritesManage_operations_importDuplicate_message), favName))
                        .setPositiveButton(R.string.action_favoritesManage_operations_importDuplicate_replace, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int whichButton) {
                                try {
                                    JSONUtil.replace(array, indexOfDuplicate, object);
                                    mSectionsPagerAdapter.notifyDataSetChanged();
                                    if (dontAsk.isChecked()) {
                                        userChoiceOnImportingDuplicate = REPLACE;
                                    }
                                } catch (JSONException e) {
                                    Debug.logError(getClass(), "Error replacing element in favorites", e);
                                    exception = e;
                                }
                                dialog.dismiss();
                            }
                        })
                        .setNeutralButton(R.string.action_favoritesManage_operations_importDuplicate_rename, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                try {
                                    String newName;
                                    int counter = 2;
                                    do {
                                        newName = String.format("%s (%d)", favName, counter++);
                                    } while (JSONUtil.has(favorites, MainActivity.FAVORITE_KEY_TITLE, newName));
                                    object.put(MainActivity.FAVORITE_KEY_TITLE, newName);
                                    favorites.put(object);
                                    mSectionsPagerAdapter.notifyDataSetChanged();
                                } catch (JSONException e) {
                                    Debug.logError(getClass(), "Error renaming element in favorites", e);
                                    exception = e;
                                }
                                if (dontAsk.isChecked()) {
                                    userChoiceOnImportingDuplicate = RENAME;
                                }
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.action_favoritesManage_operations_importDuplicate_skip, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (dontAsk.isChecked()) {
                                    userChoiceOnImportingDuplicate = SKIP;
                                }
                                dialog.dismiss();
                            }
                        });
                if (b) {
                    dontAsk.setText(R.string.action_favoritesManage_operations_importDuplicate_rememberChoice);
                    alert.setView(dontAsk);
                }
                alert.show();
            }
        }
    }

    public String loadJSONFromFile(java.io.File file) {
        String json = null;
        try {
            InputStream is = new FileInputStream(file);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            Debug.logError(getClass(), "Error reading JSON from file", ex);
            return null;
        }
        return json;
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
