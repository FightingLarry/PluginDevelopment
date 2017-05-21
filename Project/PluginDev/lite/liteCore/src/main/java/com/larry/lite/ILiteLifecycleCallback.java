
package com.larry.lite;

public interface ILiteLifecycleCallback {
    void onPluginCreate(LiteStub var1);

    void onPluginStart(LiteStub var1);

    void onPluginEnd(LiteStub var1, int var2, Object var3);

    void onPluginDestroy(LiteStub var1);
}
