package com.larry.lite.host;

import android.util.Log;

import com.larry.lite.network.NetworkLogger;


public class HostUploader implements NetworkLogger.Uploader {
    @Override
    public void sendLog(String log, boolean batch) {
        Log.v("HostUploader", log);
    }
}
