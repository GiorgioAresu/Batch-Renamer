package com.giorgioaresu.batchrenamer;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the Callbacks
 * interface.
 */
public class FilePreview_ListFragment extends File_ListFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setItemLayout(R.layout.filepreview_list_row);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setFragmentLayout(R.layout.filepreview_list_fragment_content);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FilePreview_ListFragment() {
    }

    /**
     * Extracts file Uri from an intent and adds it to mFiles
     *
     * @param intent intent to be processed
     * @return true if file has been handled successfully, false otherwise
     */
    public boolean handleSendIntent(Intent intent) {
        clear();
        Parcelable parcelableExtra = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (parcelableExtra != null) {
            Uri uri = decodeUri((Uri) parcelableExtra);
            if (uri != null) {
                add(new File(uri));
            } else {
                // Unsupported Uri scheme
                Toast.makeText(getActivity(), "Failed to get file path", Toast.LENGTH_LONG).show();
            }
            return true;
        }

        // Unsupported stream
        return false;
    }

    /**
     * Extracts multiple file Uris from an intent and adds them to mFiles
     *
     * @param intent intent to be processed
     * @return true if files have been handled successfully, false otherwise
     */
    public boolean handleSendMultipleIntent(Intent intent) {
        clear();
        ArrayList<Uri> parcelableArrayListExtra = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (parcelableArrayListExtra != null) {
            boolean filesSkipped = false;
            for (Uri uri : parcelableArrayListExtra) {
                uri = decodeUri(uri);
                if (uri != null) {
                    add(new File(uri));
                } else {
                    filesSkipped = true;
                }
            }
            if (filesSkipped) {
                // Unsupported Uri scheme
                Toast.makeText(getActivity(), "Failed to get some file path", Toast.LENGTH_LONG).show();
            }
            return true;
        }

        // Unsupported stream
        return false;
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
            Cursor cursor = getActivity().getContentResolver().query(uri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            return Uri.parse("file:///" + cursor.getString(column_index));
        } else {
            Debug.log("failed to recognize Uri: " + uri.toString());
            return null;
        }
    }
}
