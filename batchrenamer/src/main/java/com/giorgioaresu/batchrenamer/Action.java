package com.giorgioaresu.batchrenamer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.TreeMap;

public abstract class Action implements Parcelable {
    public static final Parcelable.Creator<Action> CREATOR
            = new Parcelable.Creator<Action>() {
        public Action createFromParcel(Parcel in) {
            try {
                Class<?> c = Class.forName(in.readString());
                Constructor<?> cons = c.getConstructors()[0];
                Action action = (Action) cons.newInstance(Application.getContext());
                action.createFromParcel(in);
                return action;
            } catch (Exception b) {
                Log.e("createFromParcel", "Exception creating item, skipping");
                return null;
            }
        }

        public Action[] newArray(int size) {
            return new Action[size];
        }
    };
    static final String KEY_CONTENT = "Content";
    static final String KEY_TYPE = "Type";

    protected Activity context;
    private String title;
    private int viewId;

    public Action(Activity context, String title, int viewId) {
        this.context = context;
        this.title = title;
        this.viewId = viewId;
    }

    public static final Map<String, String> getActions(Context context) {
        Map<String, String> actions = new TreeMap<>();
        actions.put(context.getString(R.string.actioncard_add_title), "Add");
        actions.put(context.getString(R.string.actioncard_remove_title), "Remove");
        actions.put(context.getString(R.string.actioncard_renumber_title), "Renumber");
        actions.put(context.getString(R.string.actioncard_replace_title), "Replace");
        return actions;
    }

    /**
     * Creates an action from content described in a JSONObject
     *
     * @param context Context used to instantiate the action
     * @param jObject JSONObject containing action description
     * @return
     */
    public static final Action createFromJSON(Context context, JSONObject jObject) {
        try {
            Class<?> c = Class.forName(jObject.getString(KEY_TYPE));
            Constructor<?> cons = c.getConstructors()[0];
            Action action = (Action) cons.newInstance(context);
            action.deserializeFromJSON(jObject.getJSONObject(KEY_CONTENT));
            return action;
        } catch (Exception b) {
            Log.e("createFromJSON", "Exception creating item, skipping");
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
     * Apply action to a string
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
     * Get the preview of a view, populated with current data from action.
     * Due to some problems with match_parent in layout not been applied when inflating
     * without a parent view, the width must be supplied to this method
     * (ie. when the preview has to be showed in a ImageView, pass iv.getMeasuredWidth()
     * as parameter).
     *
     * @param width the width of the resulting Bitmap
     * @return the Bitmap of the preview
     */
    public Bitmap getBitmapOfView(int width) {
        // TODO: Speed up bitmap creation (ie. keep an inflated view) to avoid lag on scrolling big lists
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
     * Describes the action content in a string (ie. for accessibility purposes)
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
        return string.isEmpty() ? context.getString(R.string.actioncard_empty_field_contentdescription) : string;
    }

    /**
     * Returns the string for a boolean value
     *
     * @param v boolean value
     * @return android.R.string.yes if v is true, android.R.string.no otherwise
     */
    protected String getValueToString(boolean v) {
        return context.getString(v ? android.R.string.yes : android.R.string.no);
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
     * Dump action to a JSONObject
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
            Log.e("dumpToJSON", "Exception dumping item, skipping");
            return null;
        }
    }

    /**
     * Dump action fields to a JSONObject
     *
     * @return JSONObject created
     * @throws JSONException
     */
    protected abstract JSONObject serializeToJSON() throws JSONException;

    /**
     * Restore action status from content described in a JSONObject
     *
     * @param jObject JSONObject containing action description
     * @throws JSONException
     */
    protected abstract void deserializeFromJSON(JSONObject jObject) throws JSONException;

    /**
     * Dump action to a Parcel
     *
     * @param parcel Parcel to dump action to
     * @param i
     */
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getClass().getName());
        dumpToParcel(parcel, i);
    }

    /**
     * Dump action fields to a Parcel
     *
     * @param parcel Parcel to dump action to
     * @param i
     */
    public abstract void dumpToParcel(Parcel parcel, int i);

    /**
     * Restore action status from content described in a Parcel
     *
     * @param in Parcel containing action description
     */
    protected abstract void createFromParcel(Parcel in);

    public int describeContents() {
        return 0;
    }

    /**
     * Call when inflating layout to let the action prepare the UI (ie. attach listeners)
     * @param view the inflated view for the action
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
            ApplyTo[] As = ApplyTo.values();
            for (int i = 0; i < As.length; i++) {
                if (As[i].compare(_id))
                    return As[i];
            }
            // Value not recognized. Just return default value.
            return BOTH;
        }

        public static String getLabel(Context context, ApplyTo applyTo) {
            String[] applyString = context.getResources().getStringArray(R.array.actioncard_apply_array);
            int index = Math.min(applyTo.id, applyString.length - 1);
            return applyString[index];
        }

        public int getID() {
            return id;
        }

        public boolean compare(int i) {
            return id == i;
        }
    }
}
