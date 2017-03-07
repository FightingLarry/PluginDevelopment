package com.larry.lite;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Larry on 2017/3/2.<br/>
 * LitePlugin is a multi process, Application#onCreate will perform two times
 */

public class LitePlugin {

    public static final void init(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, LitePluginService.class);
        context.startService(intent);
    }

    /**
     * 默认
     */
    public static final void customCheckAssetPlugin(CheckAssetPlugin checkAssetPlugin) {


    }


}
