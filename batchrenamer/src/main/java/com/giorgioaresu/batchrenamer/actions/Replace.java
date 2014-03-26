package com.giorgioaresu.batchrenamer.actions;

import android.content.Context;
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

public class Replace extends Action {
    static final String KEY_TARGET = "Target";
    static final String KEY_REGEX = "Regex";
    static final String KEY_REPLACEMENT = "Replacement";
    static final String KEY_APPLYTO = "ApplyTo";

    String target = "";
    Boolean regex = false;
    String replacement = "";
    ApplyTo applyTo = ApplyTo.BOTH;


    public Replace(Context context) {
        super(context, context.getString(R.string.action_replace_title), R.layout.action_card_replace);
    }

    public String getNewName(String currentName, int positionInSet, int setSize) {
        return getNewName(currentName, positionInSet, setSize, applyTo);
    }

    @Override
    protected String getPatchedString(String string, int positionInSet, int setSize) {
        return string.replaceAll(target, replacement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateDataFromView(View view) {
        try {
            EditText mTarget = (EditText) view.findViewById(R.id.action_replace_target);
            target = mTarget.getText().toString();

            CheckBox mRegex = (CheckBox) view.findViewById(R.id.action_replace_regex);
            regex = mRegex.isChecked();

            EditText mReplacement = (EditText) view.findViewById(R.id.action_replace_replacement);
            replacement = mReplacement.getText().toString();

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
            EditText mTarget = (EditText) view.findViewById(R.id.action_replace_target);
            mTarget.setText(target);

            CheckBox mRegex = (CheckBox) view.findViewById(R.id.action_replace_regex);
            mRegex.setChecked(regex);

            EditText mReplacement = (EditText) view.findViewById(R.id.action_replace_replacement);
            mReplacement.setText(replacement);

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
        str = context.getString(R.string.action_replace_target) + ": " + checkForEmpty(target) + ". "
                + context.getString(R.string.action_regex) + ": " + getValueToString(regex) + ". "
                + context.getString(R.string.action_replace_replacement) + ": " + checkForEmpty(replacement) + ". "
                + context.getString(R.string.action_apply) + ": " + ApplyTo.getLabel(context, applyTo);
        return str;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JSONObject serializeToJSON() throws JSONException {
        JSONObject jObject = new JSONObject();
        jObject.put(KEY_TARGET, target);
        jObject.put(KEY_REGEX, regex);
        jObject.put(KEY_REPLACEMENT, replacement);
        jObject.put(KEY_APPLYTO, applyTo);
        return jObject;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void deserializeFromJSON(JSONObject jObject) throws JSONException {
        target = jObject.getString(KEY_TARGET);
        regex = jObject.getBoolean(KEY_REGEX);
        replacement = jObject.getString(KEY_REPLACEMENT);
        applyTo = ApplyTo.getValue(jObject.getInt(KEY_APPLYTO));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createFromParcel(Parcel in) {
        target = in.readString();
        regex = toBoolean(in.readByte());
        replacement = in.readString();
        applyTo = ApplyTo.getValue(in.readInt());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dumpToParcel(Parcel parcel, int i) {
        parcel.writeString(target);
        parcel.writeByte(toByte(regex));
        parcel.writeString(replacement);
        parcel.writeInt(applyTo.getID());
    }
}
