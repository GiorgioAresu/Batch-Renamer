package com.giorgioaresu.batchrenamer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public abstract class Rule implements Parcelable {
    static final String KEY_CONTENT = "Content";
    static final String KEY_TYPE = "Type";

    protected Context context;
    private String title;
    private int viewId;

    public Rule(Context context, String title, int viewId) {
        this.context = context;
        this.title = title;
        this.viewId = viewId;
    }

    /**
     * Get the list of actions sorted alphabetically
     * Format is Map<"Localized string", "ClassName">
     *
     * @param context used to retrieve localized strings
     * @return
     */
    public static final Map<String, String> getRules(Context context) {
        Map<String, String> rules = new TreeMap<>();
        rules.put(context.getString(R.string.rule_add_title), "Add");
        rules.put(context.getString(R.string.rule_changecase_title), "ChangeCase");
        rules.put(context.getString(R.string.rule_remove_title), "Remove");
        rules.put(context.getString(R.string.rule_renumber_title), "Renumber");
        rules.put(context.getString(R.string.rule_replace_title), "Replace");
        rules.put(context.getString(R.string.rule_whitespaces_title), "Whitespaces");
        return rules;
    }

    /**
     * Creates an rule from content described in a JSONObject
     *
     * @param context Context used to instantiate the rule
     * @param jObject JSONObject containing rule description
     * @return
     */
    public static final Rule createFromJSON(Context context, JSONObject jObject) {
        try {
            Class<?> c = Class.forName(jObject.getString(KEY_TYPE));
            Constructor<?> cons = c.getConstructors()[0];
            Rule rule = (Rule) cons.newInstance(context);
            rule.deserializeFromJSON(jObject.getJSONObject(KEY_CONTENT));
            return rule;
        } catch (Exception e) {
            Log.e("batchrenamer", "Exception creating item from JSON, skipping");
            return null;
        }
    }

    public String getTitle() {
        return title;
    }

    public int getViewId() {
        return viewId;
    }

    /**
     * Given a string and its position in the original set of strings,
     * process it and returns the new one
     *
     * If you need the ApplyTo logic, you can just return the result
     * of a call to getNewName(String, int, ApplyTo) which will handle
     * all the ApplyTo logic for you, provided that you override
     * getPatchedString(String, int) putting the new filename logic there
     *
     * @param currentName   current string
     * @param positionInSet position of the string in the original set (useful for renumbers)
     * @param setSize number of items in the set
     * @return the new string
     */
    public abstract String getNewName(String currentName, int positionInSet, int setSize);

    /**
     * IF YOU USE THIS METHOD, OVERRIDE getPatchedString
     *
     * Given a string, its position in the original set of strings
     * and the selected ApplyTo value, process it and returns the new one
     * @param currentName current string
     * @param positionInSet position of the string in the original set (useful for renumbers)
     * @param setSize number of items in the set
     *@param applyTo enum value representing what part of the filename we should process  @return the new string
     */
    protected String getNewName(String currentName, int positionInSet, int setSize, ApplyTo applyTo) {
        String result;
        if (ApplyTo.BOTH == applyTo) {
            // We want to work on the whole string, so we just process it
            result = getPatchedString(currentName, positionInSet, setSize);
        } else {
            // We want to work only on a part of the string, we need more work

            // Find the last dot in the name
            int lastIndexOfDot = currentName.lastIndexOf('.');

            if (lastIndexOfDot == -1) {
                // Doesn't contain an extension
                if (ApplyTo.NAME == applyTo) {
                    // The whole string is the name, no extension to consider
                    result = getPatchedString(currentName, positionInSet, setSize);
                } else {
                    // No extension to modify, return untouched name
                    result = currentName;
                }
            } else {
                // We have a filename composed of name + extension, so we need
                // to discern them to modify appropriately
                String name = currentName.substring(0, lastIndexOfDot);
                String ext = currentName.substring(lastIndexOfDot + 1);

                if (ApplyTo.NAME == applyTo) {
                    result = getPatchedString(name, positionInSet, setSize) + "." + ext;
                } else {
                    result = name + "." + getPatchedString(ext, positionInSet, setSize);
                }
            }
        }
        return result;
    }

    /**
     * OVERRIDE THIS METHOD IF YOU USE getNewName(String, int, ApplyTo)
     *
     * Apply rule to a string
     * @param string string to be computed
     * @param positionInSet position of the string in the original set (useful for renumbers)
     * @param setSize number of items in the set
     * @return the new string
     */
    protected String getPatchedString(String string, int positionInSet, int setSize) {
        Exception exception = new NoSuchMethodException(getClass().getName() + ": getPatchedString not implemented");
        exception.printStackTrace();
        return null;
    }

    /**
     * Rules should perform validity checkings in this method (ie. check regex syntax)
     *
     * @return
     */
    public boolean isValid() { return true; }

    /**
     * Update underlying data from a view of the appropriate type
     *
     * @param view the view from which to get data
     * @return true if data has been update successfully, false otherwise
     */
    public abstract boolean updateDataFromView(View view);

    /**
     * Update a view of the appropriate type from underlying data
     *
     * @param view the view to be filled with data
     * @return true if data has been update successfully, false otherwise
     */
    public abstract boolean updateViewFromData(View view);

    /**
     * Get the preview of a view, populated with current data from rule.
     * Due to some problems with match_parent in layout not been applied when inflating
     * without a parent view, the width must be supplied to this method
     * (ie. when the preview has to be showed in a ImageView, pass iv.getMeasuredWidth()
     * as parameter).
     *
     * @param width the width of the resulting Bitmap
     * @return the Bitmap of the preview
     */
    public Bitmap getBitmapOfView(int width) {
        // Inflate the view
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(viewId, null);

        // Fill the view with data
        updateViewFromData(view);

        // Set dimensions and obtain bitmap
        view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    /**
     * Describes the rule content in a string (ie. for accessibility purposes)
     *
     * @return the string created
     */
    protected abstract String getContentDescription();

    /**
     * Check if a string is empty
     *
     * @param string String to check
     * @return R.string.empty_field_label if string is empty, the string itself otherwise
     */
    protected String checkForEmpty(String string) {
        return string.isEmpty() ? context.getString(R.string.rule_generic_emptyfield_contentdescription) : string;
    }

    /**
     * Returns the string for a boolean value
     *
     * @param v boolean value
     * @return string representing the value
     */
    protected String getValueToString(boolean v) {
        return context.getString(v ? R.string.rule_generic_true_contentdescription : R.string.rule_generic_false_contentdescription);
    }

    /**
     * Returns a byte encoding a boolean value
     *
     * @param b boolean value
     * @return 1 if true, 0 otherwise
     */
    protected byte toByte(boolean b) {
        return (byte) (b ? 1 : 0);
    }

    /**
     * Returns the boolean value encoded in a byte
     *
     * @param b byte
     * @return true if 1, false otherwise
     */
    protected boolean toBoolean(byte b) {
        return b != 0;
    }

    /**
     * Dump rule to a JSONObject
     *
     * @return the JSONObject created
     */
    public final JSONObject dumpToJSON() {
        JSONObject jObject = new JSONObject();
        try {
            jObject.put(KEY_CONTENT, serializeToJSON());
            jObject.put(KEY_TYPE, getClass().getName());
            return jObject;
        } catch (JSONException e) {
            Log.e("batchrenamer", "Exception dumping item to JSON, skipping");
            return null;
        }
    }

    /**
     * Dump rule fields to a JSONObject
     *
     * @return JSONObject created
     * @throws JSONException
     */
    protected abstract JSONObject serializeToJSON() throws JSONException;

    /**
     * Restore rule status from content described in a JSONObject
     *
     * @param jObject JSONObject containing rule description
     * @throws JSONException
     */
    protected abstract void deserializeFromJSON(JSONObject jObject) throws JSONException;

    public static final Parcelable.Creator<Rule> CREATOR
            = new Parcelable.Creator<Rule>() {
        public Rule createFromParcel(Parcel in) {
            try {
                Class<?> c = Class.forName(in.readString());
                Constructor<?> cons = c.getConstructors()[0];
                Rule rule = (Rule) cons.newInstance(Application.getContext());
                rule.createFromParcel(in);
                return rule;
            } catch (Exception b) {
                Log.e("batchrenamer", "Exception creating item, skipping");
                return null;
            }
        }

        public Rule[] newArray(int size) {
            return new Rule[size];
        }
    };

    /**
     * Dump rule to a Parcel
     *
     * @param parcel Parcel to dump rule to
     * @param i
     */
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getClass().getName());
        dumpToParcel(parcel, i);
    }

    /**
     * Sets or resets EditTexts errors appropriately based on regex status
     *
     * @param pattern EditText containing the regex pattern
     * @param replacement EditText containing the regex replacement
     */
    protected void updateRegexFieldsErrors(EditText pattern, EditText replacement) {
        RegexStatus status = checkRegex(pattern.getText().toString(), replacement.getText().toString());
        pattern.setError(RegexStatus.errorStringPattern(context, status));
        replacement.setError(RegexStatus.errorStringReplacement(context, status));
    }

    /**
     * Check if the regex is valid:
     * - the pattern compiles
     * - groupings referred in replacement exist in the pattern
     *
     * @param pattern EditText containing the regex pattern
     * @param replacement EditText containing the regex replacement
     */
    protected RegexStatus checkRegex(String pattern, String replacement) {
        try {
            Pattern p = Pattern.compile(pattern);
            evaluateGrouping(p, replacement);
        } catch (PatternSyntaxException ex) {
            return RegexStatus.PATTERN_SYNTAX_ERROR;
        } catch (ArrayIndexOutOfBoundsException ex) {
            return RegexStatus.GROUP_OUT_OF_BOUNDS;
        }
        return RegexStatus.OK;
    }

    /**
     * Modified version of appendEvaluated method of {@link java.util.regex.Matcher}
     * Checks that groups referred in the replacement string, occur in the pattern
     * @param pattern pattern of the regex
     * @param replacement replacement string
     */
    protected void evaluateGrouping(Pattern pattern, String replacement) {
        boolean escape = false;
        boolean dollar = false;
        int groups = pattern.matcher("").groupCount();

        for (int i = 0; i < replacement.length(); i++) {
            char c = replacement.charAt(i);
            if (c == '\\' && !escape) {
                escape = true;
            } else if (c == '$' && !escape) {
                dollar = true;
            } else if (c >= '0' && c <= '9' && dollar) {
                if (groups < (c - '0')) {
                    throw new ArrayIndexOutOfBoundsException();
                }
                dollar = false;
            } else {
                dollar = false;
                escape = false;
            }
        }

        // This seemingly stupid piece of code reproduces a JDK bug.
        if (escape) {
            throw new ArrayIndexOutOfBoundsException(replacement.length());
        }
    }

    /**
     * Dump rule fields to a Parcel
     *
     * @param parcel Parcel to dump rule to
     * @param i
     */
    public abstract void dumpToParcel(Parcel parcel, int i);

    /**
     * Restore rule status from content described in a Parcel
     *
     * @param in Parcel containing rule description
     */
    protected abstract void createFromParcel(Parcel in);

    public int describeContents() {
        return 0;
    }

    /**
     * Call when inflating layout to let the rule prepare the UI (ie. attach listeners)
     * @param view the inflated view for the rule
     */
    public void onInflate(View view) { }

    protected enum ApplyTo {
        NAME(0),
        EXTENSION(1),
        BOTH(2);

        private final int id;

        private ApplyTo(int id) {
            this.id = id;
        }

        public static ApplyTo getValue(int _id) {
            ApplyTo[] array = ApplyTo.values();
            for (int i = 0; i < array.length; i++) {
                if (array[i].compare(_id))
                    return array[i];
            }
            // Value not recognized. Just return default value.
            return BOTH;
        }

        public static String getLabel(Context context, ApplyTo applyTo) {
            String[] array = context.getResources().getStringArray(R.array.rule_generic_apply_array);
            int index = Math.min(applyTo.id, array.length - 1);
            return array[index];
        }

        public int getID() {
            return id;
        }

        public boolean compare(int i) {
            return id == i;
        }
    }

    protected enum RegexStatus {
        OK,
        PATTERN_SYNTAX_ERROR,
        GROUP_OUT_OF_BOUNDS;

        /**
         * Provide standard error for pattern EditText
         *
         * @param c context for resources
         * @param status status of the regex
         * @return null if the regex is valid, the string for the error otherwise
         */
        public static String errorStringPattern(Context c, RegexStatus status) {
            switch (status) {
                case PATTERN_SYNTAX_ERROR:
                    return c.getString(R.string.rule_generic_regex_invalid_syntaxError);
                default:
                    return null;
            }
        }

        /**
         * Provide standard error for replacement EditText
         *
         * @param c context for resources
         * @param status status of the regex
         * @return null if the regex is valid, the string for the error otherwise
         */
        public static String errorStringReplacement(Context c, RegexStatus status) {
            switch (status) {
                case GROUP_OUT_OF_BOUNDS:
                    return c.getString(R.string.rule_generic_regex_invalid_outOfBounds);
                default:
                    return null;
            }
        }
    }
}
