package com.giorgioaresu.batchrenamer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

public class RuleEdit_Fragment extends DialogFragment implements DialogInterface.OnClickListener {
    // Used to store/retrieve elements to/from bundles
    static final String KEY_RULE = "RULE";

    private ruleEditFragment_Callbacks mListener;

    private View dialogView;

    private static boolean isShowing = false;

    @Override
    public void show(FragmentManager manager, String tag) {
        if (!isShowing) {
            isShowing = true;
            super.show(manager, tag);
        }
    }

    static RuleEdit_Fragment newInstance(Rule rule) {
        RuleEdit_Fragment f = new RuleEdit_Fragment();

        // Supply rule input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(KEY_RULE, rule);
        f.setArguments(args);

        return f;
    }

    /**
     * The system calls this only when creating the layout in a dialog.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Retrieve associated rule (the one the user selected)
        Rule mRule = getArguments().getParcelable(KEY_RULE);

        if (mRule == null) {
            Debug.log("mRule is null");
        }

        // Inflate layout
        // Pass null as the parent view because its going in the dialog layout
        dialogView = inflater.inflate(R.layout.rule_edit, null);

        // Set title
        ((TextView) dialogView.findViewById(R.id.rule_title)).setText(mRule.getTitle());

        // Expand ViewStub with corresponding layout
        ViewStub viewStub = (ViewStub) dialogView.findViewById(R.id.rule_edit_contentViewStub);
        viewStub.setLayoutResource(mRule.getViewId());
        View cardLayout = viewStub.inflate();
        // Let the rule do its stuff
        mRule.onInflate(cardLayout);
        // Fill card with data
        mRule.updateViewFromData(cardLayout);

        // Set dialog layout
        builder.setView(dialogView)
                // Add rule buttons
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, this);

        return builder.create();
    }

    public void setListener(ruleEditFragment_Callbacks listener) {
        mListener = listener;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (mListener == null) {
            // If listener hasn't been set, make sure that the container activity has implemented
            // the callback interface. If not, it throws an exception
            try {
                FragmentManager fragmentManager = activity.getFragmentManager();
                mListener = (ruleEditFragment_Callbacks) fragmentManager.findFragmentById(R.id.rule_fragment);
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement OnRuleSelectedListener");
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case DialogInterface.BUTTON_POSITIVE:
                Rule mRule = getArguments().getParcelable(KEY_RULE);
                mRule.updateDataFromView(dialogView);
                mListener.notifyRuleDataSetChanged();
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                RuleEdit_Fragment.this.getDialog().cancel();
                break;
        }
        isShowing = false;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        isShowing = false;
        super.onDismiss(dialog);
    }

    public interface ruleEditFragment_Callbacks {
        /**
         * Notify that rules DataSet has changed, ie. after closing the
         * dialog used to modify rules
         */
        public void notifyRuleDataSetChanged();
    }
}
