//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.tcl.dc;

import com.tcl.dc.base.DCPlugin;

public interface PluginRuntime {
    void checkPluginsForLaunch(TriggerEvent var1, Object var2);

    ClassLoader createClassLoader(PluginStub var1) throws Exception;

    DCPlugin loadPlugin(ClassLoader var1, PluginStub var2) throws Exception;

    void destroy();
}
