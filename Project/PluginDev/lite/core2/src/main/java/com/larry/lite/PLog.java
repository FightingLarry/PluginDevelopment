
package com.larry.lite;

import android.util.Log;

public class PLog {
    private static final String TAG = "AppPlugins";
    private static final PLog.Logger DEFAULT = new PLog.Logger() {
        public boolean isDebug() {
            return true;
        }

        public void v(String tag, String format, Object... args) {}

        public void i(String tag, String format, Object... args) {}

        public void d(String tag, String format, Object... args) {}

        public void w(String tag, String format, Object... args) {}

        public void e(String tag, String format, Object... args) {}
    };
    private static PLog.Logger logger;

    public PLog() {}

    public static boolean isDebug() {
        return logger.isDebug();
    }

    public static void v(String format, Object... args) {
        logger.v("AppPlugins", format, args);
    }

    public static void vIf(boolean condition, String format, Object... args) {
        if (condition) {
            logger.v("AppPlugins", format, args);
        }

    }

    public static void i(String format, Object... args) {
        logger.i("AppPlugins", format, args);
    }

    public static void iIf(boolean condition, String format, Object... args) {
        if (condition) {
            logger.i("AppPlugins", format, args);
        }

    }

    public static void d(String format, Object... args) {
        logger.d("AppPlugins", format, args);
    }

    public static void dIf(boolean condition, String format, Object... args) {
        if (condition) {
            logger.d("AppPlugins", format, args);
        }

    }

    public static void w(String format, Object... args) {
        logger.w("AppPlugins", format, args);
    }

    public static void wIf(boolean condition, String format, Object... args) {
        if (condition) {
            logger.w("AppPlugins", format, args);
        }

    }

    public static void e(String format, Object... args) {
        logger.e("AppPlugins", format, args);
    }

    public static void eIf(boolean condition, String format, Object... args) {
        if (condition) {
            logger.e("AppPlugins", format, args);
        }

    }

    public static void printStackTrace(Throwable e) {
        if (logger.isDebug()) {
            String s = Log.getStackTraceString(e);
            logger.e("AppPlugins", s, new Object[0]);
        }

    }

    public static void setLogger(PLog.Logger l) {
        if (l == null) {
            logger = DEFAULT;
        } else {
            logger = l;
        }

    }

    static {
        logger = DEFAULT;
    }

    public interface Logger {
        boolean isDebug();

        void v(String var1, String var2, Object... var3);

        void i(String var1, String var2, Object... var3);

        void d(String var1, String var2, Object... var3);

        void w(String var1, String var2, Object... var3);

        void e(String var1, String var2, Object... var3);
    }
}
