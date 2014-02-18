package com.giorgioaresu.batchrenamer;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements FileList_Fragment.FileFragmentInterface {
    private ActionList_Fragment actionList_fragment;
    private FileList_Fragment fileList_fragment;
    private UpdateFileNames_AsyncTask updateFileNames_asyncTask;

    private TextView fileUpdatingLabel;
    private ProgressBar fileUpdatingProgress;
    private View fileUpdatingGUI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            // Eventually do something
        }

        setContentView(R.layout.activity_main);

        FragmentManager mFragmentManager = getFragmentManager();
        fileList_fragment = (FileList_Fragment) mFragmentManager.findFragmentById(R.id.file_fragment);
        actionList_fragment = (ActionList_Fragment) mFragmentManager.findFragmentById(R.id.action_fragment);
        actionList_fragment.getListAdapter().registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                startFileNamesUpdate();
            }
        });

        elaborateIntent(getIntent());

        fileUpdatingLabel = (TextView) findViewById(R.id.file_list_header_preview);
        fileUpdatingProgress = (ProgressBar) findViewById(R.id.file_list_loading_progressbar);
        fileUpdatingGUI = findViewById(R.id.file_list_loading);
    }

    /**
     * Check valids intents and sends to appropriate handlers
     *
     * @param intent Intent to work onto
     */
    private void elaborateIntent(Intent intent) {
        // Get action and MIME type
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            // Single file shared
            if (!fileList_fragment.handleSendIntent(intent)) {
                handleUnsupportedObject(intent);
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            // Multiple files shared
            if (!fileList_fragment.handleSendMultipleIntent(intent)) {
                handleUnsupportedObject(intent);
            }
        } else {
            // Handle other intents, such as being started from the home screen
        }
    }

    private void handleUnsupportedObject(Intent intent) {
        Toast.makeText(this, "Unsupported object, type: " + intent.getType(), Toast.LENGTH_LONG).show();
        // TODO: Do something more intelligent
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        actionList_fragment.populateActionMenu(menu.findItem(R.id.action_newAction).getSubMenu());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_start:
                for (File f : fileList_fragment.getFiles()) {
                    //f.newName = actionList_fragment.getNewName(f.currentName);
                    f.Rename();
                }
                return true;
            case R.id.action_settings:
                /*Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);*/
                return true;
            default:
                if (actionList_fragment.onNewActionSelected(item)) {
                    return true;
                } else {
                    return super.onOptionsItemSelected(item);
                }
        }
    }

    @Override
    public void onFileSelected(File file) {
        // TODO: Implement interface
        Toast.makeText(this, file.fileUri.toString(), Toast.LENGTH_LONG).show();
    }

    protected void startFileNamesUpdate() {
        // If there was already a task, try to stop it
        if (updateFileNames_asyncTask != null && updateFileNames_asyncTask.cancel(true)) {
            Log.d(getLocalClassName(), "Cancelled old async task");
        }

        Log.d(getLocalClassName(), "Firing async task");
        // Fire off an AsyncTask to compute file names
        updateFileNames_asyncTask = new UpdateFileNames_AsyncTask(new UpdateFileNames_AsyncTask.updateFileNames_Callbacks() {

            @Override
            public ActionList_Fragment getActionListFragment() {
                return actionList_fragment;
            }

            @Override
            public void updateFileNamesInUI() {
                // Back in the UI thread -- update our UI elements based on the data in mResults
                ((FileAdapter) fileList_fragment.getListAdapter()).notifyDataSetChanged();
            }

            @Override
            public void updateProgressInUI(Integer progress) {
                fileUpdatingProgress.setProgress(progress);
            }

            @Override
            public void setUiLoading() {
                fileUpdatingLabel.setText(getString(R.string.filelist_header_preview_updating));
                fileUpdatingProgress.setProgress(0);
                fileUpdatingGUI.setVisibility(View.VISIBLE);
            }

            @Override
            public void setUiResult() {
                fileUpdatingLabel.setText(getString(R.string.filelist_header_preview));
                fileUpdatingGUI.setVisibility(View.GONE);
            }
        });

        updateFileNames_asyncTask.execute(fileList_fragment.getFiles());
    }
}
