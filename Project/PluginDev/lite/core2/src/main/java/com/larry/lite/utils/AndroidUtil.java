
package com.larry.lite.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.larry.lite.PLog;
import java.util.Arrays;

@SuppressLint({"NewApi"})
public class AndroidUtil {
    private static final String UNKNWON = "unkwon";
    private static final String NOT_AVAILABLE = "not_avaible";
    private static final String WIFI = "wifi";
    private static final String BLUETOOTH = "bluetooth";
    private static final String G3NET = "3gnet";
    private static final String G3WAP = "3gwap";
    private static final String UNINET = "uninet";
    private static final String UNIWAP = "uniwap";
    private static final String CMNET = "cmnet";
    private static final String CMWAP = "cmwap";
    private static final String CTNET = "ctnet";
    private static final String CTWAP = "ctwap";
    private static final String MOBILE = "mobile";

    public AndroidUtil() {}

    public static String getVersionName(Context context) {
        PackageManager manager = context.getPackageManager();

        try {
            PackageInfo pi = manager.getPackageInfo(context.getPackageName(), 0);
            return pi.versionName;
        } catch (NameNotFoundException var3) {
            PLog.printStackTrace(var3);
            return "";
        }
    }

    public static int getVersionCode(Context context) {
        PackageManager manager = context.getPackageManager();

        try {
            PackageInfo pi = manager.getPackageInfo(context.getPackageName(), 0);
            return pi.versionCode;
        } catch (NameNotFoundException var3) {
            PLog.printStackTrace(var3);
            return -1;
        }
    }

    public static String getNetworkInfoName(Context context) {
        ConnectivityManager connectionManager = (ConnectivityManager) context.getSystemService("connectivity");
        NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
        return networkInfo == null ? "" : networkInfo.getTypeName();
    }

    public static String getNetworkTypeName(Context context) {
        int perms = context.checkCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE");
        if (perms == -1) {
            return "unkwon";
        } else {
            ConnectivityManager connectivitymanager = (ConnectivityManager) context.getSystemService("connectivity");
            NetworkInfo networkinfo = connectivitymanager.getActiveNetworkInfo();
            if (networkinfo != null && networkinfo.isAvailable()) {
                int type = networkinfo.getType();
                if (type == 1) {
                    return "wifi";
                } else if (type == 7) {
                    return "bluetooth";
                } else if (type == 0) {
                    String netInfo = networkinfo.getExtraInfo();
                    if (netInfo != null) {
                        netInfo = netInfo.toLowerCase();
                        String[] infos =
                                new String[] {"3gnet", "3gwap", "cmnet", "cmwap", "ctnet", "ctwap", "uninet", "uniwap"};
                        int index = Arrays.binarySearch(infos, netInfo);
                        if (index >= 0) {
                            return infos[index];
                        }
                    }

                    return "mobile";
                } else {
                    return "unkwon";
                }
            } else {
                return "not_avaible";
            }
        }
    }
}
