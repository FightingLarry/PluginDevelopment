package com.larry.lite;

import java.util.List;

/**
 * Created by Larry on 2017/5/21.
 */
public class LitePluginsConfigInfo {

    List<LiteStub> plugins;

    LiteConfigType type;

    long ts;


    public List<LiteStub> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<LiteStub> plugins) {
        this.plugins = plugins;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public LiteConfigType getType() {
        return type;
    }

    public void setType(LiteConfigType type) {
        this.type = type;
    }
}
