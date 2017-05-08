package com.larry.lite.manager;

import com.larry.lite.PluginManager;
import com.larry.lite.module.PluginStub;
import com.larry.lite.utils.Comparators;

import java.lang.ref.WeakReference;

/**
 * Created by Larry on 2017/5/8.
 */

public class PluginEntity extends PluginStub implements Comparable<PluginEntity>, Cloneable {
    public int state = 0;
    public long downloaded;
    public int priority = 50;
    public int retry = 1;
    public WeakReference<PluginManager.PluginReadyCallback> callback;

    public PluginEntity() {}

    public PluginEntity(PluginStub stub) {
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
