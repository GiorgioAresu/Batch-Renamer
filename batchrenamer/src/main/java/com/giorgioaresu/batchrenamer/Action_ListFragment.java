package com.giorgioaresu.batchrenamer;


import android.animation.Animator;
import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.giorgioaresu.batchrenamer.actions.Add;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the Callbacks
 * interface.
 */
public class Action_ListFragment extends ListFragment implements MenuItem.OnMenuItemClickListener, ActionAdapter.actionAdapter_Callbacks, ActionEdit_Fragment.actionEditFragment_Callbacks {

    private static final String ARG_ACTIONS = "actions";
    private static final String ARG_ACTION = "action";
    private static final String ARG_INDEX = "index";

    private static final int ID_NEW_ACTION_ADD = 1;

    public ArrayList<Action> getActions() {
        ArrayAdapter adapter = (ArrayAdapter) getListAdapter();
        ArrayList<Action> actions = new ArrayList<>();

        for (int i = 0; i < adapter.getCount(); ++i) {
            actions.add((Action) adapter.getItem(i));
        }
        return actions;
    }

    private final SparseIntArray mItemIdTopMap = new SparseIntArray();

    private static final long MOVE_DURATION = 250;
    private static final long FADE_DURATION = 250;

    public static Action_ListFragment newInstance(ArrayList<Action> actions) {
        Action_ListFragment fragment = new Action_ListFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_ACTIONS, actions);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public Action_ListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        ArrayList<Action> mActions;

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
            for (int i = 0; i < 0; i++) {
                mActions.add(new Add(mContext));
            }
        }
        setListAdapter(new ActionAdapter(getActivity(), R.layout.action_list_row, mActions, this));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(ARG_ACTIONS, getActions());
        super.onSaveInstanceState(outState);
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

        ActionEdit_Fragment actionEdit_fragment = ActionEdit_Fragment.newInstance((Action) getListAdapter().getItem(position));
        actionEdit_fragment.setListener(this);
        actionEdit_fragment.show(getFragmentManager(), "editAction");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.actionlist_fragment, menu);
        SubMenu subMenu = menu.findItem(R.id.action_newAction).getSubMenu();

        if (subMenu != null) {
            for (int i = 0; i < subMenu.size(); i++) {
                subMenu.getItem(i).setOnMenuItemClickListener(this);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clearActions:
                if (getListAdapter().getCount() != 0) {
                    ActionAdapter adapter = (ActionAdapter) getListAdapter();
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList(ARG_ACTIONS, getActions());

                    new UndoBarController(getActivity().findViewById(R.id.undobar), new UndoBarController.UndoListener() {
                        @Override
                        public void onUndo(Parcelable token) {
                            Bundle b = (Bundle) token;
                            ArrayList<Action> actions = b.getParcelableArrayList(ARG_ACTIONS);
                            ActionAdapter adapter = (ActionAdapter) getListAdapter();
                            adapter.clear();
                            adapter.addAll(actions);
                        }
                    }).showUndoBar(false, getString(R.string.action_clearActions_message), bundle);

                    ((ActionAdapter) getListAdapter()).clear();
                } else {
                    Toast.makeText(getActivity(), R.string.action_clearActions_emptymessage, Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        // Inflate appropriate class based on item title and add it to the list
        try {
            Activity activity = getActivity();
            String className = activity.getPackageName() + ".actions." + item.getTitle().toString();
            Class<?> c = Class.forName(className);
            Constructor<?> cons = c.getConstructors()[0];
            Action action = (Action) cons.newInstance(activity);
            ActionAdapter actionAdapter = (ActionAdapter) getListAdapter();
            actionAdapter.add(action);
            return true;
        } catch (Exception b) {
            Log.e(getClass().getSimpleName(), "Exception handling item click, skipping");
            return false;
        }
    }

    /**
     * Given a file name process it applying all actions consecutively to get
     * the resulting new name
     *
     * @param fileName filename to be processed
     * @return resulting name
     */
    public String getNewName(String fileName) {
        String res = fileName;
        try {
            ActionAdapter adapter = (ActionAdapter) getListAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                res = adapter.getItem(i).getNewName(res, i);
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

    /**
     * Finds the ListView's child item containing the view passed
     *
     * @param view descendant of a ListView's child
     * @return ListView's child or null if view doesn't belong to listview
     */
    public View getAncestorOfView(View view) {
        View listItem = view;
        try {
            View v;
            while (!(v = (View) listItem.getParent()).equals(getListView())) {
                listItem = v;
            }
        } catch (ClassCastException e) {
            // We made it up to the window without find this list view
            return null;
        }
        return listItem;
    }

    /**
     * Set the enabled state of this view and all of its children
     *
     * @param view
     * @param enabled true to enable, false to disable
     */
    private void setViewEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;

            for (int idx = 0; idx < group.getChildCount(); idx++) {
                setViewEnabled(group.getChildAt(idx), enabled);
            }
        }
    }

    @Override
    public void animateItemDeletion(View v) {
        final ListView lv = getListView();
        final View view = getAncestorOfView(v);

        long duration = (int) (view.getAlpha() * FADE_DURATION);
        setViewEnabled(lv, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.animate().setDuration(duration).alpha(0).withEndAction(new Runnable() {
                @Override
                public void run() {
                    // Restore animated values
                    view.setAlpha(1);
                    animateRemoval(lv, view);
                }
            });
        } else {
            view.animate().setDuration(duration).alpha(0).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    // Restore animated values
                    view.setAlpha(1);
                    animateRemoval(lv, view);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        }
    }

    /**
     * This method animates all other views in the ListView container (not including ignoreView)
     * into their final positions. It is called after ignoreView has been removed from the
     * adapter, but before layout has been run. The approach here is to figure out where
     * everything is now, then allow layout to run, then figure out where everything is after
     * layout, and then to run animations between all of those start/end positions.
     */
    private void animateRemoval(final ListView listview, View viewToRemove) {
        final ActionAdapter mAdapter = (ActionAdapter) getListAdapter();

        // Get position of element in the ListArray
        int position = listview.getPositionForView(viewToRemove);

        int firstVisiblePosition = listview.getFirstVisiblePosition();
        for (int i = 0; i < listview.getChildCount(); ++i) {
            View child = listview.getChildAt(i);
            if (child != viewToRemove) {
                int pos = firstVisiblePosition + i;
                if (pos > position) {
                    // Adjust positions to correspond when item will be removed
                    pos--;
                }
                mItemIdTopMap.put(pos, child.getTop());
            }
        }

        Action removedAction = mAdapter.getItem(position);

        // Prepare and show undobar
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_INDEX, position);
        bundle.putParcelable(ARG_ACTION, removedAction);
        new UndoBarController(getActivity().findViewById(R.id.undobar), new UndoBarController.UndoListener() {
            @Override
            public void onUndo(Parcelable token) {
                Bundle b = (Bundle) token;
                int index = b.getInt(ARG_INDEX);
                Action action = b.getParcelable(ARG_ACTION);
                ActionAdapter adapter = (ActionAdapter) getListAdapter();
                adapter.insert(action, index);
            }
        }).showUndoBar(false, getString(R.string.action_remove_message), bundle);

        // Delete the item from the adapter
        mAdapter.remove(removedAction);

        final ViewTreeObserver observer = listview.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                observer.removeOnPreDrawListener(this);
                boolean firstAnimation = true;
                int firstVisiblePosition = listview.getFirstVisiblePosition();
                for (int i = 0; i < listview.getChildCount(); ++i) {
                    final View child = listview.getChildAt(i);
                    int position = firstVisiblePosition + i;
                    Integer startTop = mItemIdTopMap.get(position);
                    int top = child.getTop();
                    if (startTop != null) {
                        if (startTop != top) {
                            int delta = startTop - top;
                            child.setTranslationY(delta);
                            child.animate().setDuration(MOVE_DURATION).translationY(0);
                            if (firstAnimation) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    child.animate().withEndAction(new Runnable() {
                                        public void run() {
                                            setViewEnabled(getListView(), true);
                                        }
                                    });
                                } else {
                                    child.animate().setListener(new Animator.AnimatorListener() {
                                        @Override
                                        public void onAnimationStart(Animator animator) {

                                        }

                                        @Override
                                        public void onAnimationEnd(Animator animator) {
                                            setViewEnabled(getListView(), true);
                                        }

                                        @Override
                                        public void onAnimationCancel(Animator animator) {

                                        }

                                        @Override
                                        public void onAnimationRepeat(Animator animator) {

                                        }
                                    });
                                }
                                firstAnimation = false;
                            }
                        }
                    } else {
                        // Animate new views along with the others. The catch is that they did not
                        // exist in the start state, so we must calculate their starting position
                        // based on neighboring views.
                        int childHeight = child.getHeight() + listview.getDividerHeight();
                        startTop = top + (i > 0 ? childHeight : -childHeight);
                        int delta = startTop - top;
                        child.setTranslationY(delta);
                        child.animate().setDuration(MOVE_DURATION).translationY(0);
                        if (firstAnimation) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                child.animate().withEndAction(new Runnable() {
                                    public void run() {
                                        setViewEnabled(getListView(), true);
                                    }
                                });
                            } else {
                                child.animate().setListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animator) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animator) {
                                        setViewEnabled(getListView(), true);
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animator) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animator) {

                                    }
                                });
                            }
                            firstAnimation = false;
                        }
                    }
                }
                if (firstAnimation) {
                    // If we are here it means that for has not run, so there were no child to
                    // animate, so we need to re-enable listView
                    setViewEnabled(getListView(), true);
                }
                mItemIdTopMap.clear();
                return true;
            }
        });
    }
}
