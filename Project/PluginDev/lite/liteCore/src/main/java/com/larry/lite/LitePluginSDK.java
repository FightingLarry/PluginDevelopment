package com.larry.lite;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Larry on 2017/5/16.
 */

public class LitePluginSDK {

    public static void init(Context context) {
        LiteLog.d("LitePluginSDK#init()");
        Intent intent = new Intent(context, LiteService.class);
        intent.setAction(LiteEvent.KeyEventStart.name());
        context.startService(intent);
    }

    public static void pumpEvent(Context context, LiteEvent event) {
        LiteService.pumpEvent(context, event);
    }


}
