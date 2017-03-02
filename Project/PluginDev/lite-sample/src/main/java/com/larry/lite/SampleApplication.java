package com.larry.lite;

import android.app.Application;

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
