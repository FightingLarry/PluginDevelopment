
package com.larry.lite;

import java.util.List;

public interface LitePluginManager {
    boolean loadPlugins();

    void add(LiteStub var1);

    List<LiteStub> syncWithConfiguration(LiteConfiguration var1);

    List<LiteStub> getAllPlugins();

    boolean savePlugin(LiteStub var1);

    void destroy();

    boolean requestReady(LiteStub var1, LitePluginManager.PluginReadyCallback var2);

    public interface PluginReadyCallback {
        void onReady(LiteStub var1);

        void onFail(LiteStub var1);
    }
}
