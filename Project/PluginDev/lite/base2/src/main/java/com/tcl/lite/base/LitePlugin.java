package com.tcl.lite.base;

import java.io.IOException;

public interface LitePlugin {

    void onCreated(PluginPeer pluginPeer);

    int execute(PluginPeer pluginPeer, ConnectionFactory connectionFactory) throws IOException;

    void onDestroy(PluginPeer pluginPeer);

}
