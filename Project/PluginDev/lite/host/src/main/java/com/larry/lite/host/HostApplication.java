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
        LitePluginSDK.init(getApplicationContext());
    }
}
