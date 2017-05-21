package com.larry.lite.plugin;

import android.util.Log;

import com.larry.lite.base.LiteConnectionFactory;
import com.larry.lite.base.LitePlugin;
import com.larry.lite.base.LitePluginPeer;

import java.io.IOException;


/**
 * Created by Larry on 2017/3/2.
 */
public class MyPlugin implements LitePlugin {

    private static final String TAG = "MyPlugin";

    @Override
    public void onCreate(LitePluginPeer litePluginPeer) {

    }

    @Override
    public int execute(LitePluginPeer litePluginPeer, LiteConnectionFactory liteConnectionFactory) throws IOException {

        Log.d(TAG, "execute");

        return 0;
    }

    @Override
    public void onDestroy(LitePluginPeer litePluginPeer) {
        Log.d(TAG, "onCreate");
    }
}
