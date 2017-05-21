
package com.larry.lite;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class LiteParser {
    private static final String PLUGIN_MODULE_VALUE = "LiteModule";
    private final Context context;

    public LiteParser(Context context) {
        this.context = context;
    }

    public List<LiteModule> parse() {
        ArrayList modules = new ArrayList();

        try {
            ApplicationInfo appInfo =
                    this.context.getPackageManager().getApplicationInfo(this.context.getPackageName(), 128);
            if (appInfo.metaData != null) {
                Iterator var3 = appInfo.metaData.keySet().iterator();

                while (var3.hasNext()) {
                    String key = (String) var3.next();
                    if ("LiteModule".equals(appInfo.metaData.get(key))) {
                        modules.add(parseModule(key));
                    }
                }
            }

            return modules;
        } catch (NameNotFoundException var5) {
            throw new RuntimeException("Unable to find metadata to parse LiteModule", var5);
        }
    }

    private static LiteModule parseModule(String className) {
        Class clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException var6) {
            throw new IllegalArgumentException("Unable to find LiteModule implementation", var6);
        }

        Object module;
        try {
            module = clazz.newInstance();
        } catch (InstantiationException var4) {
            throw new RuntimeException("Unable to instantiate LiteModule implementation for " + clazz, var4);
        } catch (IllegalAccessException var5) {
            throw new RuntimeException("Unable to instantiate LiteModule implementation for " + clazz, var5);
        }

        if (!(module instanceof LiteModule)) {
            throw new RuntimeException("Expected instanceof LiteModule, but found: " + module);
        } else {
            return (LiteModule) module;
        }
    }
}
