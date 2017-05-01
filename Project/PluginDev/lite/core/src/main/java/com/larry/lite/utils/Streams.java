package com.larry.lite.utils;

import android.database.Cursor;

import java.io.Closeable;
import java.io.IOException;

public final class Streams {
    public static <T> void safeClose(T is) {
        if (is == null) return;
        try {
            if ((is instanceof Closeable)) {
                ((Closeable) is).close();
            } else if ((is instanceof Cursor)) ((Cursor) is).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
