package com.giorgioaresu.batchrenamer;


import android.animation.Animator;
import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
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

import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the Callbacks
 * interface.
 */
public class Rule_ListFragment extends ListFragment implements MenuItem.OnMenuItemClickListener, RuleAdapter.ruleAdapter_Callbacks, RuleEdit_Fragment.ruleEditFragment_Callbacks {
    private static final String KEY_PREF_LAST_RULE_SET = "LastRuleSet";
    private static final String ARG_RULES = "rules";
    private static final String ARG_RULE = "rule";
    private static final String ARG_INDEX = "index";

    private static final long MOVE_DURATION = 250;
    private static final long FADE_DURATION = 250;
    private final SparseIntArray mItemIdTopMap = new SparseIntArray();

    private SharedPreferences sharedPrefs;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public Rule_ListFragment() {
    }

    public static Rule_ListFragment newInstance(ArrayList<Rule> rules) {
        ListFragment f = newInstance(new Rule_ListFragment(), rules);
        return (Rule_ListFragment) f;
    }

    public static ListFragment newInstance(ListFragment f, ArrayList<Rule> rules) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_RULES, rules);
        f.setArguments(args);
        return f;
    }

    public ArrayList<Rule> getRules() {
        ArrayAdapter adapter = (ArrayAdapter) getListAdapter();
        ArrayList<Rule> rules = new ArrayList<>();

        for (int i = 0; i < adapter.getCount(); ++i) {
            rules.add((Rule) adapter.getItem(i));
        }
        return rules;
    }

    public Set<String> getRulesAsJSONStrings() {
        ArrayAdapter adapter = (ArrayAdapter) getListAdapter();
        Set<String> rules = new HashSet<>();

        for (int i = 0; i < adapter.getCount(); ++i) {
            rules.add(((Rule) adapter.getItem(i)).dumpToJSON().toString());
        }
        return rules;
    }

    @Override
    public void onPause() {
        super.onPause();
        Set<String> rules = getRulesAsJSONStrings();
        if (sharedPrefs == null) {
            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        }
        sharedPrefs.edit().putStringSet(KEY_PREF_LAST_RULE_SET, rules).apply();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        List<Rule> mRules;

        if (getArguments() != null) {
            // Retrieve rules from arguments
            mRules = getArguments().getParcelableArrayList(ARG_RULES);
        } else if (savedInstanceState != null) {
            // Retrieve rules from saved state (ie. after rotation)
            mRules = savedInstanceState.getParcelableArrayList(ARG_RULES);
        } else {
            // Eventually populate rules for the first time
            Context context = getActivity();
            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            mRules = new ArrayList<>();
            // Check if we should start with empty list or load old rules
            if (sharedPrefs.getBoolean("remember_rules", true)) {
                Set<String> rules = sharedPrefs.getStringSet(KEY_PREF_LAST_RULE_SET, new HashSet<String>(0));
                for (String rule : rules) {
                    try {
                        JSONObject jObj = new JSONObject(rule);
                        Rule newRule = Rule.createFromJSON(context, jObj);
                        if (newRule != null) {
                            mRules.add(newRule);
                        }
                    } catch (Exception e) {
                        Debug.logError(getClass(), "failed to create JSONObject from string \"" + rule + "\"");
                    }
                }
            }
        }
        setListAdapter(new RuleAdapter(getActivity(), R.layout.rule_list_row, mRules, this));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(ARG_RULES, getRules());
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout
        LinearLayout fragmentLayout = (LinearLayout) inflater.inflate(R.layout.rule_list_fragment_content, container, false);

        /*// Set title
        View header = fragmentLayout.findViewById(R.id.file_list_header);
        if (header != null)
            ((TextView) header.findViewById(R.id.section_title_label)).setText(R.string.section_title_rulelist);*/

        return fragmentLayout;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        RuleEdit_Fragment ruleEdit_fragment = RuleEdit_Fragment.newInstance((Rule) getListAdapter().getItem(position));
        ruleEdit_fragment.setListener(this);
        ruleEdit_fragment.show(getFragmentManager(), "editRule");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.rulelist_fragment, menu);
        SubMenu subMenu = menu.findItem(R.id.action_newRule).getSubMenu();

        Map<String, String> rules = Rule.getRules(getActivity());

        for (String key : rules.keySet()) {
            subMenu.add(key)
                    .setOnMenuItemClickListener(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clearRules:
                if (getListAdapter().getCount() != 0) {
                    RuleAdapter adapter = (RuleAdapter) getListAdapter();
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList(ARG_RULES, getRules());

                    new UndoBarController(getActivity().findViewById(R.id.undobar), new UndoBarController.UndoListener() {
                        @Override
                        public void onUndo(Parcelable token) {
                            Bundle b = (Bundle) token;
                            List<Rule> rules = b.getParcelableArrayList(ARG_RULES);
                            RuleAdapter adapter = (RuleAdapter) getListAdapter();
                            adapter.clear();
                            adapter.addAll(rules);
                        }
                    }).showUndoBar(false, getString(R.string.action_clearRules_message), bundle);

                    ((RuleAdapter) getListAdapter()).clear();
                } else {
                    Toast.makeText(getActivity(), R.string.action_clearRules_emptymessage, Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Activity activity = getActivity();
        // Inflate appropriate class based on item title and add it to the list
        try {
            String rulesPackageName = getClass().getPackage().getName() + ".rules.";
            String className = rulesPackageName + Rule.getRules(activity).get(item.getTitle());
            Class<?> c = Class.forName(className);
            Constructor<?> cons = c.getConstructors()[0];
            Rule rule = (Rule) cons.newInstance(activity);
            RuleAdapter ruleAdapter = (RuleAdapter) getListAdapter();
            ruleAdapter.add(rule);
            return true;
        } catch (Exception b) {
            Toast.makeText(activity, getString(R.string.action_newRule_error), Toast.LENGTH_SHORT).show();
            Debug.logError("Exception handling item click, skipping", b);
            return false;
        }
    }

    /**
     * Given a file name process it applying all rules consecutively to get
     * the resulting new name
     *
     * @param fileName filename to be processed
     * @param position position of the file in the list
     * @param fileCount number of files in the list
     * @return resulting name
     */
    public String getNewName(String fileName, int position, int fileCount) {
        String res = fileName;
        try {
            RuleAdapter adapter = (RuleAdapter) getListAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                res = adapter.getItem(i).getNewName(res, position, fileCount);
            }
        } catch (ConcurrentModificationException ex) {
            // Rules are deleted while computing new name
            ex.printStackTrace();
        }
        return res;
    }

    /**
     * Checks if all rules return a valid status
     * @return true if all rules are valid, false if at least one is not valid
     */
    public boolean areAllRulesValid() {
        List<Rule> rules = getRules();
        for (Rule rule : rules) {
            if (!rule.isValid()) return false;
        }
        return true;
    }

    @Override
    public void notifyRuleDataSetChanged() {
        RuleAdapter ruleAdapter = (RuleAdapter) getListAdapter();
        ruleAdapter.notifyDataSetChanged();

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
        final RuleAdapter mAdapter = (RuleAdapter) getListAdapter();

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

        Rule removedRule = mAdapter.getItem(position);

        // Prepare and show undobar
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_INDEX, position);
        bundle.putParcelable(ARG_RULE, removedRule);
        new UndoBarController(getActivity().findViewById(R.id.undobar), new UndoBarController.UndoListener() {
            @Override
            public void onUndo(Parcelable token) {
                Bundle b = (Bundle) token;
                int index = b.getInt(ARG_INDEX);
                Rule rule = b.getParcelable(ARG_RULE);
                RuleAdapter adapter = (RuleAdapter) getListAdapter();
                adapter.insert(rule, index);
            }
        }).showUndoBar(false, getString(R.string.rule_generic_remove_message), bundle);

        // Delete the item from the adapter
        mAdapter.remove(removedRule);

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

    /**
     * Rule_ListFragment w/ horizontal padding
     */
    public static class withHorizontalPadding extends Rule_ListFragment {
        public withHorizontalPadding() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = super.onCreateView(inflater, container, savedInstanceState);
            View l = v.findViewById(android.R.id.list);
            Resources res = getResources();
            int vPadding = (int) res.getDimension(R.dimen.activity_vertical_margin);
            int hPadding = (int) res.getDimension(R.dimen.activity_horizontal_margin);
            l.setPadding(hPadding, vPadding, hPadding, vPadding);
            return v;
        }

        public static withHorizontalPadding newInstance(ArrayList<Rule> rules) {
            ListFragment f = newInstance(new withHorizontalPadding(), rules);
            return (withHorizontalPadding) f;
        }
    }
}

