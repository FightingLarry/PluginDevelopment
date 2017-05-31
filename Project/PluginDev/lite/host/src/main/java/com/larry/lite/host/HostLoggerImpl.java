package com.larry.lite.host;

import com.larry.lite.LiteLog;


public class HostLoggerImpl implements LiteLog.Logger {
    @Override
    public boolean isDebug() {
        return HostLog.isDebug();
    }

    @Override
    public void v(String tag, String format, Object... args) {
        HostLog.v(tag, format, args);
    }

    @Override
    public void i(String tag, String format, Object... args) {
        HostLog.i(tag, format, args);
    }

    @Override
    public void d(String tag, String format, Object... args) {
        HostLog.d(tag, format, args);
    }

    @Override
    public void w(String tag, String format, Object... args) {
        HostLog.w(tag, format, args);
    }

    @Override
    public void e(String tag, String format, Object... args) {
        HostLog.e(tag, format, args);
    }
}
