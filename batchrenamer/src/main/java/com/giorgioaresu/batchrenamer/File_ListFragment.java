package com.giorgioaresu.batchrenamer;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;

public abstract class File_ListFragment extends ListFragment {
    private FileFragmentInterface mInterface;
    private static final String ARG_FILES = "files";
    private ArrayList<File> mFiles = new ArrayList<>();

    /**
     * Used to synchronize all writings of mFiles
     */
    private final Object mLock = new Object();

    /**
     * Default item layout for fragment
     */
    private int defaultItemLayout = R.layout.filepreview_list_row;

    /**
     * Default layout for fragment
     */
    private int defaultFragmentLayout = R.layout.filepreview_list_fragment_content;

    public void setItemLayout(int itemLayout) {
        this.itemLayout = itemLayout;
    }

    private int itemLayout = -1;

    public void setFragmentLayout(int fragmentLayout) {
        this.fragmentLayout = fragmentLayout;
    }

    private int fragmentLayout = -1;

    public static File_ListFragment newInstance(ArrayList<File> files) {
        File_ListFragment fragment = new FilePreview_ListFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_FILES, files);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Classes can call {@code setItemLayout} before super.onCreate to supply layout resource for children
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            //Retrieve files from arguments
            mFiles = getArguments().getParcelableArrayList(ARG_FILES);
        } else if (savedInstanceState != null) {
            // Retrieve rules from saved state (ie. after rotation)
            mFiles = savedInstanceState.getParcelableArrayList(ARG_FILES);
        } else {
            // Eventually populate files for the first time
        }
        int layout = defaultItemLayout;
        if (itemLayout != -1) {
            layout = itemLayout;
        }
        setListAdapter(new FileAdapter(getActivity(), layout, mFiles));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(ARG_FILES, mFiles);
        super.onSaveInstanceState(outState);
    }

    public void clear() {
        synchronized (mLock) {
            mFiles.clear();
        }
    }

    public void add(File file) {
        synchronized (mLock) {
            mFiles.add(file);
        }
    }

    public File get(int index) {
        return mFiles.get(index);
    }

    public ArrayList<File> getFiles() {
        return mFiles;
    }


    /**
     * Classes can call {@code setFragmentLayout} before super.onCreateView to supply layout resource for children
     *
     * @param savedInstanceState
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout
        int layout = defaultFragmentLayout;
        if (fragmentLayout != -1) {
            layout = fragmentLayout;
        }
        LinearLayout fragmentLayout = (LinearLayout) inflater.inflate(layout, container, false);

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
            mInterface.onFileSelected(get(position));
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
