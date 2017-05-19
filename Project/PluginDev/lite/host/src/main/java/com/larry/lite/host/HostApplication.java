package com.larry.lite.host;

import android.app.Application;

import com.larry.lite.LitePlugin;

/**
 * Created by Larry on 2017/3/2.
 */

public class HostApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            NLog.setDebug(true, Logger.VERBOSE);
            NLog.trace(Logger.TRACE_REALTIME, null);
        } else {
            NLog.setDebug(false, Logger.VERBOSE);
        }


        LitePlugin.init(getApplicationContext());
    }
}
