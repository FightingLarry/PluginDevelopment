package com.larry.lite.host;

import android.app.Application;

import com.larry.lite.LitePlugin;

/**
 * Created by Larry on 2017/3/2.
 */

public class SampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        LitePlugin.init(getApplicationContext());
    }
}
