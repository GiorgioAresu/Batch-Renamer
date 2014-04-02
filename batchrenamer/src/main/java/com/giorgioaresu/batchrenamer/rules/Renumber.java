package com.giorgioaresu.batchrenamer.rules;

import android.content.Context;
import android.os.Parcel;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.giorgioaresu.batchrenamer.Rule;
import com.giorgioaresu.batchrenamer.Debug;
import com.giorgioaresu.batchrenamer.R;

import org.json.JSONException;
import org.json.JSONObject;

public class Renumber extends Rule {
    static final String KEY_START = "Start";
    static final String KEY_STEP = "Step";
    static final String KEY_PADMODE = "PadMode";
    static final String KEY_PADDING = "Padding";
    static final String KEY_POSITION = "Position";
    static final String KEY_BACKWARD = "Backward";
    static final String KEY_APPLYTO = "ApplyTo";

    int start = 1;
    int step = 1;
    PadMode padMode = PadMode.AUTO;
    int padding = 1;
    int position = 0;
    boolean backward = false;
    ApplyTo applyTo = ApplyTo.BOTH;


    public Renumber(Context context) {
        super(context, context.getString(R.string.rule_renumber_title), R.layout.rule_card_renumber);
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

        int number = start + step * positionInSet;
        String paddedNumber;
        switch (padMode) {
            case AUTO:
                // Compute maximum number of digits
                int lenght = 1;
                int max = start + step * setSize;
                while((max/=10) != 0) ++lenght;
                
                paddedNumber = String.format("%0" + lenght + "d", number);
                break;
            case MANUAL:
                if (padding > 0) {
                    paddedNumber = String.format("%0" + padding + "d", number);
                } else {
                    paddedNumber = String.valueOf(number);
                }
                break;
            default:
                paddedNumber = String.valueOf(number);
        }

        return string.substring(0, pos).concat(paddedNumber).concat(string.substring(pos));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateDataFromView(View view) {
        try {
            EditText mStart = (EditText) view.findViewById(R.id.rule_renumber_start);
            start = Integer.parseInt(mStart.getText().toString());

            EditText mStep = (EditText) view.findViewById(R.id.rule_renumber_step);
            step = Integer.parseInt(mStep.getText().toString());

            Spinner mPadMode = (Spinner) view.findViewById(R.id.rule_renumber_padding_spinner);
            padMode = PadMode.getValue(mPadMode.getSelectedItemPosition());

            EditText mPadding = (EditText) view.findViewById(R.id.rule_renumber_padding_amount);
            padding = Integer.parseInt(mPadding.getText().toString());

            EditText mPosition = (EditText) view.findViewById(R.id.rule_position);
            position = Integer.parseInt(mPosition.getText().toString());

            CheckBox mBackward = (CheckBox) view.findViewById(R.id.rule_position_backward);
            backward = mBackward.isChecked();

            Spinner mApplyTo = (Spinner) view.findViewById(R.id.rule_apply_spinner);
            applyTo = ApplyTo.getValue(mApplyTo.getSelectedItemPosition());
        } catch (Exception e) {
            Debug.logError(getClass(), "NPE updating from view");
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
            EditText mStart = (EditText) view.findViewById(R.id.rule_renumber_start);
            mStart.setText(String.valueOf(start));

            EditText mStep = (EditText) view.findViewById(R.id.rule_renumber_step);
            mStep.setText(String.valueOf(step));

            final EditText mPadding = (EditText) view.findViewById(R.id.rule_renumber_padding_amount);
            mPadding.setText(String.valueOf(padding));
            if (PadMode.MANUAL.equals(padMode)) {
                mPadding.setVisibility(View.VISIBLE);
            } else {
                mPadding.setVisibility(View.GONE);
            }

            Spinner mPadMode = (Spinner) view.findViewById(R.id.rule_renumber_padding_spinner);
            mPadMode.setSelection(padMode.getID());
            mPadMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (PadMode.MANUAL.getID() == position) {
                        mPadding.setVisibility(View.VISIBLE);
                    }
                    else {
                        mPadding.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            EditText mPosition = (EditText) view.findViewById(R.id.rule_position);
            mPosition.setText(String.valueOf(position));

            CheckBox mFromEnd = (CheckBox) view.findViewById(R.id.rule_position_backward);
            mFromEnd.setChecked(backward);

            Spinner mApplyTo = (Spinner) view.findViewById(R.id.rule_apply_spinner);
            mApplyTo.setSelection(applyTo.getID());
        } catch (Exception e) {
            Debug.logError(getClass(), "NPE updating view");
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
        str = context.getString(R.string.rule_renumber_start) + ": " + checkForEmpty(String.valueOf(start)) + ". "
                + context.getString(R.string.rule_renumber_step) + ": " + checkForEmpty(String.valueOf(step)) + ". "
                + context.getString(R.string.rule_renumber_padding) + ": " + PadMode.getLabel(context, padMode) + ". "
                + context.getString(R.string.rule_generic_position) + ": " + checkForEmpty(String.valueOf(position)) + ". "
                + context.getString(R.string.rule_generic_position_backward) + ": " + getValueToString(backward) + ". "
                + context.getString(R.string.rule_generic_apply) + ": " + ApplyTo.getLabel(context, applyTo);
        return str;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JSONObject serializeToJSON() throws JSONException {
        JSONObject jObject = new JSONObject();
        jObject.put(KEY_START, start);
        jObject.put(KEY_STEP, step);
        jObject.put(KEY_PADMODE, padMode);
        jObject.put(KEY_PADDING, padding);
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
        start = jObject.getInt(KEY_START);
        step = jObject.getInt(KEY_STEP);
        padMode = PadMode.getValue(jObject.getInt(KEY_PADMODE));
        padding = jObject.getInt(KEY_PADDING);
        position = jObject.getInt(KEY_POSITION);
        backward = jObject.getBoolean(KEY_BACKWARD);
        applyTo = ApplyTo.getValue(jObject.getInt(KEY_APPLYTO));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createFromParcel(Parcel in) {
        start = in.readInt();
        step = in.readInt();
        padMode = PadMode.getValue(in.readInt());
        padding = in.readInt();
        position = in.readInt();
        backward = toBoolean(in.readByte());
        applyTo = ApplyTo.getValue(in.readInt());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dumpToParcel(Parcel parcel, int i) {
        parcel.writeInt(start);
        parcel.writeInt(step);
        parcel.writeInt(padMode.getID());
        parcel.writeInt(padding);
        parcel.writeInt(position);
        parcel.writeByte(toByte(backward));
        parcel.writeInt(applyTo.getID());
    }

    protected enum PadMode {
        AUTO(0),
        MANUAL(1),
        OFF(2);

        private final int id;

        private PadMode(int id) {
            this.id = id;
        }

        public int getID() {
            return id;
        }

        public boolean compare(int i) {
            return id == i;
        }

        public static PadMode getValue(int _id) {
            PadMode[] array = PadMode.values();
            for (int i = 0; i < array.length; i++) {
                if (array[i].compare(_id))
                    return array[i];
            }
            // Value not recognized. Just return default value.
            return AUTO;
        }

        public static String getLabel(Context context, PadMode padMode) {
            String[] array = context.getResources().getStringArray(R.array.rule_renumber_padding_array);
            int index = Math.min(padMode.getID(), array.length - 1);
            return array[index];
        }
    }
}
