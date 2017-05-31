
package com.larry.lite;

public class LiteLifecycleCallback implements ILiteLifecycleCallback {
    private final ILiteLifecycleCallback mCallback;

    public LiteLifecycleCallback(ILiteLifecycleCallback target) {
        this.mCallback = target;
    }

    public void onPluginCreate(LiteStub plugin) {
        if (this.mCallback != null) {
            this.mCallback.onPluginCreate(plugin);
        }

    }

    public void onPluginStart(LiteStub plugin) {
        if (this.mCallback != null) {
            this.mCallback.onPluginStart(plugin);
        }

    }

    public void onPluginEnd(LiteStub plugin, int err, Object extra) {
        if (this.mCallback != null) {
            this.mCallback.onPluginEnd(plugin, err, extra);
        }

    }

    public void onPluginDestroy(LiteStub plugin) {
        if (this.mCallback != null) {
            this.mCallback.onPluginDestroy(plugin);
        }

    }
}
