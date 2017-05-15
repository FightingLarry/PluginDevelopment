//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.tcl.dc.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.tcl.dc.PLog;
import java.util.UUID;

public class TelephonyManagerUtil {
    private static final String PREF_DEVICE_ID = "IMEI_CACHE";

    public TelephonyManagerUtil() {}

    public static String getDeviceId(Context context) {
        boolean needCached = false;
        String id = null;
        boolean ignore = false;
        String uuid;
        if (SystemUtils.aboveApiLevel(23)) {
            uuid = getPrefDeviceId(context);
            if (!TextUtils.isEmpty(uuid)) {
                return uuid;
            }

            int perms = ContextCompat.checkSelfPermission(context, "android.permission.READ_PHONE_STATE");
            if (perms == 0) {
                needCached = true;
            } else {
                ignore = true;
            }
        }

        if (!ignore) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
            id = tm.getDeviceId();
        }

        if (TextUtils.isEmpty(id)) {
            uuid = SystemUtils.getSecureAndroidID(context);
            if (!TextUtils.isEmpty(uuid)) {
                id = "AID_" + uuid;
            }
        }

        if (TextUtils.isEmpty(id)) {
            uuid = UUID.randomUUID().toString();

            try {
                byte[] byes = MD5Util.encode16(uuid, "utf-8");
                id = "UUID_" + MD5Util.toHexString(byes);
            } catch (Exception var6) {
                PLog.printStackTrace(var6);
            }
        }

        if (!TextUtils.isEmpty(id) && needCached) {
            savePrefDeviceId(context, id);
        }

        return id;
    }

    public static boolean isIMEI(String deviceId) {
        return TextUtils.isEmpty(deviceId) ? false : deviceId.startsWith("AID_") || deviceId.startsWith("UUID_");
    }

    public static String getSimCountryIso(Context context) {
        TelephonyManager telManager = (TelephonyManager) context.getSystemService("phone");
        return telManager.getSimCountryIso();
    }

    public static String getPrefDeviceId(Context context) {
        return PrefsUtils.loadPrefString(context, "IMEI_CACHE");
    }

    public static void savePrefDeviceId(Context context, String value) {
        PrefsUtils.savePrefString(context, "IMEI_CACHE", value);
    }

    public static String getSubscriberId(Context context) {
        String id = null;
        boolean ignore = false;
        if (SystemUtils.aboveApiLevel(23)) {
            int perms = ContextCompat.checkSelfPermission(context, "android.permission.READ_PHONE_STATE");
            if (perms != 0) {
                ignore = true;
            }
        }

        if (!ignore) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
            id = tm.getSubscriberId();
        }

        return id;
    }

    public static int getPhoneType(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
        int id = tm.getPhoneType();
        return id;
    }

    public static String getNetworkOperator(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
        return tm.getNetworkOperator();
    }
}
