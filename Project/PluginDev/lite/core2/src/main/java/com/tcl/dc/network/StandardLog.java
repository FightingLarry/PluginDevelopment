//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.tcl.dc.network;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import com.tcl.dc.PLog;
import com.tcl.dc.PluginContext;
import com.tcl.dc.utils.AndroidUtil;
import com.tcl.dc.utils.TelephonyManagerUtil;

public class StandardLog<T> {
    public String type;
    public StandardLog.Header header;
    public T data;

    public StandardLog() {}

    public StandardLog(T t) {
        this.data = t;
    }

    public static StandardLog.Header gatherHeader(PluginContext context) {
        Context applicationContext = context.getApplicationContext();
        StandardLog.Header header = new StandardLog.Header();
        header.mod = Build.MODEL;
        header.ime = TelephonyManagerUtil.getDeviceId(applicationContext);
        header.ims = TelephonyManagerUtil.getSubscriberId(applicationContext);
        header.net = AndroidUtil.getNetworkInfoName(applicationContext);
        header.mmc = TelephonyManagerUtil.getNetworkOperator(applicationContext);
        header.dty = TelephonyManagerUtil.getPhoneType(applicationContext);
        header.chn = context.getChannel();
        header.t = System.currentTimeMillis();

        try {
            PackageManager pm = applicationContext.getPackageManager();
            header.from = applicationContext.getPackageName();
            header.ver = pm.getPackageInfo(header.from, 0).versionName;
        } catch (Exception var4) {
            PLog.printStackTrace(var4);
        }

        return header;
    }

    public static class Header {
        public String mod;
        public String net;
        public String ime;
        public String ims;
        public String mmc;
        public String ver;
        public int dty;
        public String chn;
        public long t;
        public String from;

        public Header() {}
    }
}
