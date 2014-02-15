package com.giorgioaresu.batchrenamer;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity implements FileList_Fragment.FileFragmentInterface, ActionList_Fragment.OnActionSelectedListener, ActionEdit_Fragment.actionEditFragment_Callbacks {

    private ArrayList<File> mFiles;
    private ArrayList<Uri> fileUris = new ArrayList<>();
    private ActionList_Fragment actionList_fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (savedInstanceState == null) {

        }

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Toast.makeText(this, "type: " + type, Toast.LENGTH_LONG).show();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            Parcelable parcelableExtra = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (parcelableExtra != null) {
                Uri uri = decodeUri((Uri) parcelableExtra);
                if (uri != null) {
                    fileUris.add(uri);
                } else {
                    // Unsupported Uri scheme
                    Toast.makeText(this, "Failed to get file path", Toast.LENGTH_LONG).show();
                }
            } else {
                // Unsupported stream, ie. text/plain
                handleUnsupportedObject(intent);
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            ArrayList<Uri> parcelableArrayListExtra = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (parcelableArrayListExtra != null) {
                // Add files to list
                Uri firstUri = parcelableArrayListExtra.get(0);
                if (firstUri.equals(decodeUri(firstUri))) {
                    fileUris.addAll(parcelableArrayListExtra);
                } else {
                    boolean filesSkipped = false;
                    for (Uri uri : parcelableArrayListExtra) {
                        uri = decodeUri(uri);
                        if (uri != null) {
                            fileUris.add(uri);
                        } else {
                            filesSkipped = true;
                        }
                    }
                    if (filesSkipped) {
                        // Unsupported Uri scheme
                        Toast.makeText(this, "Failed to get some file path", Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                // Unsupported stream, ie. text/plain
                handleUnsupportedObject(intent);
            }
        } else {
            // Handle other intents, such as being started from the home screen
        }

        setContentView(R.layout.activity_main);

        FragmentManager mFragmentManager = getFragmentManager();
        actionList_fragment = (ActionList_Fragment) mFragmentManager.findFragmentById(R.id.action_fragment);
    }

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
                    f.Rename();
                }
                return true;
            case R.id.action_settings:
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
    }

    @Override
    public ArrayList<File> provideFiles() {
        mFiles = new ArrayList<>();
        if (fileUris.isEmpty()) {
            mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
            mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
            mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
            mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
            mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
            mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
            mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
            mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
            mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
            mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
            mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
            mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        } else {
            for (Uri uri : fileUris) {
                mFiles.add(new File(uri));
            }
        }

        return mFiles;
    }

    @Override
    public void onActionSelected(Action action) {
        showDialog(action);
    }

    public void showDialog(Action action) {
        DialogFragment mDialogFragment = ActionEdit_Fragment.newInstance(action);
        mDialogFragment.show(getFragmentManager(), "editAction");
    }

    @Override
    public void notifyDataSetChanged() {
        FragmentManager mFragmentManager = getFragmentManager();
        ActionList_Fragment actionListFragment = (ActionList_Fragment) mFragmentManager.findFragmentById(R.id.action_fragment);
        ActionAdapter actionAdapter = (ActionAdapter) actionListFragment.getListAdapter();
        actionAdapter.notifyDataSetChanged();
    }
}
