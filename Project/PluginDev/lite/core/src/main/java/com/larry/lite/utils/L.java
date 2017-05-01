package com.larry.lite.utils;

import android.util.Log;

/**
 * Created by Larry on 2017/4/17.
 */

public class L {

    public static final boolean isDebug = true;

    public static void d(String tag, String message) {
        d(tag, message, isDebug);
    }

    public static void v(String tag, String message) {
        v(tag, message, isDebug);
    }

    public static void i(String tag, String message) {
        i(tag, message, isDebug);
    }

    public static void w(String tag, String message) {
        w(tag, message, isDebug);
    }

    public static void e(String tag, String message) {
        e(tag, message, isDebug);
    }

    public static void v(String tag, String message, boolean needLog) {
        if (needLog) {
            Log.v(tag, message);
        }
    }

    public static void d(String tag, String message, boolean needLog) {
        if (needLog) {
            Log.d(tag, message);
        }

    }

    public static void i(String tag, String message, boolean needLog) {
        if (needLog) {
            Log.i(tag, message);
        }

    }

    public static void w(String tag, String message, boolean needLog) {
        if (needLog) {
            Log.w(tag, message);
        }

    }

    public static void e(String tag, String message, boolean needLog) {
        if (needLog) {
            Log.e(tag, message);
        }

    }

}
