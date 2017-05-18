
package com.larry.lite;

import android.app.ActivityManager;
import android.content.Context;
import android.net.TrafficStats;
import android.os.Process;
import android.os.SystemClock;
import android.os.Build.VERSION;
import android.text.TextUtils;

import com.larry.lite.utils.InvokeUtil;
import com.larry.lite.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

public class Perfs {
    public int cpuUsage;
    public long memUsed;
    public long rxBytes;
    public long txBytes;
    private static Perfs.ProcessSnapShot base = null;
    private static Perfs.ProcessSnapShot current = null;
    private static int SUPPORT_FILE_STAT_NETWORK = 0;

    public Perfs() {}

    public static Perfs.ProcessSnapShot snapshot(Context context) {
        Perfs.CpuJiffy cpuJiffy = null;
        Perfs.ProcessStats stats = null;

        try {
            cpuJiffy = getCpuJiffy();
        } catch (RuntimeException var5) {
            PLog.printStackTrace(var5);
            cpuJiffy = Perfs.CpuJiffy.EMPTY;
        }

        try {
            cpuJiffy = getCpuJiffy();
            stats = getProcessStats();
        } catch (RuntimeException var4) {
            PLog.printStackTrace(var4);
            stats = new Perfs.ProcessStats();
        }

        getProcessNetworkStats(stats);
        Perfs.MemoryInfo memoryInfo = getProcessMemoryInfo(context);
        return new Perfs.ProcessSnapShot(cpuJiffy, stats, memoryInfo);
    }

    public static void start(Context context) {
        base = snapshot(context);
        current = base;
    }

    public static Perfs dumpPerformance(Context context) {
        Perfs.ProcessSnapShot pss = snapshot(context);
        Perfs p = diff(pss, current);
        p.memUsed = pss.memoryInfo.totalMem - base.memoryInfo.totalMem;
        p.txBytes = pss.stats.txBytes - base.stats.txBytes;
        p.rxBytes = pss.stats.rxBytes - base.stats.rxBytes;
        current = pss;
        return p;
    }

    public static void end() {
        base = null;
        current = null;
    }

    private static Perfs diff(Perfs.ProcessSnapShot p1, Perfs.ProcessSnapShot p2) {
        Perfs pf = new Perfs();
        if (p1.currentTime < p2.currentTime) {
            Perfs.ProcessSnapShot old = p1;
            p1 = p2;
            p2 = old;
        }

        pf.cpuUsage = Perfs.CpuJiffy.getCpuPercent(p1.cpuJiffy, p2.cpuJiffy);
        pf.memUsed = p1.memoryInfo.totalMem;
        pf.txBytes = p1.stats.txBytes;
        pf.rxBytes = p1.stats.rxBytes;
        return pf;
    }

    private static Perfs.CpuJiffy getCpuJiffy() {
        long[] sysCpu = new long[7];
        int[] SYSTEM_CPU_FORMAT = getSystemCpuFormat();
        Class clz = Process.class;
        Object[] params = new Object[] {"/proc/stat", SYSTEM_CPU_FORMAT, null, sysCpu, null};
        Object ret = null;

        try {
            ret = InvokeUtil.invokeStaticMethod(clz, "readProcFile", params);
            if (ret != null && ((Boolean) ret).booleanValue()) {
                return Perfs.CpuJiffy.parse(sysCpu);
            }
        } catch (Exception var6) {
            PLog.printStackTrace(var6);
        }

        return Perfs.CpuJiffy.EMPTY;
    }

    private static Perfs.ProcessStats getProcessStats() {
        int pid = Process.myPid();
        long[] sysCpu = new long[4];
        int[] PROCESS_STATS_FORMAT = getProcessStatsFormat();
        Class clz = Process.class;
        String statFile = String.format(Locale.US, "/proc/%d/stat", new Object[] {Integer.valueOf(pid)});
        Object[] params = new Object[] {statFile, PROCESS_STATS_FORMAT, null, sysCpu, null};
        Object ret = null;

        try {
            ret = InvokeUtil.invokeStaticMethod(clz, "readProcFile", params);
            if (ret != null && ((Boolean) ret).booleanValue()) {
                Perfs.ProcessStats ps = new Perfs.ProcessStats();
                ps.utime = sysCpu[2];
                ps.stime = sysCpu[3];
                return ps;
            }
        } catch (Exception var8) {
            PLog.printStackTrace(var8);
        }

        return getProcessNetworkStats((Perfs.ProcessStats) null);
    }

    private static long getTxBytes(int uid) throws IOException {
        String tcpsnd = String.format(Locale.US, "/proc/uid_stat/%d/tcp_snd", new Object[] {Integer.valueOf(uid)});
        File file = new File(tcpsnd);
        if (!file.exists()) {
            throw new FileNotFoundException(tcpsnd + " not found");
        } else {
            String content = FileUtils.readString(file);
            if (TextUtils.isEmpty(content)) {
                throw new IOException("tcp_snd content empty");
            } else {
                content = content.trim();
                if (TextUtils.isEmpty(content)) {
                    throw new IOException("tcp_snd content empty");
                } else {
                    PLog.i("tcp_snd: %s", new Object[] {content});
                    return Long.parseLong(content);
                }
            }
        }
    }

