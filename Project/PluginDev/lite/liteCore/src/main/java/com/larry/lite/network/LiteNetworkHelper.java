package com.larry.lite.network;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LiteNetworkHelper {
    private static String TAG = "LiteNetworkHelper";
    boolean mRegistered;
    LiteNetworkHelper.NetworkStatus mStatus;
    LiteNetworkHelper.NetworkBroadcastReceiver mReceiver;
    List<WeakReference<NetworkInductor>> mInductors;

    public static LiteNetworkHelper sharedHelper() {
        return LiteNetworkHelper.HelperHolder.helper;
    }

    private LiteNetworkHelper() {
        this.mRegistered = false;
        this.mStatus = LiteNetworkHelper.NetworkStatus.NetworkNotReachable;
        this.mReceiver = new LiteNetworkHelper.NetworkBroadcastReceiver();
        this.mInductors = new LinkedList();
    }

    public void registerNetworkSensor(Context context) {
        if (!this.mRegistered) {
            this.mRegistered = true;
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService("connectivity");
            NetworkInfo info = manager.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                if (info.getType() == 0) {
                    this.mStatus = LiteNetworkHelper.NetworkStatus.NetworkReachableViaWWAN;
                } else if (info.getType() == 1) {
                    this.mStatus = LiteNetworkHelper.NetworkStatus.NetworkReachableViaWiFi;
                }
            } else {
                this.mStatus = LiteNetworkHelper.NetworkStatus.NetworkNotReachable;
            }

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            context.registerReceiver(this.mReceiver, intentFilter);
        }
    }

    public void unregisterNetworkSensor(Context context) {
        if (this.mRegistered) {
            this.mRegistered = false;
            context.unregisterReceiver(this.mReceiver);
        }
    }

    public LiteNetworkHelper.NetworkStatus getNetworkStatus() {
        return this.mStatus;
    }

    public boolean isWifiActive() {
        return this.mStatus.equals(LiteNetworkHelper.NetworkStatus.NetworkReachableViaWiFi);
    }

    public boolean isNetworkAvailable() {
        return !this.mStatus.equals(LiteNetworkHelper.NetworkStatus.NetworkNotReachable);
    }

    public void addNetworkInductor(LiteNetworkHelper.NetworkInductor inductor) {
        List<WeakReference<NetworkInductor>> list = new ArrayList(this.mInductors);

        for (int i = 0; i < list.size(); ++i) {
            WeakReference<NetworkInductor> inductorRef = (WeakReference) list.get(i);
            LiteNetworkHelper.NetworkInductor ind = (LiteNetworkHelper.NetworkInductor) inductorRef.get();
            if (ind == inductor) {
                return;
            }

            if (ind == null) {
                this.mInductors.remove(inductorRef);
            }
        }

        this.mInductors.add(new WeakReference(inductor));
    }

    public void removeNetworkInductor(LiteNetworkHelper.NetworkInductor inductor) {
        List<WeakReference<NetworkInductor>> list = new ArrayList(this.mInductors);

        for (int i = 0; i < list.size(); ++i) {
            WeakReference<NetworkInductor> inductorRef = (WeakReference) list.get(i);
            LiteNetworkHelper.NetworkInductor ind = (LiteNetworkHelper.NetworkInductor) inductorRef.get();
            if (ind == inductor) {
                this.mInductors.remove(inductorRef);
                return;
            }

            if (ind == null) {
                this.mInductors.remove(inductorRef);
            }
        }

    }

    protected void onNetworkChanged() {
        if (this.mInductors.size() != 0) {
            List<WeakReference<NetworkInductor>> list = new ArrayList(this.mInductors);

            for (int i = 0; i < list.size(); ++i) {
                WeakReference<NetworkInductor> inductorRef = (WeakReference) list.get(i);
                LiteNetworkHelper.NetworkInductor inductor = (LiteNetworkHelper.NetworkInductor) inductorRef.get();
                if (inductor != null) {
                    inductor.onNetworkChanged(this.mStatus);
                } else {
                    this.mInductors.remove(inductorRef);
                }
            }

        }
    }

    private static class HelperHolder {
        private static final LiteNetworkHelper helper = new LiteNetworkHelper();

        private HelperHolder() {}
    }

    protected class NetworkBroadcastReceiver extends BroadcastReceiver {
        protected NetworkBroadcastReceiver() {}

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                    ConnectivityManager manager = (ConnectivityManager) context.getSystemService("connectivity");
                    NetworkInfo info = manager.getActiveNetworkInfo();
                    LiteNetworkHelper.NetworkStatus ns = LiteNetworkHelper.NetworkStatus.NetworkNotReachable;
                    if (info != null && info.isAvailable()) {
                        if (info.getType() == 0) {
                            ns = LiteNetworkHelper.NetworkStatus.NetworkReachableViaWWAN;
                        } else if (info.getType() == 1) {
                            ns = LiteNetworkHelper.NetworkStatus.NetworkReachableViaWiFi;
                        }
                    } else {
                        ns = LiteNetworkHelper.NetworkStatus.NetworkNotReachable;
                    }

                    if (!LiteNetworkHelper.this.mStatus.equals(ns)) {
                        LiteNetworkHelper.this.mStatus = ns;
                        LiteNetworkHelper.this.onNetworkChanged();
                    }
                }

            }
        }
    }

    public interface NetworkInductor {
        void onNetworkChanged(LiteNetworkHelper.NetworkStatus var1);
    }

    public static enum NetworkStatus {
        NetworkNotReachable, NetworkReachableViaWWAN, NetworkReachableViaWiFi;

        private NetworkStatus() {}
    }
}
