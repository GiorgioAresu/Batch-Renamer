package com.giorgioaresu.batchrenamer.rules;

import android.content.Context;
import android.os.Parcel;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import com.giorgioaresu.batchrenamer.Debug;
import com.giorgioaresu.batchrenamer.R;
import com.giorgioaresu.batchrenamer.Rule;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.PatternSyntaxException;

public class Replace extends Rule {
    static final String KEY_PATTERN = "Pattern";
    static final String KEY_REGEX = "Regex";
    static final String KEY_REPLACEMENT = "Replacement";
    static final String KEY_APPLYTO = "ApplyTo";

    String pattern = "";
    Boolean regex = false;
    String replacement = "";
    ApplyTo applyTo = ApplyTo.BOTH;


    public Replace(Context context) {
        super(context, context.getString(R.string.rule_replace_title), R.layout.rule_card_replace);
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
            } catch (PatternSyntaxException | ArrayIndexOutOfBoundsException e) {
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
            return checkRegex(pattern, replacement) == RegexStatus.OK;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateDataFromView(View view) {
        try {
            EditText mPattern = (EditText) view.findViewById(R.id.rule_replace_pattern);
            pattern = mPattern.getText().toString();

            CheckBox mRegex = (CheckBox) view.findViewById(R.id.rule_replace_regex);
            regex = mRegex.isChecked();

            EditText mReplacement = (EditText) view.findViewById(R.id.rule_replace_replacement);
            replacement = mReplacement.getText().toString();

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
            EditText mPattern = (EditText) view.findViewById(R.id.rule_replace_pattern);
            mPattern.setText(pattern);

            CheckBox mRegex = (CheckBox) view.findViewById(R.id.rule_replace_regex);
            mRegex.setChecked(regex);

            EditText mReplacement = (EditText) view.findViewById(R.id.rule_replace_replacement);
            mReplacement.setText(replacement);

            if (regex) {
                updateRegexFieldsErrors(mPattern, mReplacement);
            }

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
        str = context.getString(R.string.rule_replace_pattern) + ": " + checkForEmpty(pattern) + ". "
                + context.getString(R.string.rule_generic_regex) + ": " + getValueToString(regex) + ". "
                + context.getString(R.string.rule_replace_replacement) + ": " + checkForEmpty(replacement) + ". "
                + context.getString(R.string.rule_generic_apply) + ": " + ApplyTo.getLabel(context, applyTo);
        return str;
    }

    @Override
    public void onInflate(View view) {
        final EditText mPattern = (EditText) view.findViewById(R.id.rule_replace_pattern);
        final CheckBox mRegex = (CheckBox) view.findViewById(R.id.rule_replace_regex);
        final EditText mReplacement = (EditText) view.findViewById(R.id.rule_replace_replacement);

        mRegex.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPattern.setError(null);
                mReplacement.setError(null);
                if (isChecked) {
                    updateRegexFieldsErrors(mPattern, mReplacement);
                }
            }
        });

        // Common textwatcher
        TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mRegex.isChecked()) {
                    updateRegexFieldsErrors(mPattern, mReplacement);
                }
            }
        };

        mPattern.addTextChangedListener(tw);
        mReplacement.addTextChangedListener(tw);
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
        jObject.put(KEY_APPLYTO, applyTo.getID());
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
