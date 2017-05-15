package com.tcl.dc.base;

import java.io.IOException;

public interface DCPlugin {
    void onCreated(PluginPeer var1);

    int execute(PluginPeer var1, ConnectionFactory var2) throws IOException;

    void onDestroy(PluginPeer var1);
}
