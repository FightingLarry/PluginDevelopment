
package com.larry.lite;

import com.larry.lite.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PluginConfiguration {
    List<PluginStub> plugins = new ArrayList();
    private long ts;
    private long lastUpdateTimestamp;

    public PluginConfiguration() {}

    public long getLastUpdateTimestamp() {
        return this.lastUpdateTimestamp;
    }

    public long getTs() {
        return this.ts;
    }

    public List<PluginStub> getPlugins() {
        return this.plugins;
    }

    public void update(List<PluginStub> plugins, long t) {
        this.plugins.clear();
        if (!CollectionUtils.isEmpty(plugins)) {
            this.plugins.addAll(plugins);
        }

        this.lastUpdateTimestamp = System.currentTimeMillis();
        this.ts = t;
    }

    void syncTo(PluginManager manager) {
        List<PluginStub> updates = manager.syncWithConfiguration(this);
        if (!CollectionUtils.isEmpty(updates)) {
            this.plugins.clear();
            this.plugins.addAll(updates);
        }

    }

    void delete(PluginStub plugin) {
        this.plugins.remove(plugin);
    }

    void print() {
        PLog.i("====configuration start====", new Object[0]);
        PLog.i("lastUpdateTimestamp: %d", new Object[] {Long.valueOf(this.lastUpdateTimestamp)});
        PLog.i("ts: %d", new Object[] {Long.valueOf(this.ts)});
        PLog.i("plugins: %d", new Object[] {Integer.valueOf(this.plugins.size())});
        if (!this.plugins.isEmpty()) {
            ArrayList<PluginStub> stubs = new ArrayList(this.plugins);
            Iterator var2 = stubs.iterator();

            while (var2.hasNext()) {
                PluginStub stub = (PluginStub) var2.next();
                PLog.i("==plugin id=%d, name=%s, ready=%b, last=%d", new Object[] {Integer.valueOf(stub.id), stub.name,
                        Boolean.valueOf(stub.ready), Long.valueOf(stub.lastLaunchTime)});
                if (stub.strategy != null) {
                    PLog.i("====strategy: %s", new Object[] {stub.strategy});
                }
            }
        }

        PLog.i("====configuration end=====", new Object[0]);
    }
}
