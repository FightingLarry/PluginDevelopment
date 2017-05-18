
package com.larry.lite;

import android.content.Context;

public class PluginStats {
    public int maxCpuUsage;
    public long duration;
    public long startTime;
    public long endTime;
    public long maxMemUsed;
    public long sendBytes;
    public long recvBytes;
    public int error;
    public int state;
    public long releaseTime;
    public int updateCount;
    public int totalCpuUsage;
    public long totalMenUsage;
    private final Context mContext;
    private boolean hasStarted = false;

    public PluginStats(Context context) {
        this.mContext = context;
    }

    public PluginStats(PluginStats that) {
        this.mContext = that.mContext;
        this.maxCpuUsage = that.maxCpuUsage;
        this.duration = that.duration;
        this.startTime = that.startTime;
        this.maxMemUsed = that.maxMemUsed;
        this.sendBytes = that.sendBytes;
        this.recvBytes = that.recvBytes;
        this.endTime = that.endTime;
        this.error = that.error;
        this.state = that.state;
        this.releaseTime = that.releaseTime;
        this.updateCount = that.updateCount;
        this.totalCpuUsage = that.totalCpuUsage;
        this.totalMenUsage = that.totalMenUsage;
    }

    public void start() {
        this.hasStarted = true;
        this.maxCpuUsage = 0;
        this.duration = 0L;
        this.startTime = System.currentTimeMillis();
        this.endTime = 0L;
        this.releaseTime = 0L;
        this.maxMemUsed = 0L;
        this.sendBytes = 0L;
        this.recvBytes = 0L;
        this.error = 0;
        this.state = 0;
        this.updateCount = 0;
        this.totalCpuUsage = 0;
        this.totalMenUsage = 0L;
        Perfs.start(this.mContext);
    }

    public void end(int err) {
        Perfs.end();
        if (this.hasStarted) {
            this.endTime = System.currentTimeMillis();
            this.error = err;
            this.duration = this.endTime - this.startTime;
            this.hasStarted = false;
        } else {
            this.startTime = 0L;
        }

    }

    public void beginRelease() {
        this.releaseTime = System.currentTimeMillis();
    }

    public void updatePerfs() {
        Perfs p = Perfs.dumpPerformance(this.mContext);
        ++this.updateCount;
        this.totalCpuUsage += p.cpuUsage;
        this.totalMenUsage += p.memUsed;
        if (p.cpuUsage > this.maxCpuUsage) {
            this.maxCpuUsage = p.cpuUsage;
        }

        if (p.memUsed > this.maxMemUsed) {
            this.maxMemUsed = p.memUsed;
        }

        this.sendBytes = p.txBytes;
        this.recvBytes = p.rxBytes;
    }
}
