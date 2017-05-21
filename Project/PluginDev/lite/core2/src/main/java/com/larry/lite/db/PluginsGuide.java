package com.larry.lite.db;

import java.util.List;

/**
 * Created by Larry on 2017/5/21.
 */
public class PluginsGuide {

    List<PluginEntity> plugins;

    PluginGuideType type;

    long ts;


    public List<PluginEntity> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<PluginEntity> plugins) {
        this.plugins = plugins;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }
}
