package com.giorgioaresu.batchrenamer.actions;

import android.content.Context;
import android.os.Parcel;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.giorgioaresu.batchrenamer.Action;
import com.giorgioaresu.batchrenamer.R;

import org.json.JSONException;
import org.json.JSONObject;

public class Replace extends Action {
    static final String KEY_TEXT = "Text";
    static final String KEY_WITH = "With";
    static final String KEY_APPLYTO = "ApplyTo";

    String text = "";
    String with = "";
    ApplyTo applyTo = ApplyTo.BOTH;


    public Replace(Context context) {
        super(context, context.getString(R.string.action_replace_title), R.layout.action_card_replace);
    }

    public String getNewName(String currentName, int positionInSet, int setSize) {
        return getNewName(currentName, positionInSet, setSize, applyTo);
    }

    @Override
    protected String getPatchedString(String string, int positionInSet, int setSize) {
        return string.replaceAll(text, with);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateDataFromView(View view) {
        try {
            EditText mText = (EditText) view.findViewById(R.id.action_replace_text);
            text = mText.getText().toString();

            EditText mWith = (EditText) view.findViewById(R.id.action_replace_with);
            with = mWith.getText().toString();

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
            EditText mText = (EditText) view.findViewById(R.id.action_replace_text);
            mText.setText(text);

            EditText mWith = (EditText) view.findViewById(R.id.action_replace_with);
            mWith.setText(with);

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
        str = context.getString(R.string.action_replace_text) + ": " + checkForEmpty(text) + ". "
                + context.getString(R.string.action_replace_with) + ": " + checkForEmpty(with) + ". "
                + context.getString(R.string.action_apply) + ": " + ApplyTo.getLabel(context, applyTo);
        return str;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JSONObject serializeToJSON() throws JSONException {
        JSONObject jObject = new JSONObject();
        jObject.put(KEY_TEXT, text);
        jObject.put(KEY_WITH, with);
        jObject.put(KEY_APPLYTO, applyTo);
        return jObject;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void deserializeFromJSON(JSONObject jObject) throws JSONException {
        text = jObject.getString(KEY_TEXT);
        with = jObject.getString(KEY_WITH);
        applyTo = ApplyTo.getValue(jObject.getInt(KEY_APPLYTO));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createFromParcel(Parcel in) {
        text = in.readString();
        with = in.readString();
        applyTo = ApplyTo.getValue(in.readInt());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dumpToParcel(Parcel parcel, int i) {
        parcel.writeString(text);
        parcel.writeString(with);
        parcel.writeInt(applyTo.getID());
    }
}
