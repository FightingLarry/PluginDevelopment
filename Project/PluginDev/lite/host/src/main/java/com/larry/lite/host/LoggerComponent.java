package com.larry.lite.host;

import android.util.Log;

import com.larry.lite.LiteLog;


public class LoggerComponent implements LiteLog.Logger {
    @Override
    public boolean isDebug() {
        return BuildConfig.DEBUG;
    }

    @Override
    public void v(String tag, String format, Object... args) {
        Log.v(tag, buildWholeMessage(format, args));
    }

    @Override
    public void i(String tag, String format, Object... args) {
        Log.i(tag, buildWholeMessage(format, args));
    }

    @Override
    public void d(String tag, String format, Object... args) {
        Log.d(tag, buildWholeMessage(format, args));
    }

    @Override
    public void w(String tag, String format, Object... args) {
        Log.w(tag, buildWholeMessage(format, args));
    }

    @Override
    public void e(String tag, String format, Object... args) {
        Log.e(tag, buildWholeMessage(format, args));
    }


    private static String buildWholeMessage(String format, Object... args) {
        if (args != null && args.length != 0) {
            String msg = String.format(format, args);
            return msg;
        } else {
            return format;
        }
    }

}
