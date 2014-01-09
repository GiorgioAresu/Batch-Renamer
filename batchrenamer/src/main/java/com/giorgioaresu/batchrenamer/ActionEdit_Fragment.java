package com.giorgioaresu.batchrenamer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

public class ActionEdit_Fragment extends DialogFragment implements DialogInterface.OnClickListener {
    // Used to store/retrieve elements to/from bundles
    static final String keyAction = "ACTION";

    private actionEditFragment_Callbacks mListener;

    private View dialogView;

    private static boolean isShowing = false;

    @Override
    public void show(FragmentManager manager, String tag) {
        if (!isShowing) {
            isShowing = true;
            super.show(manager, tag);
        }
    }

    static ActionEdit_Fragment newInstance(Action action) {
        ActionEdit_Fragment f = new ActionEdit_Fragment();

        // Supply action input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(keyAction, action);
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

        // Retrieve associated action (the one the user selected)
        Action mAction = getArguments().getParcelable(keyAction);

        if (mAction == null) {
            Log.e("onCreateDialog", "mAction is null");
        }

        // Inflate layout
        // Pass null as the parent view because its going in the dialog layout
        dialogView = inflater.inflate(R.layout.action_edit, null);

        // Set title
        ((TextView) dialogView.findViewById(R.id.action_title)).setText(mAction.getTitle());

        // Expand ViewStub with corresponding layout
        ViewStub viewStub = (ViewStub) dialogView.findViewById(R.id.action_edit_contentViewStub);
        viewStub.setLayoutResource(mAction.getViewId());
        View cardLayout = viewStub.inflate();
        // Fill card with data
        mAction.updateViewFromData(cardLayout);

        // Set dialog layout
        builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton(R.string.action_confirmedit, this)
                .setNegativeButton(R.string.action_discardedit, this);

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mListener = (actionEditFragment_Callbacks) activity;
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
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case DialogInterface.BUTTON_POSITIVE:
                // TODO: Validate changes
                Action mAction = getArguments().getParcelable(keyAction);
                mAction.updateDataFromView(dialogView);
                mListener.notifyDataSetChanged();
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                ActionEdit_Fragment.this.getDialog().cancel();
                break;
        }
        isShowing = false;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        isShowing = false;
        super.onDismiss(dialog);
    }

    public interface actionEditFragment_Callbacks {
        public void notifyDataSetChanged();
    }
}
