
package com.larry.lite.network;

import android.text.TextUtils;

import com.larry.lite.LiteLog;
import com.larry.lite.LiteContext;
import com.larry.lite.LiteStats;
import com.larry.lite.utils.JSONUtils;
import com.larry.lite.utils.SafeProguard;

public class NetworkLogger {
    public static final String TYPE_PLUGIN_MONI = "plugin_moni";
    public static final String TYPE_PLUGIN_DOWNLOAD = "plugin_down";
    public static final String TYPE_PLUGIN_DOWNLOAD_REQ = "req";
    public static final String TYPE_PLUGIN_DOWNLOAD_DOWN = "down";
    public static final String TYPE_PLUGIN_DOWNLOAD_INSTALL = "install";

    public NetworkLogger() {}

    private static StandardLog createMonitorLog(LiteContext context, LiteStats liteStats, String id, String md5,
            String type) {
        NetworkLogger.PluginBody pluginBody = new NetworkLogger.PluginBody();
        StandardLog<NetworkLogger.PluginBody> standardLog = new StandardLog();
        standardLog.type = type;
        standardLog.header = StandardLog.gatherHeader(context);
        standardLog.data = pluginBody;
        pluginBody.pkey = id;
        pluginBody.pver = md5;
        pluginBody.dur = liteStats.duration;
        pluginBody.maxcpu = liteStats.maxCpuUsage;
        pluginBody.maxram = liteStats.maxMemUsed;
        pluginBody.recv = liteStats.recvBytes;
        pluginBody.send = liteStats.sendBytes;
        pluginBody.avgcpu = liteStats.updateCount == 0 ? 0 : liteStats.totalCpuUsage / liteStats.updateCount;
        pluginBody.avgram = liteStats.updateCount == 0 ? 0L : liteStats.totalMenUsage / (long) liteStats.updateCount;
        pluginBody.sta = liteStats.state;
        pluginBody.err = liteStats.error;
        return standardLog;
    }

    public static void uploadPluginMonitorLog(LiteContext context, String id, String md5, LiteStats liteStats) {
        StandardLog standardLog = createMonitorLog(context, liteStats, id, md5, "plugin_moni");

        try {
            String json = JSONUtils.toJSONString(standardLog);
            if (TextUtils.isEmpty(json)) {
                return;
            }

            LiteLog.d(json, new Object[0]);
            updateLog(context, json, false);
        } catch (Exception var6) {
            LiteLog.printStackTrace(var6);
        }

    }

    public static void reportDownloaded(LiteContext context, int id, String md5, String type, int sta, String ms) {
        NetworkLogger.DownloadBody dnBody = new NetworkLogger.DownloadBody();
        StandardLog<NetworkLogger.DownloadBody> standardLog = new StandardLog();
        standardLog.type = "plugin_down";
        standardLog.header = StandardLog.gatherHeader(context);
        standardLog.data = dnBody;
        dnBody.pkey = id;
        dnBody.pver = md5;
        dnBody.type = type;
        dnBody.sta = sta;
        dnBody.ms = ms;

        try {
            String json = JSONUtils.toJSONString(standardLog);
            LiteLog.d(json, new Object[0]);
            updateLog(context, json, false);
        } catch (Exception var9) {
            LiteLog.printStackTrace(var9);
        }

    }

    private static void updateLog(LiteContext context, String log, boolean batch) {
        Object uploader = context.getComponent("uploader");
        if (uploader != null && uploader instanceof NetworkLogger.Uploader) {
            ((NetworkLogger.Uploader) uploader).sendLog(log, batch);
        }

    }

    public interface Uploader {
        void sendLog(String var1, boolean var2);
    }

    public static class DownloadBody implements SafeProguard {
        public int pkey;
        public String pver;
        public String type;
        public int sta;
        public String ms;

        public DownloadBody() {}
    }

    public static class PluginBody implements SafeProguard {
        public String pkey;
        public String pver;
        public int maxcpu;
        public int sta;
        public int err;
        public int avgcpu;
        public long dur;
        public long send;
        public long recv;
        public long maxram;
        public long avgram;

        public PluginBody() {}
    }
}
