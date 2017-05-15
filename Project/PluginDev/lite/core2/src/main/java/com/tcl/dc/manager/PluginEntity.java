//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.tcl.dc.manager;

import com.tcl.dc.PluginStub;
import com.tcl.dc.PluginManager.PluginReadyCallback;
import com.tcl.dc.utils.Comparators;
import java.lang.ref.WeakReference;

public class PluginEntity extends PluginStub implements Comparable<PluginEntity>, Cloneable {
    public int state = 0;
    public long downloaded;
    public int priority = 50;
    public int retry = 1;
    public WeakReference<PluginReadyCallback> callback;

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
