
package com.larry.lite;

import com.larry.lite.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LiteConfiguration {
    List<LiteStub> plugins = new ArrayList();
    private long ts;
    private long lastUpdateTimestamp;

    public LiteConfiguration() {}

    public long getLastUpdateTimestamp() {
        return this.lastUpdateTimestamp;
    }

    public long getTs() {
        return this.ts;
    }

    public List<LiteStub> getPlugins() {
        return this.plugins;
    }

    public void update(List<LiteStub> plugins, long t) {
        this.plugins.clear();
        if (!CollectionUtils.isEmpty(plugins)) {
            this.plugins.addAll(plugins);
        }

        this.lastUpdateTimestamp = System.currentTimeMillis();
        this.ts = t;
    }

    void syncTo(LitePluginManager manager) {
        List<LiteStub> updates = manager.syncWithConfiguration(this);
        if (!CollectionUtils.isEmpty(updates)) {
            this.plugins.clear();
            this.plugins.addAll(updates);
        }

    }

    void delete(LiteStub plugin) {
        this.plugins.remove(plugin);
    }

    void print() {
        LiteLog.i("====configuration start====", new Object[0]);
        LiteLog.i("lastUpdateTimestamp: %d", new Object[] {Long.valueOf(this.lastUpdateTimestamp)});
        LiteLog.i("ts: %d", new Object[] {Long.valueOf(this.ts)});
        LiteLog.i("plugins: %d", new Object[] {Integer.valueOf(this.plugins.size())});
        if (!this.plugins.isEmpty()) {
            ArrayList<LiteStub> stubs = new ArrayList(this.plugins);
            Iterator var2 = stubs.iterator();

            while (var2.hasNext()) {
                LiteStub stub = (LiteStub) var2.next();
                LiteLog.i("==plugin id=%d, name=%s, ready=%b, last=%d", new Object[] {Integer.valueOf(stub.id),
                        stub.name, Boolean.valueOf(stub.ready), Long.valueOf(stub.lastLaunchTime)});
                if (stub.strategy != null) {
                    LiteLog.i("====strategy: %s", new Object[] {stub.strategy});
                }
            }
        }

        LiteLog.i("====configuration end=====", new Object[0]);
    }
}
