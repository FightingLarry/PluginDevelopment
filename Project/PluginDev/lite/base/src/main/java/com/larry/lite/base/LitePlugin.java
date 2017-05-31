package com.larry.lite.base;

import java.io.IOException;

public interface LitePlugin {

    void onCreate(LitePluginPeer litePluginPeer);

    int execute(LitePluginPeer litePluginPeer, LiteConnectionFactory liteConnectionFactory) throws IOException;

    void onDestroy(LitePluginPeer litePluginPeer);

}
