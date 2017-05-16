package com.larry.lite.host;

import com.tcl.dc.PLog;


public class TbksLogger implements PLog.Logger {
    @Override
    public boolean isDebug() {
        return NLog.isDebug();
    }

    @Override
    public void v(String tag, String format, Object... args) {
        NLog.v(tag, format, args);
    }

    @Override
    public void i(String tag, String format, Object... args) {
        NLog.i(tag, format, args);
    }

    @Override
    public void d(String tag, String format, Object... args) {
        NLog.d(tag, format, args);
    }

    @Override
    public void w(String tag, String format, Object... args) {
        NLog.w(tag, format, args);
    }

    @Override
    public void e(String tag, String format, Object... args) {
        NLog.e(tag, format, args);
    }
}
