
package com.tcl.dc;

public interface PluginLifecycleCallback {
    void onPluginCreate(PluginStub var1);

    void onPluginStart(PluginStub var1);

    void onPluginEnd(PluginStub var1, int var2, Object var3);

    void onPluginDestroy(PluginStub var1);
}