    private static long getRxBytes(int uid) throws IOException {
        String tcprcv = String.format(Locale.US, "/proc/uid_stat/%d/tcp_rcv", new Object[] {Integer.valueOf(uid)});
        File file = new File(tcprcv);
        if (!file.exists()) {
            throw new FileNotFoundException(tcprcv + " not found");
        } else {
            String content = FileUtils.readString(file);
            if (TextUtils.isEmpty(content)) {
                throw new IOException("tcp_rcv content empty");
            } else {
                content = content.trim();
                if (TextUtils.isEmpty(content)) {
                    throw new IOException("tcp_rcv content empty");
                } else {
                    PLog.i("tcp_rcv: %s", new Object[] {content});
                    return Long.parseLong(content);
                }
            }
        }
    }

    private static Perfs.ProcessStats getProcessNetworkStats(Perfs.ProcessStats ps) {
        if (ps == null) {
            ps = new Perfs.ProcessStats();
        }

        int uid = Process.myUid();
        PLog.i("SUPPORT_FILE_STAT_NETWORK = %d", new Object[] {Integer.valueOf(SUPPORT_FILE_STAT_NETWORK)});
        if (SUPPORT_FILE_STAT_NETWORK == 0) {
            try {
                ps.txBytes = getTxBytes(uid);
                ps.rxBytes = getRxBytes(uid);
                SUPPORT_FILE_STAT_NETWORK = 1;
                PLog.i("uid =%d, getTxBytes =%d, getRxBytes =%d",
                        new Object[] {Integer.valueOf(uid), Long.valueOf(ps.txBytes), Long.valueOf(ps.rxBytes)});
            } catch (Exception var4) {
                PLog.printStackTrace(var4);
                SUPPORT_FILE_STAT_NETWORK = -1;
                ps.txBytes = TrafficStats.getUidTxBytes(uid);
                ps.rxBytes = TrafficStats.getUidRxBytes(uid);
                PLog.i("uid =%d, TrafficStats getUidTxBytes =%d, getUidRxBytes =%d",
                        new Object[] {Integer.valueOf(uid), Long.valueOf(ps.txBytes), Long.valueOf(ps.rxBytes)});
            }
        } else if (SUPPORT_FILE_STAT_NETWORK == 1) {
            try {
                ps.txBytes = getTxBytes(uid);
                ps.rxBytes = getRxBytes(uid);
                PLog.i("uid =%d, getTxBytes =%d, getRxBytes =%d",
                        new Object[] {Integer.valueOf(uid), Long.valueOf(ps.txBytes), Long.valueOf(ps.rxBytes)});
            } catch (Exception var3) {
                PLog.printStackTrace(var3);
                ps.txBytes = 0L;
                ps.rxBytes = 0L;
            }
        } else {
            ps.txBytes = TrafficStats.getUidTxBytes(uid);
            ps.rxBytes = TrafficStats.getUidRxBytes(uid);
            PLog.i("uid =%d, TrafficStats getUidTxBytes =%d, getUidRxBytes =%d",
                    new Object[] {Integer.valueOf(uid), Long.valueOf(ps.txBytes), Long.valueOf(ps.rxBytes)});
        }

        if (ps.txBytes == -1L) {
            ps.txBytes = 0L;
        }

        if (ps.rxBytes == -1L) {
            ps.rxBytes = 0L;
        }

        return ps;
    }

    private static int[] getProcessStatsFormat() {
        if (VERSION.SDK_INT < 11) {
            throw new RuntimeException("getProcessStatsFormat not support under 3.0");
        } else {
            Class clz;
            Object obj;
            if (VERSION.SDK_INT < 19) {
                try {
                    clz = Class.forName("com.android.internal.os.ProcessStats");
                    obj = InvokeUtil.valueOfStaticField(clz, "PROCESS_STATS_FORMAT");
                    return (int[]) ((int[]) obj);
                } catch (Exception var2) {
                    throw new RuntimeException("getProcessStatsFormat failed", var2);
                }
            } else {
                try {
                    clz = Class.forName("com.android.internal.os.ProcessCpuTracker");
                    obj = InvokeUtil.valueOfStaticField(clz, "PROCESS_STATS_FORMAT");
                    return (int[]) ((int[]) obj);
                } catch (Exception var3) {
                    throw new RuntimeException("getProcessStatsFormat failed", var3);
                }
            }
        }
    }

    private static int[] getSystemCpuFormat() {
        if (VERSION.SDK_INT < 11) {
            throw new RuntimeException("getSystemCpuFormat not support under 3.0");
        } else {
            Class clz;
            Object obj;
            if (VERSION.SDK_INT < 19) {
                try {
                    clz = Class.forName("com.android.internal.os.ProcessStats");
                    obj = InvokeUtil.valueOfStaticField(clz, "SYSTEM_CPU_FORMAT");
                    return (int[]) ((int[]) obj);
                } catch (Exception var2) {
                    throw new RuntimeException("getSystemCpuFormat failed", var2);
                }
            } else {
                try {
                    clz = Class.forName("com.android.internal.os.ProcessCpuTracker");
                    obj = InvokeUtil.valueOfStaticField(clz, "SYSTEM_CPU_FORMAT");
                    return (int[]) ((int[]) obj);
                } catch (Exception var3) {
                    throw new RuntimeException("getSystemCpuFormat failed", var3);
                }
            }
        }
    }

