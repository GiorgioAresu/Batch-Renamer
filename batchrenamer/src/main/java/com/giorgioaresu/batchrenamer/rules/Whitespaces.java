package com.giorgioaresu.batchrenamer.rules;

import android.content.Context;
import android.os.Parcel;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.giorgioaresu.batchrenamer.Debug;
import com.giorgioaresu.batchrenamer.R;
import com.giorgioaresu.batchrenamer.Rule;

import org.json.JSONException;
import org.json.JSONObject;

public class Whitespaces extends Rule {
    static final String KEY_SPACE_CHAR = "SpaceChar";
    static final String KEY_REDUCE_DUPLICATES = "ReduceDuplicates";
    static final String KEY_REPLACE_WITH = "ReplaceWith";
    static final String KEY_TRIM = "Trim";
    static final String KEY_APPLYTO = "ApplyTo";

    SpaceChar spaceChar = SpaceChar.WHITESPACE;
    Boolean reduceDuplicates = false;
    ReplaceChar replaceWith = ReplaceChar.NULL;
    Trim trim = Trim.NO;
    ApplyTo applyTo = ApplyTo.NAME;

    public Whitespaces(Context context) {
        super(context, context.getString(R.string.rule_whitespaces_title), R.layout.rule_card_whitespaces);
    }

    public String getNewName(String currentName, int positionInSet, int setSize) {
        return getNewName(currentName, positionInSet, setSize, applyTo);
    }

    @Override
    protected String getPatchedString(String string, int positionInSet, int setSize) {
        String res = string;
        char mChar = SpaceChar.getChar(spaceChar);

        if (reduceDuplicates) {
            // Remove duplicates
            String doubleChar = "" + mChar + mChar;
            while (res.indexOf(doubleChar) != -1)
                res = res.replace(doubleChar, String.valueOf(mChar));
        }

        switch (trim) {
            case START:
                res = ltrim(res, mChar);
                break;
            case END:
                res = rtrim(res, mChar);
                break;
            case BOTH:
                res = ltrim(res, mChar);
                res = rtrim(res, mChar);
                break;
        }

        if (!spaceChar.compare(replaceWith.getID())) {
            // Convert whitespace character
            String mTarget = String.valueOf(mChar);
            String mReplacement = String.valueOf(ReplaceChar.getChar(replaceWith));
            res = res.replaceAll(mTarget, mReplacement);
        }

        return res;
    }

    /**
     * Trims c's from the right of s
     * @param s
     * @param c
     * @return
     */
    private String rtrim(String s, char c) {
        int i = s.length()-1;
        while (i >= 0 && c == s.charAt(i)) {
            i--;
        }
        s = s.substring(0,i+1);
        return s;
    }

