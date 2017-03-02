package com.larry.lite;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Larry on 2017/3/2.<br/>
 * LitePlugin is a multi process, Application#onCreate will perform two times
 */

public class LitePlugin {

    public static final void init(Context context) {

        context.startService(new Intent("com.larry.pluginlite.LitePluginService"));

    }


}
