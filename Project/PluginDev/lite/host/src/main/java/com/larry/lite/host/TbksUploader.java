package com.larry.lite.host;

import android.util.Log;

import com.tcl.dc.network.PluginLogger;


public class TbksUploader implements PluginLogger.Uploader {
    @Override
    public void sendLog(String log, boolean batch) {
        Log.v("TbksUploader", log);
    }
}