    public static Perfs.MemoryInfo getProcessMemoryInfo(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService("activity");
        int pid = Process.myPid();
        int[] pids = new int[] {pid};
        android.os.Debug.MemoryInfo[] memoryInfo = am.getProcessMemoryInfo(pids);
        Perfs.MemoryInfo mi = new Perfs.MemoryInfo();
        mi.USS = (long) memoryInfo[0].getTotalPrivateDirty();
        mi.PSS = (long) memoryInfo[0].getTotalPss();
        mi.RSS = (long) memoryInfo[0].getTotalSharedDirty();
        android.app.ActivityManager.MemoryInfo outInfo = new android.app.ActivityManager.MemoryInfo();
        am.getMemoryInfo(outInfo);
        mi.availMem = outInfo.availMem / 1024L;
        mi.totalMem = mi.PSS;
        PLog.i("mem: uss=%dKB, pss=%dKB, rss=%dKB, free=%dKB", new Object[] {Long.valueOf(mi.USS), Long.valueOf(mi.PSS),
                Long.valueOf(mi.RSS), Long.valueOf(mi.availMem)});
        return mi;
    }

    public static class MemoryInfo {
        public long USS;
        public long PSS;
        public long RSS;
        public long VSS;
        public long availMem;
        public long totalMem;

        public MemoryInfo() {}
    }

    public static class ProcessStats {
        public long utime;
        public long stime;
        public long txBytes;
        public long rxBytes;

        public ProcessStats() {}
    }

    public static class CpuJiffy {
        static final Perfs.CpuJiffy EMPTY = new Perfs.CpuJiffy();
        public long userTime;
        public long systemTime;
        public long idleTime;
        public long ioWaitTime;
        public long irqTime;
        public long softIrqTime;

        public CpuJiffy() {}

        static Perfs.CpuJiffy parse(long[] sysCpu) {
            if (sysCpu.length < 7) {
                throw new IllegalArgumentException("sysCpu length < 7");
            } else {
                Perfs.CpuJiffy ct = new Perfs.CpuJiffy();
                ct.userTime = sysCpu[0] + sysCpu[1];
                ct.systemTime = sysCpu[2];
                ct.idleTime = sysCpu[3];
                ct.ioWaitTime = sysCpu[4];
                ct.irqTime = sysCpu[5];
                ct.softIrqTime = sysCpu[6];
                return ct;
            }
        }

        long totalTime() {
            return this.userTime + this.systemTime + this.idleTime + this.ioWaitTime + this.irqTime + this.softIrqTime;
        }

        public static Perfs.CpuJiffy diff(Perfs.CpuJiffy ct1, Perfs.CpuJiffy ct2) {
            long ut = Math.abs(ct1.userTime - ct2.userTime);
            long st = Math.abs(ct1.systemTime - ct2.systemTime);
            long it = Math.abs(ct1.idleTime - ct2.idleTime);
            long iot = Math.abs(ct1.ioWaitTime - ct2.ioWaitTime);
            long irt = Math.abs(ct1.irqTime - ct2.irqTime);
            long sit = Math.abs(ct1.softIrqTime - ct2.softIrqTime);
            Perfs.CpuJiffy ct = new Perfs.CpuJiffy();
            ct.userTime = ut;
            ct.systemTime = st;
            ct.idleTime = it;
            ct.ioWaitTime = iot;
            ct.irqTime = irt;
            ct.softIrqTime = sit;
            return ct;
        }

        public static int getCpuPercent(Perfs.CpuJiffy ct1, Perfs.CpuJiffy ct2) {
            long ut = Math.abs(ct1.userTime - ct2.userTime);
            long st = Math.abs(ct1.systemTime - ct2.systemTime);
            long it = Math.abs(ct1.idleTime - ct2.idleTime);
            long irt = Math.abs(ct1.irqTime - ct2.irqTime);
            int denom = (int) (ut + st + it + irt);
            if (denom == 0) {
                return 0;
            } else {
                int all = (int) (ut + st + irt);
                return all * 100 / denom;
            }
        }
    }

    public static class ProcessSnapShot {
        public final Perfs.CpuJiffy cpuJiffy;
        public final Perfs.ProcessStats stats;
        public final Perfs.MemoryInfo memoryInfo;
        private long currentTime;

        private ProcessSnapShot(Perfs.CpuJiffy cj, Perfs.ProcessStats ps, Perfs.MemoryInfo mi) {
            this.cpuJiffy = cj;
            this.stats = ps;
            this.memoryInfo = mi;
            this.currentTime = SystemClock.elapsedRealtime();
        }
    }
}