    /**
     * Trims c's from the left of s
     * @param s
     * @param c
     * @return
     */
    private String ltrim(String s, char c) {
        int i = 0;
        while (i < s.length() && c == s.charAt(i)) {
            i++;
        }
        s = s.substring(i);
        return s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateDataFromView(View view) {
        try {
            Spinner mSpaceChar = (Spinner) view.findViewById(R.id.rule_whitespaces_char);
            spaceChar = SpaceChar.getValue(mSpaceChar.getSelectedItemPosition());

            CheckBox mReduceDuplicates = (CheckBox) view.findViewById(R.id.rule_whitespaces_duplicates);
            reduceDuplicates = mReduceDuplicates.isChecked();

            Spinner mReplaceWith = (Spinner) view.findViewById(R.id.rule_whitespaces_replace_with);
            replaceWith = ReplaceChar.getValue(mReplaceWith.getSelectedItemPosition());

            Spinner mTrim = (Spinner) view.findViewById(R.id.rule_whitespaces_trim_spinner);
            trim = Trim.getValue(mTrim.getSelectedItemPosition());

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
            Spinner mSpaceChar = (Spinner) view.findViewById(R.id.rule_whitespaces_char);
            mSpaceChar.setSelection(spaceChar.getID());

            CheckBox mReduceDuplicates = (CheckBox) view.findViewById(R.id.rule_whitespaces_duplicates);
            mReduceDuplicates.setChecked(reduceDuplicates);

            Spinner mReplaceWith = (Spinner) view.findViewById(R.id.rule_whitespaces_replace_with);
            mReplaceWith.setSelection(replaceWith.getID());

            Spinner mTrim = (Spinner) view.findViewById(R.id.rule_whitespaces_trim_spinner);
            mTrim.setSelection(trim.getID());

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
        str = context.getString(R.string.rule_whitespaces_char) + ": " + SpaceChar.getLabel(context, spaceChar) + ". "
                + context.getString(R.string.rule_whitespaces_duplicates) + ": " + getValueToString(reduceDuplicates) + ". "
                + context.getString(R.string.rule_whitespaces_replace) + ": " + ReplaceChar.getLabel(context, replaceWith) + ". "
                + context.getString(R.string.rule_whitespaces_trim) + ": " + Trim.getLabel(context, trim)
                + context.getString(R.string.rule_generic_apply) + ": " + ApplyTo.getLabel(context, applyTo);
        return str;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JSONObject serializeToJSON() throws JSONException {
        JSONObject jObject = new JSONObject();
        jObject.put(KEY_SPACE_CHAR, spaceChar.getID());
        jObject.put(KEY_REDUCE_DUPLICATES, reduceDuplicates);
        jObject.put(KEY_REPLACE_WITH, replaceWith.getID());
        jObject.put(KEY_TRIM, trim.getID());
        jObject.put(KEY_APPLYTO, applyTo.getID());
        return jObject;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void deserializeFromJSON(JSONObject jObject) throws JSONException {
        spaceChar = SpaceChar.getValue(jObject.getInt(KEY_SPACE_CHAR));
        reduceDuplicates = jObject.getBoolean(KEY_REDUCE_DUPLICATES);
        replaceWith = ReplaceChar.getValue(jObject.getInt(KEY_REPLACE_WITH));
        trim = Trim.getValue(jObject.getInt(KEY_TRIM));
        applyTo = ApplyTo.getValue(jObject.getInt(KEY_APPLYTO));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createFromParcel(Parcel in) {
        spaceChar = SpaceChar.getValue(in.readInt());
        reduceDuplicates = toBoolean(in.readByte());
        replaceWith = ReplaceChar.getValue(in.readInt());
        trim = Trim.getValue(in.readInt());
        applyTo = ApplyTo.getValue(in.readInt());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dumpToParcel(Parcel parcel, int i) {
        parcel.writeInt(spaceChar.getID());
        parcel.writeByte(toByte(reduceDuplicates));
        parcel.writeInt(replaceWith.getID());
        parcel.writeInt(trim.getID());
        parcel.writeInt(applyTo.getID());
    }

    private enum SpaceChar {
        WHITESPACE(0),
        UNDERSCORE(1),
        MINUS_SIGN(2),
        DOT(3);

        private final int id;

        private SpaceChar(int id) {
            this.id = id;
        }

        public static SpaceChar getValue(int _id) {
            SpaceChar[] array = SpaceChar.values();
            for (int i = 0; i < array.length; i++) {
                if (array[i].compare(_id))
                    return array[i];
            }
            // Value not recognized. Just return default value.
            return WHITESPACE;
        }

        public static String getLabel(Context context, SpaceChar spaceChar) {
            String[] array = context.getResources().getStringArray(R.array.rule_whitespaces_char_array);
            int index = Math.min(spaceChar.id, array.length - 1);
            return array[index];
        }

        public static char getChar(SpaceChar spaceChar) {
            switch (spaceChar) {
                case WHITESPACE:
                    return ' ';
                case UNDERSCORE:
                    return '_';
                case MINUS_SIGN:
                    return '-';
                case DOT:
                    return '.';
                default:
                    // Value not recognized. Just return default value.
                    return getChar(WHITESPACE);
            }
        }

        public int getID() {
            return id;
        }

        public boolean compare(int i) {
            return id == i;
        }
    }

    private enum ReplaceChar {
        NULL(0),
        WHITESPACE(1),
        UNDERSCORE(2),
        MINUS_SIGN(3),
        DOT(4);

        private final int id;

        private ReplaceChar(int id) {
            this.id = id;
        }

        public static ReplaceChar getValue(int _id) {
            ReplaceChar[] array = ReplaceChar.values();
            for (int i = 0; i < array.length; i++) {
                if (array[i].compare(_id))
                    return array[i];
            }
            // Value not recognized. Just return default value.
            return NULL;
        }

        public static String getLabel(Context context, ReplaceChar replaceChar) {
            String[] array = context.getResources().getStringArray(R.array.rule_whitespaces_char_array);
            int index = Math.min(replaceChar.id, array.length - 1);
            return array[index];
        }

        public static char getChar(ReplaceChar replaceChar) {
            switch (replaceChar) {
                case WHITESPACE:
                    return ' ';
                case UNDERSCORE:
                    return '_';
                case MINUS_SIGN:
                    return '-';
                case DOT:
                    return '.';
                default:
                    // Value not recognized. Just return default value.
                    return getChar(NULL);
            }
        }

        public int getID() {
            return id;
        }

        public boolean compare(int i) {
            return id == i;
        }
    }

    private enum Trim {
        NO(0),
        START(1),
        END(2),
        BOTH(3);

        private final int id;

        private Trim(int id) {
            this.id = id;
        }

        public static Trim getValue(int _id) {
            Trim[] array = Trim.values();
            for (int i = 0; i < array.length; i++) {
                if (array[i].compare(_id))
                    return array[i];
            }
            // Value not recognized. Just return default value.
            return NO;
        }

        public static String getLabel(Context context, Trim trim) {
            String[] array = context.getResources().getStringArray(R.array.rule_whitespaces_trim_array);
            int index = Math.min(trim.id, array.length - 1);
            return array[index];
        }

        public int getID() {
            return id;
        }

        public boolean compare(int i) {
            return id == i;
        }
    }
}
