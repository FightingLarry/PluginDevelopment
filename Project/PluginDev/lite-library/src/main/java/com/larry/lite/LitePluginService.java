package com.larry.lite;

import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

import com.larry.lite.utils.GrayService;
import com.larry.taskflows.TaskManager;

/**
 * Created by Larry on 2017/3/2.
 */

public class LitePluginService extends GrayService {

    public static final String ACTION_CHECK_PLUGIN = "com.larry.lite.action_check_plugin";


    @Override
    public void onCreate() {
        super.onCreate();
        LitePluginStatue.getInstance(getApplicationContext()).onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent == null || TextUtils.isEmpty(intent.getAction())) {
            return START_STICKY;
        }

        if (ACTION_CHECK_PLUGIN.equals(intent.getAction())) {
            TaskManager.runOnWorkerThread(new CheckPluginTask(getApplicationContext()));
        }


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LitePluginStatue.getInstance(getApplicationContext()).onDestroy();
    }
}
