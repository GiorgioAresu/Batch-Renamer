package com.giorgioaresu.batchrenamer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import java.lang.reflect.Constructor;

public class ActionEdit_Fragment extends DialogFragment {
    /**
     * The system calls this only when creating the layout in a dialog.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        int layout = 0;

        try {
            Class<?> c = Class.forName(getTag());
            Constructor<?> cons = c.getConstructors()[0];
            Action action = (Action) cons.newInstance(getActivity());
            layout = action.getEditViewId();
        } catch (Exception b) {
            Log.e("onCreateDialog", "Exception retrieving action layout");
        }

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(layout, null))
                // Add action buttons
                .setPositiveButton(R.string.action_confirmedit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...
                        Toast.makeText(getActivity(), "OK", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.action_discardedit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ActionEdit_Fragment.this.getDialog().cancel();
                        Toast.makeText(getActivity(), "Cancel", Toast.LENGTH_SHORT).show();
                    }
                });
        return builder.create();
    }
}
