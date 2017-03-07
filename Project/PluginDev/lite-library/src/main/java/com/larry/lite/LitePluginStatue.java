package com.larry.lite;

import android.content.Context;
import android.content.IntentFilter;

import com.larry.lite.utils.ScheduleUtil;

/**
 * Created by Larry on 2017/3/6.
 */

class LitePluginStatue {

    private static LitePluginStatue instance;

    private Context mContext;
    private NetworkChangeReceiver mNetworkChangeReceive;

    public synchronized static LitePluginStatue getInstance(Context context) {
        if (instance == null) {
            instance = new LitePluginStatue(context);
        }
        return instance;
    }


    private LitePluginStatue(Context context) {
        this.mContext = context.getApplicationContext();

    }


    public void onCreate() {

        initAlarm();
        registerNetWorkReceiver();

    }

    public void onDestroy() {

        cancelAlarm();
        unregisterNetWorkReceiver();

    }


    private void initAlarm() {
        //3个小时一次
        ScheduleUtil.startAlarmSchedule(mContext, LitePluginService.ACTION_CHECK_PLUGIN, null, 1,
                0, 10800000);
    }

    private void cancelAlarm() {
        ScheduleUtil.stopAlarmSchedule(mContext, LitePluginService.ACTION_CHECK_PLUGIN, null, 1);
    }


    private void registerNetWorkReceiver() {
        mNetworkChangeReceive = new NetworkChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        mContext.registerReceiver(mNetworkChangeReceive, intentFilter);
    }


    private void unregisterNetWorkReceiver() {
        if (mNetworkChangeReceive != null) {
            mContext.unregisterReceiver(mNetworkChangeReceive);
        }
    }


}
