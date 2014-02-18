package com.giorgioaresu.batchrenamer;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity implements FileList_Fragment.FileFragmentInterface, ActionList_Fragment.OnActionSelectedListener, ActionEdit_Fragment.actionEditFragment_Callbacks {
    private ArrayList<File> mFiles = new ArrayList<>();
    private FileAdapter fileAdapter;
    private ActionList_Fragment actionList_fragment;
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

        elaborateIntent(getIntent());

        setContentView(R.layout.activity_main);

        FragmentManager mFragmentManager = getFragmentManager();
        fileAdapter = (FileAdapter) ((FileList_Fragment) mFragmentManager.findFragmentById(R.id.file_fragment)).getListAdapter();
        actionList_fragment = (ActionList_Fragment) mFragmentManager.findFragmentById(R.id.action_fragment);
        actionList_fragment.getListAdapter().registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                startFileNamesUpdate();
            }
        });

        fileUpdatingLabel = (TextView) findViewById(R.id.file_list_header_preview);
        fileUpdatingProgress = (ProgressBar) findViewById(R.id.file_list_loading_progressbar);
        fileUpdatingGUI = findViewById(R.id.file_list_loading);
    }

    /**
     * Works on intent to retrieve data. Gets passed files and adds them to mFiles
     *
     * @param intent Intent to work onto
     */
    private void elaborateIntent(Intent intent) {
        // Get action and MIME type
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            // Single file shared
            Parcelable parcelableExtra = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (parcelableExtra != null) {
                Uri uri = decodeUri((Uri) parcelableExtra);
                if (uri != null) {
                    mFiles.add(new File(uri));
                } else {
                    // Unsupported Uri scheme
                    Toast.makeText(this, "Failed to get file path", Toast.LENGTH_LONG).show();
                }
            } else {
                // Unsupported stream, ie. text/plain
                handleUnsupportedObject(intent);
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            // Multiple files shared
            ArrayList<Uri> parcelableArrayListExtra = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (parcelableArrayListExtra != null) {
                boolean filesSkipped = false;
                for (Uri uri : parcelableArrayListExtra) {
                    uri = decodeUri(uri);
                    if (uri != null) {
                        mFiles.add(new File(uri));
                    } else {
                        filesSkipped = true;
                    }
                }
                if (filesSkipped) {
                    // Unsupported Uri scheme
                    Toast.makeText(this, "Failed to get some file path", Toast.LENGTH_LONG).show();
                }
            } else {
                // Unsupported stream, ie. text/plain
                handleUnsupportedObject(intent);
            }
        } else {
            // Handle other intents, such as being started from the home screen
        }
    }

    /**
     * Decode various types of Uri to get full file path
     *
     * @param uri Uri to be decoded
     * @return Uri containing the absolute path of the file if the file has been decoded succesfully, null otherwise
     */
    private Uri decodeUri(Uri uri) {
        String scheme = uri.getScheme();
        if ("file".equals(scheme)) {
            return uri;
        } else if ("content".equals(scheme)) {
            // Find in media store database the true reference to the file
            String[] proj = {MediaStore.MediaColumns.DATA};
            Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            return Uri.parse("file:///" + cursor.getString(column_index));
        } else {
            Log.e(getLocalClassName(), "failed to recognize Uri: " + uri.toString());
            return null;
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
                for (File f : mFiles) {
                    f.newName = actionList_fragment.getNewName(f.currentName);
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

    @Override
    public ArrayList<File> provideFiles() {
        if (mFiles.isEmpty()) {
            for (int i = 0; i < 50; i++) {
                mFiles.add(new File(Uri.parse("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip")));
            }
        }

        return mFiles;
    }

    @Override
    public void onActionSelected(Action action) {
        DialogFragment mDialogFragment = ActionEdit_Fragment.newInstance(action);
        mDialogFragment.show(getFragmentManager(), "editAction");
    }

    @Override
    public void notifyActionDataSetChanged() {
        ActionAdapter actionAdapter = (ActionAdapter) actionList_fragment.getListAdapter();
        actionAdapter.notifyDataSetChanged();
    }

    protected void startFileNamesUpdate() {
        // If there was already a task, try to stop it
        if (updateFileNames_asyncTask != null) {
            Log.w("startFileNamesUpdate", "Cancelling async task");
            if (updateFileNames_asyncTask.cancel(true)) {
                Log.w("startFileNamesUpdate", "Cancelled async task");
            }
        }

        Log.w("startFileNamesUpdate", "Firing async task");
        // Fire off an AsyncTask to compute file names
        updateFileNames_asyncTask = new UpdateFileNames_AsyncTask(new UpdateFileNames_AsyncTask.updateFileNames_Callbacks() {

            @Override
            public ActionList_Fragment getActionListFragment() {
                Log.w("AsyncTask", "Getting action fragment");
                return actionList_fragment;
            }

            @Override
            public void updateFileNamesInUI() {
                Log.w("AsyncTask", "Updating GUI");

                // Back in the UI thread -- update our UI elements based on the data in mResults
                fileAdapter.notifyDataSetChanged();
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

        updateFileNames_asyncTask.execute(mFiles);
    }
}
