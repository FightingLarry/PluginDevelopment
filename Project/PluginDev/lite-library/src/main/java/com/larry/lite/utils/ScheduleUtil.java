package com.larry.lite.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.larry.lite.LitePluginService;

public class ScheduleUtil {
    private static final String TAG = "ScheduleUtil";
    private static final long TIME_REPEAT_PERIOD = 30 * 60 * 1000; // 30分钟


    /**
     * @param context
     * @param action
     * @param requestCode
     * @param startTime    毫秒
     * @param repeatPeriod 毫秒，如果<=0，则执行不repeat的Alarm
     */
    public static void startAlarmSchedule(Context context, String action, Bundle bundle, int requestCode,
                                          long startTime, long repeatPeriod) {
        try {
            Intent intent = new Intent(context, LitePluginService.class);
            intent.setAction(action);
            if (bundle != null) {
                intent.putExtras(bundle);
            }
            PendingIntent sender =
                    PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (startTime < System.currentTimeMillis()) {
                // 时间过去了，则过一秒执行
                startTime = System.currentTimeMillis() + 1000;
            }
            if (repeatPeriod > 0) {
                if (repeatPeriod < TIME_REPEAT_PERIOD) {
                    repeatPeriod = TIME_REPEAT_PERIOD;
                }
                am.setRepeating(AlarmManager.RTC, startTime, repeatPeriod, sender);
            } else {
                am.set(AlarmManager.RTC, startTime, sender);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void stopAlarmSchedule(Context context, String action, Bundle bundle, int requestCode) {
        try {
            Intent intent = new Intent(context, LitePluginService.class);
            intent.setAction(action);
            if (bundle != null) {
                intent.putExtras(bundle);
            }
            PendingIntent sender =
                    PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.cancel(sender);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
