package com.larry.lite.plugin;

import android.util.Log;

import com.larry.lite.ILitePlugin;

import java.io.IOException;


/**
 * Created by Larry on 2017/3/2.
 */
public class MyPlugin implements ILitePlugin {

    private static final String TAG = "MyPlugin";

    @Override
    public void onCreated() {
        Log.d(TAG, "onCreated");
    }

    @Override
    public int execute() throws IOException {

        Log.d(TAG, "execute");

        return 0;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
    }
}
