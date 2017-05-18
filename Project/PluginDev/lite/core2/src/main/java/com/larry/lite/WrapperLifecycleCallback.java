
package com.larry.lite;

public class WrapperLifecycleCallback implements PluginLifecycleCallback {
    private final PluginLifecycleCallback mCallback;

    public WrapperLifecycleCallback(PluginLifecycleCallback target) {
        this.mCallback = target;
    }

    public void onPluginCreate(PluginStub plugin) {
        if (this.mCallback != null) {
            this.mCallback.onPluginCreate(plugin);
        }

    }

    public void onPluginStart(PluginStub plugin) {
        if (this.mCallback != null) {
            this.mCallback.onPluginStart(plugin);
        }

    }

    public void onPluginEnd(PluginStub plugin, int err, Object extra) {
        if (this.mCallback != null) {
            this.mCallback.onPluginEnd(plugin, err, extra);
        }

    }

    public void onPluginDestroy(PluginStub plugin) {
        if (this.mCallback != null) {
            this.mCallback.onPluginDestroy(plugin);
        }

    }
}
