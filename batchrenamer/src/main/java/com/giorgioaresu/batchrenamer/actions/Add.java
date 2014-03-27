package com.giorgioaresu.batchrenamer.actions;

import android.app.Activity;
import android.os.Parcel;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.giorgioaresu.batchrenamer.Action;
import com.giorgioaresu.batchrenamer.R;

import org.json.JSONException;
import org.json.JSONObject;

public class Add extends Action {
    static final String KEY_TEXT = "Text";
    static final String KEY_POSITION = "Position";
    static final String KEY_BACKWARD = "Backward";
    static final String KEY_APPLYTO = "ApplyTo";

    String text = "";
    int position = 0;
    boolean backward = false;
    ApplyTo applyTo = ApplyTo.BOTH;


    public Add(Activity context) {
        super(context, context.getString(R.string.actioncard_add_title), R.layout.action_card_add);
    }

    public String getNewName(String currentName, int positionInSet, int setSize) {
        return getNewName(currentName, positionInSet, setSize, applyTo);
    }

    @Override
    protected String getPatchedString(String string, int positionInSet, int setSize) {
        // Compute right index
        int pos;
        if (backward) {
            // Right index or 0 if out of bounds
            pos = Math.max(string.length() - position, 0);
        } else {
            // Right index or last one if out of bounds
            pos = Math.min(string.length(), position);
        }

        return string.substring(0, pos).concat(text).concat(string.substring(pos));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateDataFromView(View view) {
        try {
            EditText mText = (EditText) view.findViewById(R.id.action_add_text);
            text = mText.getText().toString();

            EditText mPosition = (EditText) view.findViewById(R.id.action_position);
            position = Integer.valueOf(mPosition.getText().toString());

            CheckBox mBackward = (CheckBox) view.findViewById(R.id.action_position_backward);
            backward = mBackward.isChecked();

            Spinner mApplyTo = (Spinner) view.findViewById(R.id.action_apply_spinner);
            applyTo = ApplyTo.getValue(mApplyTo.getSelectedItemPosition());
        } catch (Exception e) {
            Log.e("updateDataFromView", "NPE");
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateViewFromData(View view) {
        try {
            EditText mText = (EditText) view.findViewById(R.id.action_add_text);
            mText.setText(text);

            EditText mPosition = (EditText) view.findViewById(R.id.action_position);
            mPosition.setText(String.valueOf(position));

            CheckBox mBackward = (CheckBox) view.findViewById(R.id.action_position_backward);
            mBackward.setChecked(backward);

            Spinner mApplyTo = (Spinner) view.findViewById(R.id.action_apply_spinner);
            mApplyTo.setSelection(applyTo.getID());
        } catch (Exception e) {
            Log.e("updateViewFromData", "NPE");
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getContentDescription() {
        String str;
        str = context.getString(R.string.actioncard_add_text) + ": " + checkForEmpty(text) + ". "
                + context.getString(R.string.actioncard_position) + ": " + checkForEmpty(String.valueOf(position)) + ". "
                + context.getString(R.string.actioncard_position_backward) + ": " + getValueToString(backward) + ". "
                + context.getString(R.string.actioncard_apply) + ": " + ApplyTo.getLabel(context, applyTo);
        return str;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JSONObject serializeToJSON() throws JSONException {
        JSONObject jObject = new JSONObject();
        jObject.put(KEY_TEXT, text);
        jObject.put(KEY_POSITION, position);
        jObject.put(KEY_BACKWARD, backward);
        jObject.put(KEY_APPLYTO, applyTo);
        return jObject;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void deserializeFromJSON(JSONObject jObject) throws JSONException {
        text = jObject.getString(KEY_TEXT);
        position = jObject.getInt(KEY_POSITION);
        backward = jObject.getBoolean(KEY_BACKWARD);
        applyTo = ApplyTo.getValue(jObject.getInt(KEY_APPLYTO));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createFromParcel(Parcel in) {
        text = in.readString();
        position = in.readInt();
        backward = toBoolean(in.readByte());
        applyTo = ApplyTo.getValue(in.readInt());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dumpToParcel(Parcel parcel, int i) {
        parcel.writeString(text);
        parcel.writeInt(position);
        parcel.writeByte(toByte(backward));
        parcel.writeInt(applyTo.getID());
    }
}
