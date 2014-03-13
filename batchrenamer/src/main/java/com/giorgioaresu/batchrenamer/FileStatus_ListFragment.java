package com.giorgioaresu.batchrenamer;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A fragment representing a list of file statuses.
 */
public class FileStatus_ListFragment extends File_ListFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FileStatus_ListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setItemLayout(R.layout.filestatus_list_row);
        super.onCreate(savedInstanceState);

        // TODO: remove this
        if (getFiles().isEmpty())
            for (int i = 0; i < 50; i++)
                add(new File(Uri.parse("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip")));
        ((FileAdapter) getListAdapter()).notifyDataSetChanged();

        /*if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setFragmentLayout(R.layout.filestatus_list_fragment_content);
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
