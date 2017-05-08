package com.larry.lite;


import com.larry.lite.module.PluginStub;

import java.util.List;

public interface PluginManager {
    boolean loadPlugins();

    void add(PluginStub paramPluginStub);

    List<PluginStub> syncWithConfiguration(PluginConfiguration paramPluginConfiguration);

    List<PluginStub> getAllPlugins();

    boolean savePlugin(PluginStub paramPluginStub);

    void destroy();

    boolean requestReady(PluginStub paramPluginStub, PluginReadyCallback paramPluginReadyCallback);

    static interface PluginReadyCallback {
        void onReady(PluginStub paramPluginStub);

        void onFail(PluginStub paramPluginStub);
    }
}

/*
 * Location: F:\MyWork\TCL\Tracker\2.0\fox-core-1.0.2.jar Qualified Name: com.tcl.dc.PluginManager
 * JD-Core Version: 0.6.0
 */
