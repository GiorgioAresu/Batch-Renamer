package com.giorgioaresu.batchrenamer;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;

public abstract class Action implements Parcelable {
    private String title;
    private int viewId;
    private int editViewId;

    public Action(String title, int viewId, int editViewId) {
        this.title = title;
        this.viewId = viewId;
        this.editViewId = editViewId;
    }

    public String getTitle() {
        return title;
    }

    public String[] GetNewNames(String[] currentNames) {
        int length = currentNames.length;
        String[] names = new String[length];

        for (int i = 0; i < length; i++)
            names[i] = GetNewName(currentNames[i]);

        return names;
    }

    public int getViewId() {
        return viewId;
    }

    public int getEditViewId() {
        return editViewId;
    }

    public abstract int getViewType();

    public abstract String GetNewName(String currentName);

    protected abstract JSONObject serializeToJSON() throws JSONException;

    protected abstract void serializeFromJSON(JSONObject jObject) throws JSONException;

    protected abstract void createFromParcel(Parcel in);

    public JSONObject dumpToJSON() {
        JSONObject jObject = new JSONObject();
        try {
            jObject.put("Content", serializeToJSON());
            jObject.put("Type", getClass().getName());
            return jObject;
        } catch (JSONException e) {
            Log.e("dumpToJSON", "Exception dumping item, skipping");
            return null;
        }
    }

    public static Action createFromJSON(JSONObject jObject, Context context) {
        try {
            Class<?> c = Class.forName(jObject.getString("Type"));
            Constructor<?> cons = c.getConstructors()[0];
            Action action = (Action) cons.newInstance(context);
            action.serializeFromJSON(jObject.getJSONObject("Content"));
            return action;
        } catch (Exception b) {
            Log.e("createFromJSON", "Exception creating item, skipping");
            return null;
        }
    }

    public static final Parcelable.Creator<Action> CREATOR
            = new Parcelable.Creator<Action>() {
        public Action createFromParcel(Parcel in) {
            try {
                Class<?> c = Class.forName(in.readString());
                Constructor<?> cons = c.getConstructors()[0];
                Action action = (Action) cons.newInstance();
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

    public int describeContents() {
        return 0;
    }

    public static final class ActionIDS {
        // Set to number of different IDS
        public static final int COUNT = 2;

        public static final int ADD = 0;
        public static final int REMOVE = 1;
    }
}
