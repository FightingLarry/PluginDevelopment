package com.larry.lite.plugin;

import android.util.Log;

import com.larry.lite.base.ConnectionFactory;
import com.larry.lite.base.LitePlugin;
import com.larry.lite.base.PluginPeer;

import java.io.IOException;


/**
 * Created by Larry on 2017/3/2.
 */
public class MyPlugin implements LitePlugin {

    private static final String TAG = "MyPlugin";

    @Override
    public void onCreate(PluginPeer pluginPeer) {

    }

    @Override
    public int execute(PluginPeer pluginPeer, ConnectionFactory connectionFactory) throws IOException {

        Log.d(TAG, "execute");

        return 0;
    }

    @Override
    public void onDestroy(PluginPeer pluginPeer) {
        Log.d(TAG, "onCreate");
    }
}
