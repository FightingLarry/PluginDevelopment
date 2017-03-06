package com.larry.lite;

import android.content.Intent;
import android.os.IBinder;

import com.larry.lite.utils.GrayService;

/**
 * Created by Larry on 2017/3/2.
 */

public class LitePluginService extends GrayService {


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }


}
