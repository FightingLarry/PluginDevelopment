//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.tcl.dc.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public final class PrefsUtils {
    private static SharedPreferences pref = null;
    private static Editor editor = null;

    public PrefsUtils() {}

    public static SharedPreferences pref(Context context) {
        if (pref == null) {
            pref = PreferenceManager.getDefaultSharedPreferences(context);
        }

        return pref;
    }

    public static String loadPrefString(Context context, String key) {
        return loadPrefString(context, key, (String) null);
    }

    public static String loadPrefString(Context context, String key, String defaultValue) {
        return pref(context).getString(key, defaultValue);
    }

    public static void savePrefString(Context context, String key, String value) {
        SharedPreferences sp = pref(context);
        sp.edit().putString(key, value).apply();
    }

    public static int loadPrefInt(Context context, String key, int defaultValue) {
        return pref(context).getInt(key, defaultValue);
    }

    public static void savePrefInt(Context context, String key, int value) {
        SharedPreferences sp = pref(context);
        sp.edit().putInt(key, value).apply();
    }

    public static long loadPrefLong(Context context, String key, long defaultValue) {
        return pref(context).getLong(key, defaultValue);
    }

    public static void savePrefLong(Context context, String key, long value) {
        SharedPreferences sp = pref(context);
        sp.edit().putLong(key, value).apply();
    }

    public static float loadPrefFloat(Context context, String key, float defaultValue) {
        return pref(context).getFloat(key, defaultValue);
    }

    public static void savePrefFloat(Context context, String key, float value) {
        SharedPreferences sp = pref(context);
        sp.edit().putFloat(key, value).apply();
    }

    public static boolean loadPrefBoolean(Context context, String key, boolean defaultValue) {
        return pref(context).getBoolean(key, defaultValue);
    }

    public static void savePrefBoolean(Context context, String key, boolean value) {
        SharedPreferences sp = pref(context);
        sp.edit().putBoolean(key, value).apply();
    }
}
