package com.larry.lite;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.larry.lite.utils.AndroidUtil;


public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (AndroidUtil.isNetConnect(context)) {
            //TODO
            LitePlugin.init(context);
        }
    }
}
