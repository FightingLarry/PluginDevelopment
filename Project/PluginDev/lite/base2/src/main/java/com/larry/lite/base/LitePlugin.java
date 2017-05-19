package com.larry.lite.base;

import java.io.IOException;

public interface LitePlugin {

    void onCreate(PluginPeer pluginPeer);

    int execute(PluginPeer pluginPeer, ConnectionFactory connectionFactory) throws IOException;

    void onDestroy(PluginPeer pluginPeer);

}
