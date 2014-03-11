package com.giorgioaresu.batchrenamer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
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

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class MainActivity extends Activity implements File_ListFragment.FileFragmentInterface {
    public static java.io.File scriptFile;
    private Action_ListFragment actionList_fragment;
    private FilePreview_ListFragment filePreviewList_fragment;
    private UpdateFileNames_AsyncTask updateFileNames_asyncTask;

    private UpdatingFilenamesGuiHolder guiHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scriptFile = new java.io.File(getFilesDir(), "root_rename.sh");

        if (savedInstanceState == null) {
            // Eventually do something
            // Copy script to internal storage
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        FileOutputStream outputStream = new FileOutputStream(scriptFile);
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.root_rename)));
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            Log.d("Writing", line);
                            outputStream.write((line + "\n").getBytes());
                        }
                        outputStream.close();
                    } catch (Exception e) {
                        Log.e(getLocalClassName(), "Failed to copy script");
                    }
                }
            };
            runnable.run();
        }

        FragmentManager mFragmentManager = getFragmentManager();
        filePreviewList_fragment = (FilePreview_ListFragment) mFragmentManager.findFragmentById(R.id.file_fragment);
        actionList_fragment = (Action_ListFragment) mFragmentManager.findFragmentById(R.id.action_fragment);
        actionList_fragment.getListAdapter().registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                startFileNamesUpdate();
            }
        });

        elaborateIntent(getIntent());

        guiHolder = new UpdatingFilenamesGuiHolder();
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
            if (!filePreviewList_fragment.handleSendIntent(intent)) {
                handleUnsupportedObject(intent);
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            // Multiple files shared
            if (!filePreviewList_fragment.handleSendMultipleIntent(intent)) {
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_start:
                if (filePreviewList_fragment.getListAdapter().getCount() == 0) {
                    Toast.makeText(this, getString(R.string.empty_filelist), Toast.LENGTH_LONG).show();
                } else if (actionList_fragment.getListAdapter().getCount() == 0) {
                    Toast.makeText(this, getString(R.string.empty_actionlist), Toast.LENGTH_LONG).show();
                } else {
                    // Show alert to confirm rename
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.action_start_alert_message)
                            .setTitle(R.string.action_start_alert_title)
                            .setPositiveButton(R.string.action_start_alert_positive, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Rename files
                                    startFileRename();
                                }
                            })
                            .setNegativeButton(R.string.action_start_alert_negative, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create()
                            .show();
                }
                return true;
            /*case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;*/
            default:
                return super.onOptionsItemSelected(item);
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

        Log.d(getLocalClassName(), "Firing async newnames task");
        // Fire off an AsyncTask to compute file names
        updateFileNames_asyncTask = new UpdateFileNames_AsyncTask(new UpdateFileNames_AsyncTask.updateFileNames_Callbacks() {

            @Override
            public Action_ListFragment getActionListFragment() {
                return actionList_fragment;
            }

            @Override
            public void setUiLoading() {
                guiHolder.setLoading();
            }

            @Override
            public void updateProgressInUI(Integer progress) {
                guiHolder.updateProgress(progress);
            }

            @Override
            public void setUiResult() {
                // Update our UI elements based on the data in mResults
                ((FileAdapter) filePreviewList_fragment.getListAdapter()).notifyDataSetChanged();

                guiHolder.setResult();
            }
        });

        updateFileNames_asyncTask.execute(filePreviewList_fragment.getFiles());
    }

    protected void startFileRename() {
        // Start activity
        Intent intent = new Intent(this, RenameStatusActivity.class);
        startActivity(intent);

        // Prevent user going back to this
        finish();

        /*// TODO: handle cancellation?
        Log.d(getLocalClassName(), "Firing async rename task");

        RenameFiles_AsyncTask renameFiles_asyncTask = new RenameFiles_AsyncTask(new RenameFiles_AsyncTask.renameFiles_Callbacks() {
            @Override
            public void updateProgressInUI(Integer progress, Integer elements, File.RENAME result) {
                Log.d("Rename task", "Progress: " + progress);
            }

            @Override
            public void setUiLoading() {
                Log.d("Rename task", "Preparing UI");
            }

            @Override
            public void setUiResult() {
                Log.d("Rename task", "Resetting UI");
            }

            @Override
            public Context getContext() {
                return getApplicationContext();
            }
        });

        renameFiles_asyncTask.execute(filePreviewList_fragment.getFiles());*/
    }

    private class UpdatingFilenamesGuiHolder {
        private TextView fileUpdatingLabel;
        private ProgressBar fileUpdatingProgress;
        private View fileUpdatingGUI;

        public UpdatingFilenamesGuiHolder() {
            fileUpdatingLabel = (TextView) findViewById(R.id.file_list_header_preview);
            fileUpdatingProgress = (ProgressBar) findViewById(R.id.file_list_loading_progressbar);
            fileUpdatingGUI = findViewById(R.id.file_list_loading);
        }

        public void setLoading() {
            fileUpdatingLabel.setText(getString(R.string.filelist_header_preview_updating));
            fileUpdatingProgress.setProgress(0);
            fileUpdatingGUI.setVisibility(View.VISIBLE);
        }

        public void updateProgress(Integer progress) {
            fileUpdatingProgress.setProgress(progress);
        }

        public void setResult() {
            fileUpdatingLabel.setText(getString(R.string.filelist_header_preview));
            fileUpdatingGUI.setVisibility(View.GONE);
        }
    }
}
