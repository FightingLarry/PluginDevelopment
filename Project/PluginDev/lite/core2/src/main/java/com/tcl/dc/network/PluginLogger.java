
package com.tcl.dc.network;

import android.text.TextUtils;
import com.tcl.dc.PLog;
import com.tcl.dc.PluginContext;
import com.tcl.dc.PluginStats;
import com.tcl.dc.utils.JSONUtils;
import com.tcl.dc.utils.SafeProguard;

public class PluginLogger {
    public static final String TYPE_PLUGIN_MONI = "plugin_moni";
    public static final String TYPE_PLUGIN_DOWNLOAD = "plugin_down";
    public static final String TYPE_PLUGIN_DOWNLOAD_REQ = "req";
    public static final String TYPE_PLUGIN_DOWNLOAD_DOWN = "down";
    public static final String TYPE_PLUGIN_DOWNLOAD_INSTALL = "install";

    public PluginLogger() {}

    private static StandardLog createMonitorLog(PluginContext context, PluginStats pluginStats, String id, String md5,
            String type) {
        PluginLogger.PluginBody pluginBody = new PluginLogger.PluginBody();
        StandardLog<PluginLogger.PluginBody> standardLog = new StandardLog();
        standardLog.type = type;
        standardLog.header = StandardLog.gatherHeader(context);
        standardLog.data = pluginBody;
        pluginBody.pkey = id;
        pluginBody.pver = md5;
        pluginBody.dur = pluginStats.duration;
        pluginBody.maxcpu = pluginStats.maxCpuUsage;
        pluginBody.maxram = pluginStats.maxMemUsed;
        pluginBody.recv = pluginStats.recvBytes;
        pluginBody.send = pluginStats.sendBytes;
        pluginBody.avgcpu = pluginStats.updateCount == 0 ? 0 : pluginStats.totalCpuUsage / pluginStats.updateCount;
        pluginBody.avgram =
                pluginStats.updateCount == 0 ? 0L : pluginStats.totalMenUsage / (long) pluginStats.updateCount;
        pluginBody.sta = pluginStats.state;
        pluginBody.err = pluginStats.error;
        return standardLog;
    }

    public static void uploadPluginMonitorLog(PluginContext context, String id, String md5, PluginStats pluginStats) {
        StandardLog standardLog = createMonitorLog(context, pluginStats, id, md5, "plugin_moni");

        try {
            String json = JSONUtils.toJSONString(standardLog);
            if (TextUtils.isEmpty(json)) {
                return;
            }

            PLog.d(json, new Object[0]);
            updateLog(context, json, false);
        } catch (Exception var6) {
            PLog.printStackTrace(var6);
        }

    }

    public static void reportDownloaded(PluginContext context, int id, String md5, String type, int sta, String ms) {
        PluginLogger.DownloadBody dnBody = new PluginLogger.DownloadBody();
        StandardLog<PluginLogger.DownloadBody> standardLog = new StandardLog();
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
            PLog.d(json, new Object[0]);
            updateLog(context, json, false);
        } catch (Exception var9) {
            PLog.printStackTrace(var9);
        }

    }

    private static void updateLog(PluginContext context, String log, boolean batch) {
        Object uploader = context.getComponent("uploader");
        if (uploader != null && uploader instanceof PluginLogger.Uploader) {
            ((PluginLogger.Uploader) uploader).sendLog(log, batch);
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
