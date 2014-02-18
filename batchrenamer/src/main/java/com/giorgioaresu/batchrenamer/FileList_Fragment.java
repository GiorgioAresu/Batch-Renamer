package com.giorgioaresu.batchrenamer;

import android.app.Activity;
import android.app.ListFragment;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the Callbacks
 * interface.
 */
public class FileList_Fragment extends ListFragment {

    private static final String ARG_FILES = "files";

    public ArrayList<File> getFiles() {
        return mFiles;
    }

    private ArrayList<File> mFiles = new ArrayList<>();

    private FileFragmentInterface mInterface;

    public static FileList_Fragment newInstance(ArrayList<File> files) {
        FileList_Fragment fragment = new FileList_Fragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_FILES, files);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FileList_Fragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            //Retrieve files from arguments
            mFiles = getArguments().getParcelableArrayList(ARG_FILES);
        } else if (savedInstanceState != null) {
            // Retrieve actions from saved state (ie. after rotation)
            mFiles = savedInstanceState.getParcelableArrayList(ARG_FILES);
        } else {
            // Eventually populate actions for the first time
            // TODO: remove this
            if (mFiles.isEmpty()) {
                for (int i = 0; i < 50; i++) {
                    mFiles.add(new File(Uri.parse("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip")));
                }
            }
        }

        setListAdapter(new FileAdapter(getActivity(), R.layout.file_list_row, mFiles));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(ARG_FILES, mFiles);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout
        LinearLayout fragmentLayout = (LinearLayout) inflater.inflate(R.layout.file_list_fragment_content, container, false);

        /*// Set header
        ListView listView = (ListView) fragmentLayout.findViewById(android.R.id.list);
        listView.addHeaderView(inflater.inflate(R.layout.file_list_header_row, null), null,	false);

        // Set title
        View header = fragmentLayout.findViewById(R.id.file_list_header);
        if (header != null)
            ((TextView) header.findViewById(R.id.section_title_label)).setText(R.string.section_title_filelist);*/

        return fragmentLayout;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mInterface = (FileFragmentInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FileFragmentInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mInterface = null;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mInterface) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mInterface.onFileSelected(mFiles.get(position));
        }
    }

    /**
     * Extracts file Uri from an intent and adds it to mFiles
     * @param intent intent to be processed
     * @return true if file has been handled successfully, false otherwise
     */
    public boolean handleSendIntent(Intent intent) {
        Parcelable parcelableExtra = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (parcelableExtra != null) {
            Uri uri = decodeUri((Uri) parcelableExtra);
            if (uri != null) {
                mFiles.add(new File(uri));
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
     * @param intent intent to be processed
     * @return true if files have been handled successfully, false otherwise
     */
    public boolean handleSendMultipleIntent(Intent intent) {
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
            Log.w(getClass().getCanonicalName(), "failed to recognize Uri: " + uri.toString());
            return null;
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow communication between them.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface FileFragmentInterface {
        /**
         * Allow an interaction in this fragment to be communicated
         * to the activity and potentially other fragments contained in that
         * activity.
         */
        public void onFileSelected(File file);
    }
}
