package com.larry.lite;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import com.larry.lite.utils.ScheduleUtil;

import java.util.Calendar;

/**
 * Created by Larry on 2017/3/6.
 */

class LitePluginStatue {

    private static final String LITE_PREF = "lite_pref";

    private static final String KEY_TASK_STATUS = "LitePluginStatue.key_task_status";

    private static LitePluginStatue instance;

    private Context mContext;
    private NetworkChangeReceiver mNetworkChangeReceive;
    private SharedPreferences mPrefs;


    public synchronized static LitePluginStatue getInstance(Context context) {
        if (instance == null) {
            instance = new LitePluginStatue(context);
        }
        return instance;
    }


    private LitePluginStatue(Context context) {
        this.mContext = context.getApplicationContext();
        mPrefs = mContext.getSharedPreferences(LITE_PREF, Context.MODE_PRIVATE);

        CheckPluginManager.init(context);

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
        // 3个小时一次
        ScheduleUtil.startAlarmSchedule(mContext, LitePluginService.ACTION_CHECK_PLUGIN, null, 1, 0, 10800000);
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



    public String makeTaskStatus() {
        // 年月日保证了一天执行一次
        Calendar calendar = Calendar.getInstance();
        StringBuilder key = new StringBuilder();
        key.append(calendar.get(Calendar.YEAR)).append(calendar.get(Calendar.MONTH))
                .append(calendar.get(Calendar.DAY_OF_MONTH));
        return key.toString();
    }


    public void setTaskStatus(Context context, String value) {
        mPrefs.edit().putString(KEY_TASK_STATUS, value).commit();
    }

    public String getTaskStatus(Context context) {
        return mPrefs.getString(KEY_TASK_STATUS, "");
    }


}
