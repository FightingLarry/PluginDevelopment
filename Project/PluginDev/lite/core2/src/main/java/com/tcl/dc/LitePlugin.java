package com.tcl.dc;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Larry on 2017/5/16.
 */

public class LitePlugin {

    public static void init(Context context) {
        PLog.d("LitePlugin#init()");
        Intent intent = new Intent(context, PluginService.class);
        intent.setAction(TriggerEvent.KeyEventStart.name());
        context.startService(intent);
    }

    public static void pumpEvent(Context context, TriggerEvent event) {
        PluginService.pumpEvent(context, event);
    }


}
