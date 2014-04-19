package com.giorgioaresu.batchrenamer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.GregorianCalendar;

public class MainActivity extends Activity implements File_ListFragment.FileFragmentInterface {
    GregorianCalendar expDate = new GregorianCalendar( 2014, 11, 31 ); // midnight
    GregorianCalendar now = new GregorianCalendar();

    public static java.io.File scriptFile;

    private Rule_ListFragment ruleList_fragment;
    private FilePreview_ListFragment filePreviewList_fragment;
    private UpdateFileNames_AsyncTask updateFileNames_asyncTask;

    private UpdatingFilenamesGuiHolder guiHolder;

    private Eula eula = new Eula();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if expired
        if (now.after(expDate)) {
            new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(R.string.expired_message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final String appPackageName = getPackageName();
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                            }
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        }

        if (!Eula.hasAcceptedEula(this)) {
            eula.show(false, this);
        }

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
                            outputStream.write((line + "\n").getBytes());
                        }
                        outputStream.close();
                    } catch (Exception e) {
                        Debug.logError("Failed to copy script", e);
                    }
                }
            };
            runnable.run();
        }

        FragmentManager mFragmentManager = getFragmentManager();
        filePreviewList_fragment = (FilePreview_ListFragment) mFragmentManager.findFragmentById(R.id.file_fragment);
        ruleList_fragment = (Rule_ListFragment) mFragmentManager.findFragmentById(R.id.rule_fragment);
        ruleList_fragment.getListAdapter().registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                startFileNamesUpdate();
            }
        });

        elaborateIntent(getIntent());

        guiHolder = new UpdatingFilenamesGuiHolder();

        if (ruleList_fragment.getListAdapter().getCount() > 0){
            startFileNamesUpdate();
        }
    }

    @Override
    protected void onPause() {
        eula.dismiss();
        super.onPause();
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

        if (Debug.isExternalStorageWritable()) {
            Debug.log("Action " + action);
            Debug.log("Type " + type);
        } else {
            Toast.makeText(this, "Permessi insufficienti per scrivere il file di log", Toast.LENGTH_LONG).show();
        }

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
                } else if (ruleList_fragment.getListAdapter().getCount() == 0) {
                    Toast.makeText(this, getString(R.string.empty_rulelist), Toast.LENGTH_LONG).show();
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
            case R.id.action_settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
                return true;
            case R.id.action_about:
                Intent about = new Intent(this, AboutActivity.class);
                startActivity(about);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFileSelected(File file) {
        // TODO: Implement interface
        Toast.makeText(this, file.newName, Toast.LENGTH_LONG).show();
    }

    protected void startFileNamesUpdate() {
        // If there was already a task, try to stop it
        if (updateFileNames_asyncTask != null && updateFileNames_asyncTask.cancel(true)) {
            Debug.log("Cancelled old async task");
        }

        Debug.log("Firing async newnames task");
        // Fire off an AsyncTask to compute file names
        updateFileNames_asyncTask = new UpdateFileNames_AsyncTask(this, new UpdateFileNames_AsyncTask.updateFileNames_Callbacks() {

            @Override
            public Rule_ListFragment getRuleListFragment() {
                return ruleList_fragment;
            }

            @Override
            public File_ListFragment getFileListFragment() { return filePreviewList_fragment; }

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
        /*Intent intent = new Intent(this, RenameStatusActivity.class);
        startActivity(intent);*/

        Debug.log("Firing async rename task");

        RenameFiles_AsyncTask renameFiles_asyncTask = new RenameFiles_AsyncTask(new RenameFiles_AsyncTask.renameFiles_Callbacks() {
            @Override
            public void updateProgressInUI(Integer progress, Integer elements, File.RENAME result) {
                //Debug.log("Progress: " + progress);
            }

            @Override
            public void setUiLoading() {
                Debug.log("Preparing UI for rename");
            }

            @Override
            public void setUiResult() {
                Debug.log("Resetting UI after rename");
            }

            @Override
            public Context getContext() {
                return getApplicationContext();
            }
        });

        renameFiles_asyncTask.execute(filePreviewList_fragment.getFiles());

        Toast.makeText(this, R.string.action_start_toast, Toast.LENGTH_LONG).show();

        // Prevent user going back to this
        finish();
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
