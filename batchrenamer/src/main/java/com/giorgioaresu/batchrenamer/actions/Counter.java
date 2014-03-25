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

public class Counter extends Action {
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
    int padding = 0;
    int position = 0;
    boolean backward = false;
    ApplyTo applyTo = ApplyTo.BOTH;


    public Counter(Context context) {
        super(context, context.getString(R.string.action_counter_title), R.layout.action_card_counter);
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
                int lenght = 0;
                for (int size = setSize; size >= 0; size/=10) lenght++;
                paddedNumber = String.format("%0" + lenght + "d", number);
                break;
            case MANUAL:
                paddedNumber = String.format("%0" + padding + "d", number);
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
            EditText mStart = (EditText) view.findViewById(R.id.action_counter_start);
            start = Integer.parseInt(mStart.getText().toString());

            EditText mStep = (EditText) view.findViewById(R.id.action_counter_step);
            step = Integer.parseInt(mStep.getText().toString());

            RadioGroup mPadMode = (RadioGroup) view.findViewById(R.id.action_counter_padMode_radiogroup);
            padMode = PadMode.getValue(mPadMode.getCheckedRadioButtonId());

            EditText mPadding = (EditText) view.findViewById(R.id.action_counter_padding);
            padding = Integer.parseInt(mPadding.getText().toString());

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
            EditText mStart = (EditText) view.findViewById(R.id.action_counter_start);
            mStart.setText(String.valueOf(start));

            EditText mStep = (EditText) view.findViewById(R.id.action_counter_step);
            mStart.setText(String.valueOf(step));

            RadioGroup mPadMode = (RadioGroup) view.findViewById(R.id.action_counter_padMode_radiogroup);
            mPadMode.check(padMode.getID());

            EditText mPadding = (EditText) view.findViewById(R.id.action_counter_padding);
            mPadding.setText(String.valueOf(padding));

            EditText mPosition = (EditText) view.findViewById(R.id.action_counter_index);
            mPosition.setText(String.valueOf(position));

            CheckBox mFromEnd = (CheckBox) view.findViewById(R.id.action_counter_backward);
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
        /*str = context.getString(R.string.action_add_text) + ": " + checkForEmpty(text) + ". "
                + context.getString(R.string.action_position_index) + ": " + checkForEmpty(String.valueOf(position)) + ". "
                + context.getString(R.string.action_position_backward) + ": "
                + context.getString(backward ? R.string.true_ : R.string.false_) + ". "
                + context.getString(R.string.action_apply) + ": " + context.getString(ApplyTo.getStringResource(applyTo));
        return str;*/
        return "";
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
        backward = in.readByte() != 0 ? true : false;
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
        parcel.writeByte((byte) (backward ? 1 : 0));
        parcel.writeInt(applyTo.getID());
    }

    protected enum PadMode {
        AUTO(R.id.action_counter_padModeAuto),
        MANUAL(R.id.action_counter_padModeManual),
        OFF(R.id.action_counter_padModeOff);

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
            PadMode[] As = PadMode.values();
            for (int i = 0; i < As.length; i++) {
                if (As[i].compare(_id))
                    return As[i];
            }
            // Value not recognized. Just return default value.
            return OFF;
        }

        public static int getStringResource(PadMode padMode) {
            switch (padMode) {
                case AUTO:
                    return R.string.action_counter_padModeAuto;
                case MANUAL:
                    return R.string.action_counter_padModeManual;
                default:
                    return R.string.action_counter_padModeOff;
            }
        }
    }
}
