package com.larry.lite;

import android.content.Context;

/**
 * Created by Larry on 2017/3/6.
 */

class LitePluginStatue {

    private static LitePluginStatue instance;

    private Context mContext;


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


    }

    public void onDestroy() {

    }


}
