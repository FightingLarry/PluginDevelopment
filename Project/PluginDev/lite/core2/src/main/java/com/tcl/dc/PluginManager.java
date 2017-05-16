
package com.tcl.dc;

import java.util.List;

public interface PluginManager {
    boolean loadPlugins();

    void add(PluginStub var1);

    List<PluginStub> syncWithConfiguration(PluginConfiguration var1);

    List<PluginStub> getAllPlugins();

    boolean savePlugin(PluginStub var1);

    void destroy();

    boolean requestReady(PluginStub var1, PluginManager.PluginReadyCallback var2);

    public interface PluginReadyCallback {
        void onReady(PluginStub var1);

        void onFail(PluginStub var1);
    }
}
