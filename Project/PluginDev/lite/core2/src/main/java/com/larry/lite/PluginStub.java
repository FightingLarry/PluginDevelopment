
package com.larry.lite;

import com.tcl.lite.base.LaunchStrategy;

public class PluginStub {
    public int id;
    public String name;
    public String url;
    public String path;
    public long size;
    public String md5;
    public String desc;
    public long lastLaunchTime;
    public boolean ready;
    public LaunchStrategy strategy;

    public PluginStub() {}

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && o instanceof PluginStub) {
            PluginStub that = (PluginStub) o;
            return this.id != that.id
                    ? false
                    : (this.strategy != null && that.strategy != null ? this.strategy.equals(that.strategy) : true);
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = this.id;
        result = 31 * result + (this.strategy != null ? this.strategy.hashCode() : 0);
        return result;
    }
}
