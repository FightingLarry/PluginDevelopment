package com.larry.lite;

import android.content.Context;

public class CheckPluginManager {

    private static CheckPlugin sCheckPlugins;

    private CheckPluginManager() {

    }

    public synchronized static CheckPlugin getInstance() {
        if (sCheckPlugins == null) {
            sCheckPlugins = new CheckPlugin();
        }
        return sCheckPlugins;
    }

    public static void init(Context context) {
        getInstance().clear();
        getInstance().addCheckPlugin(new CheckAssetPlugin(context));
        getInstance().addCheckPlugin(new CheckNetPlugin(context));
    }

    public static void add(ICheckPlugin checkPlugin) {
        getInstance().addCheckPlugin(checkPlugin);
    }

}
