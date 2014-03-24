package com.giorgioaresu.batchrenamer.actions;

import android.content.Context;
import android.os.Parcel;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;

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


    public Add(Context context) {
        super(context, context.getString(R.string.action_add_title), R.layout.action_card_add);
    }

    public String getNewName(String currentName, int positionInSet) {
        return getNewName(currentName, positionInSet, applyTo);
    }

    @Override
    protected String getPatchedString(String string, int positionInSet) {
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
            EditText mPosition = (EditText) view.findViewById(R.id.action_add_index);
            position = Integer.valueOf(mPosition.getText().toString());
            CheckBox mBackward = (CheckBox) view.findViewById(R.id.action_add_backward);
            backward = mBackward.isChecked();
            RadioGroup mApplyTo = (RadioGroup) view.findViewById(R.id.action_radiogroup);
            applyTo = ApplyTo.getValue(mApplyTo.getCheckedRadioButtonId());
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

            EditText mPosition = (EditText) view.findViewById(R.id.action_add_index);
            mPosition.setText(String.valueOf(position));

            CheckBox mFromEnd = (CheckBox) view.findViewById(R.id.action_add_backward);
            mFromEnd.setChecked(backward);

            RadioGroup mApplyTo = (RadioGroup) view.findViewById(R.id.action_radiogroup);
            mApplyTo.check(applyTo.getID());
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
        str = context.getString(R.string.action_add_text) + ": " + checkForEmpty(text) + ". "
                + context.getString(R.string.action_add_index) + ": " + checkForEmpty(String.valueOf(position)) + ". "
                + context.getString(R.string.action_add_backward) + ": "
                + context.getString(backward ? R.string.true_ : R.string.false_) + ". "
                + context.getString(R.string.action_apply) + ": " + context.getString(ApplyTo.getStringResource(applyTo));
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
        backward = in.readByte() != 0 ? true : false;
        applyTo = ApplyTo.getValue(in.readInt());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dumpToParcel(Parcel parcel, int i) {
        parcel.writeString(text);
        parcel.writeInt(position);
        parcel.writeByte((byte) (backward ? 1 : 0));
        parcel.writeInt(applyTo.getID());
    }
}
