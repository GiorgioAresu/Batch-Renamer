package com.giorgioaresu.batchrenamer.actions;

import android.content.Context;
import android.os.Parcel;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.giorgioaresu.batchrenamer.Action;
import com.giorgioaresu.batchrenamer.R;

import org.json.JSONException;
import org.json.JSONObject;

public class Add extends Action {
    static final String KEY_TEXT = "Text";
    static final String KEY_POSITION = "Position";
    static final String KEY_BACKWARD = "Backward";

    Context context;
    String text = "";
    int position = 0;
    boolean backward = false;


    public Add(Context context) {
        super(context, context.getString(R.string.action_add_title), R.layout.action_card_add);
        this.context = context;
    }

    public String getNewName(String currentName) {
        // Compute right index
        int pos;
        if (backward) {
            // Right index or 0 if out of bounds
            pos = Math.max(currentName.length() - position, 0);
        } else {
            // Right index or last one if out of bounds
            pos = Math.min(currentName.length(), position);
        }

        return currentName.substring(0, pos).concat(text).concat(currentName.substring(pos));
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
            CheckBox mFromEnd = (CheckBox) view.findViewById(R.id.action_add_fromend);
            backward = mFromEnd.isChecked();
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
            // Need to change InputType otherwise setText has no effect
            mPosition.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            mPosition.setText(String.valueOf(position));
            mPosition.setRawInputType(InputType.TYPE_CLASS_NUMBER);

            CheckBox mFromEnd = (CheckBox) view.findViewById(R.id.action_add_fromend);
            mFromEnd.setChecked(backward);
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
                + context.getString(backward ? R.string.true_ : R.string.false_) + ". ";
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createFromParcel(Parcel in) {
        text = in.readString();
        position = in.readInt();
        backward = in.readByte() != 0 ? true : false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(text);
        parcel.writeInt(position);
        parcel.writeByte((byte) (backward ? 1 : 0));
    }
}
