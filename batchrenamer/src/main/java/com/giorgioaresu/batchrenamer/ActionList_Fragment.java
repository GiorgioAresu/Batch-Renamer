package com.giorgioaresu.batchrenamer;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.giorgioaresu.batchrenamer.actions.Add;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the Callbacks
 * interface.
 */
public class ActionList_Fragment extends ListFragment implements ActionEdit_Fragment.actionEditFragment_Callbacks{

    private static final String ARG_ACTIONS = "actions";

    private static final int ID_NEW_ACTION_ADD = 1;

    private ArrayList<Action> mActions;

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
            // Eventually populate actions for the first time
            Context mContext = getActivity();
            mActions = new ArrayList<>();
            for (int i = 1; i<10; i++) {
                mActions.add(new Add(mContext));
            }
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
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ActionEdit_Fragment actionEdit_fragment = ActionEdit_Fragment.newInstance(mActions.get(position));
        actionEdit_fragment.setListener(this);
        actionEdit_fragment.show(getFragmentManager(), "editAction");
    }

    public void populateActionMenu(SubMenu subMenu) {
        subMenu.add(0, ID_NEW_ACTION_ADD, 0, R.string.action_add_title);
    }

    public boolean onNewActionSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case ID_NEW_ACTION_ADD:
                mActions.add(new Add(getActivity()));
                ActionAdapter actionAdapter = (ActionAdapter) getListAdapter();
                actionAdapter.notifyDataSetChanged();
                return true;
            default:
                return false;
        }
    }

    public String getNewName(String fileName) {
        String res = fileName;
        try {
            for (Action action : mActions) {
                res = action.getNewName(res);
            }
        } catch (ConcurrentModificationException ex) {
            // Actions are deleted while computing new name
            ex.printStackTrace();
        }
        return res;
    }

    @Override
    public void notifyActionDataSetChanged() {
        ActionAdapter actionAdapter = (ActionAdapter) getListAdapter();
        actionAdapter.notifyDataSetChanged();
    }
}

