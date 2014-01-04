package com.giorgioaresu.batchrenamer.actions;

import android.content.Context;
import android.os.Parcel;

import com.giorgioaresu.batchrenamer.Action;
import com.giorgioaresu.batchrenamer.R;

import org.json.JSONException;
import org.json.JSONObject;

public class Add extends Action {
    String text = "prova";
    int position = 5;
    boolean fromStart = true;

    public Add(Context context) {
        super(context.getString(R.string.action_add_title), R.layout.action_tab_add, R.layout.action_tab_add_edit);
    }

    public String GetNewName(String currentName) {
        int pos;

        if (fromStart) {
            pos = position;
        } else {
            pos = currentName.length() - position - 1;
        }

        return currentName.substring(0, pos).concat(text).concat(currentName.substring(pos));
    }

    @Override
    public int getViewType() {
        return ActionIDS.ADD;
    }

    @Override
    protected JSONObject serializeToJSON() throws JSONException {
        JSONObject jObject = new JSONObject();
        jObject.put("Text", text);
        jObject.put("Position", position);
        jObject.put("FromStart", fromStart);
        return jObject;
    }

    @Override
    protected void serializeFromJSON(JSONObject jObject) throws JSONException {
        text = jObject.getString("Text");
        position = jObject.getInt("Position");
        fromStart = jObject.getBoolean("FromStart");
    }

    @Override
    protected void createFromParcel(Parcel in) {
        text = in.readString();
        position = in.readInt();
        fromStart = in.readByte() != 0 ? true : false;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(text);
        parcel.writeInt(position);
        parcel.writeByte((byte) (fromStart ? 1 : 0));
    }
}
