
package com.larry.lite;

import com.tcl.lite.base.LitePlugin;

public interface PluginRuntime {
    void checkPluginsForLaunch(TriggerEvent var1, Object var2);

    ClassLoader createClassLoader(PluginStub var1) throws Exception;

    LitePlugin loadPlugin(ClassLoader var1, PluginStub var2) throws Exception;

    void destroy();
}
