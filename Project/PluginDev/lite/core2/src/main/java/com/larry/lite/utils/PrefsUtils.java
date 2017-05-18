
package com.larry.lite.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class PrefsUtils {

    private static SharedPreferences pref = null;

    private PrefsUtils() {}

    private static SharedPreferences pref(Context context) {
        if (pref == null) {
            pref = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return pref;
    }

    public static String getString(Context context, String key) {
        return getString(context, key, (String) null);
    }

    public static String getString(Context context, String key, String defaultValue) {
        return pref(context).getString(key, defaultValue);
    }

    public static void saveString(Context context, String key, String value) {
        SharedPreferences sp = pref(context);
        sp.edit().putString(key, value).apply();
    }

    public static int getInt(Context context, String key, int defaultValue) {
        return pref(context).getInt(key, defaultValue);
    }

    public static void saveInt(Context context, String key, int value) {
        SharedPreferences sp = pref(context);
        sp.edit().putInt(key, value).apply();
    }

    public static long getLong(Context context, String key, long defaultValue) {
        return pref(context).getLong(key, defaultValue);
    }

    public static void saveLong(Context context, String key, long value) {
        SharedPreferences sp = pref(context);
        sp.edit().putLong(key, value).apply();
    }

    public static float getFloat(Context context, String key, float defaultValue) {
        return pref(context).getFloat(key, defaultValue);
    }

    public static void saveFloat(Context context, String key, float value) {
        SharedPreferences sp = pref(context);
        sp.edit().putFloat(key, value).apply();
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        return pref(context).getBoolean(key, defaultValue);
    }

    public static void saveBoolean(Context context, String key, boolean value) {
        SharedPreferences sp = pref(context);
        sp.edit().putBoolean(key, value).apply();
    }
}
