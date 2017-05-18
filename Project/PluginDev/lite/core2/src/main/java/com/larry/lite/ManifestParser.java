
package com.larry.lite;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class ManifestParser {
    private static final String PLUGIN_MODULE_VALUE = "PluginModule";
    private final Context context;

    public ManifestParser(Context context) {
        this.context = context;
    }

    public List<PluginModule> parse() {
        ArrayList modules = new ArrayList();

        try {
            ApplicationInfo appInfo =
                    this.context.getPackageManager().getApplicationInfo(this.context.getPackageName(), 128);
            if (appInfo.metaData != null) {
                Iterator var3 = appInfo.metaData.keySet().iterator();

                while (var3.hasNext()) {
                    String key = (String) var3.next();
                    if ("PluginModule".equals(appInfo.metaData.get(key))) {
                        modules.add(parseModule(key));
                    }
                }
            }

            return modules;
        } catch (NameNotFoundException var5) {
            throw new RuntimeException("Unable to find metadata to parse PluginModule", var5);
        }
    }

    private static PluginModule parseModule(String className) {
        Class clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException var6) {
            throw new IllegalArgumentException("Unable to find PluginModule implementation", var6);
        }

        Object module;
        try {
            module = clazz.newInstance();
        } catch (InstantiationException var4) {
            throw new RuntimeException("Unable to instantiate PluginModule implementation for " + clazz, var4);
        } catch (IllegalAccessException var5) {
            throw new RuntimeException("Unable to instantiate PluginModule implementation for " + clazz, var5);
        }

        if (!(module instanceof PluginModule)) {
            throw new RuntimeException("Expected instanceof PluginModule, but found: " + module);
        } else {
            return (PluginModule) module;
        }
    }
}
