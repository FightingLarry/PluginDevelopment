package com.larry.lite.module;

import com.larry.lite.LaunchStrategy;

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

    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (!(o instanceof PluginStub))) return false;

        PluginStub that = (PluginStub) o;
        if (this.id != that.id) return false;
        if ((this.strategy == null) || (that.strategy == null)) {
            return true;
        }
        return this.strategy.equals(that.strategy);
    }

    public int hashCode() {
        int result = this.id;
        result = 31 * result + (this.strategy != null ? this.strategy.hashCode() : 0);
        return result;
    }
}
