
package com.larry.lite.db;

import com.larry.lite.LiteStub;
import com.larry.lite.utils.Comparators;
import com.larry.lite.LitePluginManager.PluginReadyCallback;

import java.lang.ref.WeakReference;

public class PluginEntity extends LiteStub implements Comparable<PluginEntity>, Cloneable {
    public int state = 0;
    public long downloaded;
    public int priority = 50;
    public int retry = 1;
    public WeakReference<PluginReadyCallback> callback;

    public PluginEntity() {}

    public PluginEntity(LiteStub stub) {
        this.id = stub.id;
        this.url = stub.url;
        this.path = stub.path;
        this.size = stub.size;
        this.md5 = stub.md5;
        this.lastLaunchTime = stub.lastLaunchTime;
        this.strategy = stub.strategy;
        this.priority = 50;
        this.ready = stub.ready;
    }

    public int compareTo(PluginEntity another) {
        return Comparators.compare(this.priority, (long) another.priority);
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException var2) {
            return null;
        }
    }
}
