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

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the Callbacks
 * interface.
 */
public class FileList_Fragment extends ListFragment {

    private static final String ARG_FILES = "files";

    private ArrayList<File> mFiles;

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
            mFiles = getArguments().getParcelableArrayList(ARG_FILES);
        } else {
            mFiles = mInterface.provideFiles();
        }

        setListAdapter(new FileAdapter(getActivity(), R.layout.file_list_row, mFiles));
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

        /**
         * This method allow to provide an initial set of files to the fragment.
         *
         * @return an ArrayList of files
         */
        public ArrayList<File> provideFiles();
    }
}
