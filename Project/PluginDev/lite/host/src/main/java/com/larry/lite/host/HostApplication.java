package com.larry.lite.host;

import android.app.Application;

import com.larry.lite.LitePluginSDK;

/**
 * Created by Larry on 2017/3/2.
 */

public class HostApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            HostLog.setDebug(true, HostLogger.VERBOSE);
            HostLog.trace(HostLogger.TRACE_REALTIME, null);
        } else {
            HostLog.setDebug(false, HostLogger.VERBOSE);
        }


        LitePluginSDK.init(getApplicationContext());
    }
}
