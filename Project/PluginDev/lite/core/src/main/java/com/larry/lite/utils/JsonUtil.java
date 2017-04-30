package com.larry.lite.utils;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {

    public static int getInt(JSONObject data, String key) throws JSONException {
        if (data == null || TextUtils.isEmpty(key)) {
            return -1;
        }
        if (data.has(key)) {
            return data.getInt(key);
        }
        return -1;
    }

    public static String getString(JSONObject data, String key) throws JSONException {
        if (data == null || TextUtils.isEmpty(key)) {
            return "";
        }
        if (data.has(key)) {
            return data.getString(key);
        }
        return "";
    }

    public static boolean getBoolean(JSONObject data, String key) throws JSONException {
        if (data == null || TextUtils.isEmpty(key)) {
            return false;
        }
        if (data.has(key)) {
            return data.getBoolean(key);
        }
        return false;
    }

    public static long getLong(JSONObject data, String key) throws JSONException {
        if (data == null || TextUtils.isEmpty(key)) {
            return -1;
        }
        if (data.has(key)) {
            return data.getLong(key);
        }
        return -1;
    }

    public static double getDouble(JSONObject data, String key) throws JSONException {
        if (data == null || TextUtils.isEmpty(key)) {
            return -1;
        }
        if (data.has(key)) {
            return data.getDouble(key);
        }
        return -1;
    }

}
