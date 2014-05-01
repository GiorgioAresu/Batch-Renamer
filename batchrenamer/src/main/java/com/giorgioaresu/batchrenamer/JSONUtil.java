package com.giorgioaresu.batchrenamer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtil {
    public static final int POSITION_INVALID = -1;
    /**
     * Scan a JSONArray searching for a JSONObject with specified value for key
     * @param array JSONArray to be scanned
     * @param key String to be searched in keys
     * @param value String looked for in key
     * @return the position of the element if found, -1 otherwise
     * @throws JSONException
     */
    public static int indexOf(JSONArray array, String key, String value) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            String v = obj.getString(key);
            if (v != null && v.equals(value)) {
                return i;
            }
        }
        return POSITION_INVALID;
    }

    public static boolean has(JSONArray array, String key, String value) throws JSONException {
        return indexOf(array, key, value) != POSITION_INVALID;
    }

    public static JSONArray remove(JSONArray array, int index) throws JSONException {
        JSONArray res = new JSONArray();
        for (int i=0; i<array.length(); i++) {
            if (index != i) {
                res.put(array.get(i));
            }
        }
        return res;
    }
}
