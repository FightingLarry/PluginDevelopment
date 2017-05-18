
package com.larry.lite.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Build.VERSION;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.view.ViewConfiguration;

import com.larry.lite.PLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

public final class SystemUtils {
    private static final String BUILD_PROP_FILE = "/system/build.prop";
    private static final String PROP_NAME_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static String app_version_name;
    private static int app_version_code;

    public SystemUtils() {}

    public static boolean aboveApiLevel(int paramInt) {
        return getApiLevel() >= paramInt;
    }

    public static String[] getAbis() {
        if (VERSION.SDK_INT >= 21) {
            return Build.SUPPORTED_ABIS;
        } else {
            String[] arrayOfString = new String[] {Build.CPU_ABI, Build.CPU_ABI2};
            return arrayOfString;
        }
    }

    public static int getApiLevel() {
        return VERSION.SDK_INT;
    }

    public static Locale getLocale(Context paramContext) {
        try {
            Configuration localConfiguration = new Configuration();
            System.getConfiguration(paramContext.getContentResolver(), localConfiguration);
            Locale localLocale = localConfiguration.locale;
            if (localLocale == null) {
                localLocale = Locale.getDefault();
            }

            return localLocale;
        } catch (Exception var3) {
            PLog.printStackTrace(var3);
            return null;
        }
    }

    public static String getMacAddress(Context paramContext) {
        try {
            WifiManager wm = (WifiManager) paramContext.getSystemService("wifi");
            if (wm == null) {
                return null;
            }

            WifiInfo localWifiInfo = wm.getConnectionInfo();
            if (localWifiInfo != null) {
                return localWifiInfo.getMacAddress();
            }
        } catch (Throwable var3) {
            PLog.printStackTrace(var3);
        }

        return null;
    }

    public static String getSecureAndroidID(Context paramContext) {
        return Secure.getString(paramContext.getContentResolver(), "android_id");
    }

    public static int getVersionCode(Context paramContext) {
        if (app_version_code != 0) {
            return app_version_code;
        } else {
            try {
                app_version_code =
                        paramContext.getPackageManager().getPackageInfo(paramContext.getPackageName(), 0).versionCode;
                return app_version_code;
            } catch (NameNotFoundException var2) {
                PLog.printStackTrace(var2);
            } catch (RuntimeException var3) {
                PLog.printStackTrace(var3);
            }

            return 0;
        }
    }

    public static String getVersionName(Context paramContext) {
        if (app_version_name == null) {
            PackageInfo localPackageInfo = getPackageInfo(paramContext, paramContext.getPackageName(), 0);
            if (localPackageInfo == null) {
                return "";
            }

            app_version_name = localPackageInfo.versionName;
        }

        return app_version_name;
    }

    public static String getWifiIPAddress(Context paramContext) {
        try {
            WifiManager wm = (WifiManager) paramContext.getSystemService("wifi");
            if (wm == null) {
                return null;
            } else {
                WifiInfo localWifiInfo = wm.getConnectionInfo();
                if (localWifiInfo == null) {
                    return null;
                } else {
                    int i = localWifiInfo.getIpAddress();
                    if (i == 0) {
                        return null;
                    } else {
                        Locale localLocale = Locale.US;
                        Object[] arrayOfObject = new Object[] {Integer.valueOf(i & 255), Integer.valueOf(255 & i >> 8),
                                Integer.valueOf(255 & i >> 16), Integer.valueOf(255 & i >> 24)};
                        String str = String.format(localLocale, "%d.%d.%d.%d", arrayOfObject);
                        return str;
                    }
                }
            }
        } catch (Exception var7) {
            return null;
        }
    }

    public static boolean hasSoftKeys(Context paramContext) {
        return VERSION.SDK_INT >= 14 ? ViewConfiguration.get(paramContext).hasPermanentMenuKey() : false;
    }

    public static boolean isMIUI() {
        File localFile = new File("/system/build.prop");
        FileReader fr = null;
        BufferedReader localBufferedReader = null;

        try {
            fr = new FileReader(localFile);
            localBufferedReader = new BufferedReader(fr);
            boolean r = false;

            while (true) {
                String str = localBufferedReader.readLine();
                if (str != null) {
                    r = str.startsWith("ro.miui.ui.version.code");
                    if (!r) {
                        continue;
                    }
                }

                boolean var12 = r;
                return var12;
            }
        } catch (FileNotFoundException var9) {
            PLog.printStackTrace(var9);
        } catch (IOException var10) {
            PLog.printStackTrace(var10);
        } finally {
            Streams.safeClose(localBufferedReader);
            Streams.safeClose(fr);
        }

        return false;
    }

    public static boolean isPrimaryExternalStorageRemoveable() {
        return getApiLevel() >= 9 ? Environment.isExternalStorageRemovable() : true;
    }

    public static boolean isRooted() {
        return (new File("/system/bin/su")).exists()
                ? true
                : ((new File("/system/xbin/su")).exists() ? true : (new File("/data/bin/su")).exists());
    }

    public static PackageInfo getPackageInfo(Context paramContext, String paramString, int paramInt) {
        PackageManager pm = paramContext.getPackageManager();

        try {
            PackageInfo localPackageInfo = pm.getPackageInfo(paramString, paramInt);
            return localPackageInfo;
        } catch (NameNotFoundException var5) {
            PLog.printStackTrace(var5);
        } catch (RuntimeException var6) {
            PLog.printStackTrace(var6);
        }

        return null;
    }
}
