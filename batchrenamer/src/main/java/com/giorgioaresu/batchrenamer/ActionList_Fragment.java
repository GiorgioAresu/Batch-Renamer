package com.giorgioaresu.batchrenamer;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.giorgioaresu.batchrenamer.actions.Add;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the Callbacks
 * interface.
 */
public class ActionList_Fragment extends ListFragment {

    private static final String ARG_ACTIONS = "actions";

    private ArrayList<Action> mActions;

    private OnActionSelectedListener mListener;

    public static ActionList_Fragment newInstance(ArrayList<Action> actions) {
        ActionList_Fragment fragment = new ActionList_Fragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_ACTIONS, actions);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ActionList_Fragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            // Retrieve actions from arguments
            mActions = getArguments().getParcelableArrayList(ARG_ACTIONS);
        } else if (savedInstanceState != null) {
            // Retrieve actions from saved state (ie. after rotation)
            mActions = savedInstanceState.getParcelableArrayList(ARG_ACTIONS);
        } else {
            // Populate actions for the first time
            mActions = new ArrayList<>();
            Context mContext = getActivity();
            mActions.add(new Add(mContext));
            mActions.add(new Add(mContext));
            mActions.add(new Add(mContext));
            mActions.add(new Add(mContext));
            mActions.add(new Add(mContext));
            mActions.add(new Add(mContext));
            mActions.add(new Add(mContext));
            mActions.add(new Add(mContext));
            mActions.add(new Add(mContext));
            mActions.add(new Add(mContext));
        }

        setListAdapter(new ActionAdapter(getActivity(), R.layout.action_list_row, mActions));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(ARG_ACTIONS, mActions);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout
        LinearLayout fragmentLayout = (LinearLayout) inflater.inflate(R.layout.action_list_fragment_content, container, false);

        /*// Set title
        View header = fragmentLayout.findViewById(R.id.file_list_header);
        if (header != null)
            ((TextView) header.findViewById(R.id.section_title_label)).setText(R.string.section_title_actionlist);*/

        return fragmentLayout;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mListener = (OnActionSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnActionSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onActionSelected(mActions.get(position));
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnActionSelectedListener {
        public void onActionSelected(Action action);
    }

}

