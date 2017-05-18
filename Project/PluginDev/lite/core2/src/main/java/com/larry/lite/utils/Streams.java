
package com.larry.lite.utils;

import android.database.Cursor;

import com.larry.lite.PLog;

import java.io.Closeable;
import java.io.IOException;

public final class Streams {
    public Streams() {}

    public static <T> void safeClose(T is) {
        if (is != null) {
            try {
                if (is instanceof Closeable) {
                    ((Closeable) is).close();
                } else if (is instanceof Cursor) {
                    ((Cursor) is).close();
                }
            } catch (IOException var2) {
                PLog.printStackTrace(var2);
            }

        }
    }
}
