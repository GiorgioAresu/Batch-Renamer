package com.giorgioaresu.batchrenamer;

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

public abstract class Action implements Parcelable {
    static final String KEY_CONTENT = "Content";
    static final String KEY_TYPE = "Type";

    protected Context context;
    private String title;
    private int viewId;

    public Action(Context context, String title, int viewId) {
        this.context = context;
        this.title = title;
        this.viewId = viewId;
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
     * @param currentName   current string
     * @param positionInSet position of the string in the original set (useful for counters)
     * @return the new string
     */
    public abstract String getNewName(String currentName, int positionInSet);

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
     * @return R.String.empty_field_label if string is empty, the string itself otherwise
     */
    protected String checkForEmpty(String string) {
        return string.isEmpty() ? context.getString(R.string.empty_field_label) : string;
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
}
