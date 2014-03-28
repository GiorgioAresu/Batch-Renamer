package com.giorgioaresu.batchrenamer.actions;

import android.content.Context;
import android.os.Parcel;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import com.giorgioaresu.batchrenamer.Action;
import com.giorgioaresu.batchrenamer.Debug;
import com.giorgioaresu.batchrenamer.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Replace extends Action {
    static final String KEY_PATTERN = "Pattern";
    static final String KEY_REGEX = "Regex";
    static final String KEY_REPLACEMENT = "Replacement";
    static final String KEY_APPLYTO = "ApplyTo";

    String pattern = "";
    Boolean regex = false;
    String replacement = "";
    ApplyTo applyTo = ApplyTo.BOTH;


    public Replace(Context context) {
        super(context, context.getString(R.string.actioncard_replace_title), R.layout.action_card_replace);
    }

    public String getNewName(String currentName, int positionInSet, int setSize) {
        return getNewName(currentName, positionInSet, setSize, applyTo);
    }

    @Override
    protected String getPatchedString(String string, int positionInSet, int setSize) {
        String res;
        if (regex) {
            try {
                res = string.replaceAll(pattern, replacement);
            } catch (PatternSyntaxException e) {
                // Syntax error, keep string untouched
                res = string;
            }
        } else {
            res = string.replace(pattern, replacement);
        }
        return res;
    }

    @Override
    public boolean isValid() {
        if (regex) {
            try {
                Pattern.compile(pattern);
            } catch (PatternSyntaxException ex) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateDataFromView(View view) {
        try {
            EditText mPattern = (EditText) view.findViewById(R.id.action_replace_pattern);
            pattern = mPattern.getText().toString();

            CheckBox mRegex = (CheckBox) view.findViewById(R.id.action_replace_regex);
            regex = mRegex.isChecked();

            EditText mReplacement = (EditText) view.findViewById(R.id.action_replace_replacement);
            replacement = mReplacement.getText().toString();

            Spinner mApplyTo = (Spinner) view.findViewById(R.id.action_apply_spinner);
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
            EditText mPattern = (EditText) view.findViewById(R.id.action_replace_pattern);
            mPattern.setText(pattern);
            checkRegex(mPattern, regex);

            CheckBox mRegex = (CheckBox) view.findViewById(R.id.action_replace_regex);
            mRegex.setChecked(regex);

            EditText mReplacement = (EditText) view.findViewById(R.id.action_replace_replacement);
            mReplacement.setText(replacement);

            Spinner mApplyTo = (Spinner) view.findViewById(R.id.action_apply_spinner);
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
        str = context.getString(R.string.actioncard_replace_pattern) + ": " + checkForEmpty(pattern) + ". "
                + context.getString(R.string.actioncard_regex) + ": " + getValueToString(regex) + ". "
                + context.getString(R.string.actioncard_replace_replacement) + ": " + checkForEmpty(replacement) + ". "
                + context.getString(R.string.actioncard_apply) + ": " + ApplyTo.getLabel(context, applyTo);
        return str;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JSONObject serializeToJSON() throws JSONException {
        JSONObject jObject = new JSONObject();
        jObject.put(KEY_PATTERN, pattern);
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
        pattern = jObject.getString(KEY_PATTERN);
        regex = jObject.getBoolean(KEY_REGEX);
        replacement = jObject.getString(KEY_REPLACEMENT);
        applyTo = ApplyTo.getValue(jObject.getInt(KEY_APPLYTO));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createFromParcel(Parcel in) {
        pattern = in.readString();
        regex = toBoolean(in.readByte());
        replacement = in.readString();
        applyTo = ApplyTo.getValue(in.readInt());
    }

    @Override
    public void onInflate(View view) {
        final CheckBox mRegex = (CheckBox) view.findViewById(R.id.action_replace_regex);
        final EditText mPattern = (EditText) view.findViewById(R.id.action_replace_pattern);

        mRegex.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPattern.setError(null);
                checkRegex(mPattern, isChecked);
            }
        });

        mPattern.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkRegex(mPattern, mRegex.isChecked());
            }
        });
    }

    private void checkRegex(EditText mPattern, boolean regex) {
        if (regex) {
            try {
                Pattern.compile(mPattern.getText().toString());
                mPattern.setError(null);
            } catch (PatternSyntaxException ex) {
                mPattern.setError(context.getString(R.string.actioncard_regex_invalid));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dumpToParcel(Parcel parcel, int i) {
        parcel.writeString(pattern);
        parcel.writeByte(toByte(regex));
        parcel.writeString(replacement);
        parcel.writeInt(applyTo.getID());
    }
}
