package com.giorgioaresu.batchrenamer.rules;

import android.content.Context;
import android.os.Parcel;
import android.view.View;
import android.widget.Spinner;

import com.giorgioaresu.batchrenamer.Debug;
import com.giorgioaresu.batchrenamer.R;
import com.giorgioaresu.batchrenamer.Rule;

import org.json.JSONException;
import org.json.JSONObject;

public class ChangeCase extends Rule {
    static final String KEY_CASEMODE = "CaseMode";
    static final String KEY_APPLYTO = "ApplyTo";

    CaseMode caseMode = CaseMode.UPPER;
    ApplyTo applyTo = ApplyTo.BOTH;


    public ChangeCase(Context context) {
        super(context, context.getString(R.string.rule_changecase_title), R.layout.rule_card_changecase);
    }

    public String getNewName(String currentName, int positionInSet, int setSize) {
        return getNewName(currentName, positionInSet, setSize, applyTo);
    }

    @Override
    protected String getPatchedString(String string, int positionInSet, int setSize) {
        String res = string;
        switch (caseMode) {
            case UPPER:
                res = CaseManipulation.toUpperCase(string);
                break;
            case LOWER:
                res = CaseManipulation.toLowerCase(string);
                break;
            case CAMEL:
                res = CaseManipulation.toCamelCase(string);
                break;
            case SENTENCE:
                res = CaseManipulation.toSentenceCase(string);
                break;
            case TOGGLE:
                res = CaseManipulation.toToggleCase(string);
                break;
        }

        return res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateDataFromView(View view) {
        try {
            Spinner mCaseMode = (Spinner) view.findViewById(R.id.rule_changecase_case_spinner);
            caseMode = CaseMode.getValue(mCaseMode.getSelectedItemPosition());

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
            Spinner mCaseMode = (Spinner) view.findViewById(R.id.rule_changecase_case_spinner);
            mCaseMode.setSelection(caseMode.getID());

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
        str = context.getString(R.string.rule_changecase_case) + ": " + CaseMode.getLabel(context, caseMode) + ". "
                + context.getString(R.string.rule_generic_apply) + ": " + ApplyTo.getLabel(context, applyTo);
        return str;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JSONObject serializeToJSON() throws JSONException {
        JSONObject jObject = new JSONObject();
        jObject.put(KEY_CASEMODE, caseMode);
        jObject.put(KEY_APPLYTO, applyTo);
        return jObject;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void deserializeFromJSON(JSONObject jObject) throws JSONException {
        caseMode = CaseMode.getValue(jObject.getInt(KEY_CASEMODE));
        applyTo = ApplyTo.getValue(jObject.getInt(KEY_APPLYTO));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createFromParcel(Parcel in) {
        caseMode = CaseMode.getValue(in.readInt());
        applyTo = ApplyTo.getValue(in.readInt());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dumpToParcel(Parcel parcel, int i) {
        parcel.writeInt(caseMode.getID());
        parcel.writeInt(applyTo.getID());
    }

    protected enum CaseMode {
        UPPER(0),
        LOWER(1),
        CAMEL(2),
        SENTENCE(3),
        TOGGLE(4);

        private final int id;

        private CaseMode(int id) {
            this.id = id;
        }

        public int getID() {
            return id;
        }

        public boolean compare(int i) {
            return id == i;
        }

        public static CaseMode getValue(int _id) {
            CaseMode[] array = CaseMode.values();
            for (int i = 0; i < array.length; i++) {
                if (array[i].compare(_id))
                    return array[i];
            }
            // Value not recognized. Just return default value.
            return UPPER;
        }

        public static String getLabel(Context context, CaseMode caseMode) {
            String[] array = context.getResources().getStringArray(R.array.rule_changecase_case_array);
            int index = Math.min(caseMode.getID(), array.length - 1);
            return array[index];
        }
    }

    public static class CaseManipulation {

        public static String toUpperCase(String inputString) {
            String result = "";
            for (int i = 0; i < inputString.length(); i++) {
                char currentChar = inputString.charAt(i);
                char currentCharToUpperCase = Character.toUpperCase(currentChar);
                result = result + currentCharToUpperCase;
            }
            return result;
        }

        public static String toLowerCase(String inputString) {
            String result = "";
            for (int i = 0; i < inputString.length(); i++) {
                char currentChar = inputString.charAt(i);
                char currentCharToLowerCase = Character.toLowerCase(currentChar);
                result = result + currentCharToLowerCase;
            }
            return result;
        }

        public static String toToggleCase(String inputString) {
            String result = "";
            for (int i = 0; i < inputString.length(); i++) {
                char currentChar = inputString.charAt(i);
                if (Character.isUpperCase(currentChar)) {
                    char currentCharToLowerCase = Character.toLowerCase(currentChar);
                    result = result + currentCharToLowerCase;
                } else {
                    char currentCharToUpperCase = Character.toUpperCase(currentChar);
                    result = result + currentCharToUpperCase;
                }
            }
            return result;
        }

        public static String toCamelCase(String inputString) {
            String result = "";
            if (inputString.length() == 0) {
                return result;
            }
            char firstChar = inputString.charAt(0);
            char firstCharToUpperCase = Character.toUpperCase(firstChar);
            result = result + firstCharToUpperCase;
            for (int i = 1; i < inputString.length(); i++) {
                char currentChar = inputString.charAt(i);
                char previousChar = inputString.charAt(i - 1);
                if (previousChar == ' ') {
                    char currentCharToUpperCase = Character.toUpperCase(currentChar);
                    result = result + currentCharToUpperCase;
                } else {
                    char currentCharToLowerCase = Character.toLowerCase(currentChar);
                    result = result + currentCharToLowerCase;
                }
            }
            return result;
        }

        public static String toSentenceCase(String inputString) {
            String result = "";
            if (inputString.length() == 0) {
                return result;
            }
            char firstChar = inputString.charAt(0);
            char firstCharToUpperCase = Character.toUpperCase(firstChar);
            result = result + firstCharToUpperCase;
            boolean terminalCharacterEncountered = false;
            char[] terminalCharacters = {'.', '?', '!'};
            for (int i = 1; i < inputString.length(); i++) {
                char currentChar = inputString.charAt(i);
                if (terminalCharacterEncountered) {
                    if (currentChar == ' ') {
                        result = result + currentChar;
                    } else {
                        char currentCharToUpperCase = Character.toUpperCase(currentChar);
                        result = result + currentCharToUpperCase;
                        terminalCharacterEncountered = false;
                    }
                } else {
                    char currentCharToLowerCase = Character.toLowerCase(currentChar);
                    result = result + currentCharToLowerCase;
                }
                for (int j = 0; j < terminalCharacters.length; j++) {
                    if (currentChar == terminalCharacters[j]) {
                        terminalCharacterEncountered = true;
                        break;
                    }
                }
            }
            return result;
        }
    }
}
