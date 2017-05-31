package com.larry.lite.host;


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

public class HostNetworkHelper {
    private static String TAG = "HostNetworkHelper";
    boolean mRegistered;
    HostNetworkHelper.NetworkStatus mStatus;
    HostNetworkHelper.NetworkBroadcastReceiver mReceiver;
    List<WeakReference<NetworkInductor>> mInductors;

    public static HostNetworkHelper sharedHelper() {
        return HostNetworkHelper.HelperHolder.helper;
    }

    private HostNetworkHelper() {
        this.mRegistered = false;
        this.mStatus = HostNetworkHelper.NetworkStatus.NetworkNotReachable;
        this.mReceiver = new HostNetworkHelper.NetworkBroadcastReceiver();
        this.mInductors = new LinkedList();
    }

    public void registerNetworkSensor(Context context) {
        if (!this.mRegistered) {
            this.mRegistered = true;
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService("connectivity");
            NetworkInfo info = manager.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                if (info.getType() == 0) {
                    this.mStatus = HostNetworkHelper.NetworkStatus.NetworkReachableViaWWAN;
                } else if (info.getType() == 1) {
                    this.mStatus = HostNetworkHelper.NetworkStatus.NetworkReachableViaWiFi;
                }
            } else {
                this.mStatus = HostNetworkHelper.NetworkStatus.NetworkNotReachable;
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

    public HostNetworkHelper.NetworkStatus getNetworkStatus() {
        return this.mStatus;
    }

    public boolean isWifiActive() {
        return this.mStatus.equals(HostNetworkHelper.NetworkStatus.NetworkReachableViaWiFi);
    }

    public boolean isNetworkAvailable() {
        return !this.mStatus.equals(HostNetworkHelper.NetworkStatus.NetworkNotReachable);
    }

    public void addNetworkInductor(HostNetworkHelper.NetworkInductor inductor) {
        List<WeakReference<HostNetworkHelper.NetworkInductor>> list = new ArrayList(this.mInductors);

        for (int i = 0; i < list.size(); ++i) {
            WeakReference<HostNetworkHelper.NetworkInductor> inductorRef = (WeakReference) list.get(i);
            HostNetworkHelper.NetworkInductor ind = (HostNetworkHelper.NetworkInductor) inductorRef.get();
            if (ind == inductor) {
                return;
            }

            if (ind == null) {
                this.mInductors.remove(inductorRef);
            }
        }

        this.mInductors.add(new WeakReference(inductor));
    }

    public void removeNetworkInductor(HostNetworkHelper.NetworkInductor inductor) {
        List<WeakReference<HostNetworkHelper.NetworkInductor>> list = new ArrayList(this.mInductors);

        for (int i = 0; i < list.size(); ++i) {
            WeakReference<HostNetworkHelper.NetworkInductor> inductorRef = (WeakReference) list.get(i);
            HostNetworkHelper.NetworkInductor ind = (HostNetworkHelper.NetworkInductor) inductorRef.get();
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
            List<WeakReference<HostNetworkHelper.NetworkInductor>> list = new ArrayList(this.mInductors);

            for (int i = 0; i < list.size(); ++i) {
                WeakReference<HostNetworkHelper.NetworkInductor> inductorRef = (WeakReference) list.get(i);
                HostNetworkHelper.NetworkInductor inductor = (HostNetworkHelper.NetworkInductor) inductorRef.get();
                if (inductor != null) {
                    inductor.onNetworkChanged(this.mStatus);
                } else {
                    this.mInductors.remove(inductorRef);
                }
            }

        }
    }

    private static class HelperHolder {
        private static final HostNetworkHelper helper = new HostNetworkHelper();

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
                    HostNetworkHelper.NetworkStatus ns = HostNetworkHelper.NetworkStatus.NetworkNotReachable;
                    if (info != null && info.isAvailable()) {
                        if (info.getType() == 0) {
                            ns = HostNetworkHelper.NetworkStatus.NetworkReachableViaWWAN;
                        } else if (info.getType() == 1) {
                            ns = HostNetworkHelper.NetworkStatus.NetworkReachableViaWiFi;
                        }
                    } else {
                        ns = HostNetworkHelper.NetworkStatus.NetworkNotReachable;
                    }

                    if (!HostNetworkHelper.this.mStatus.equals(ns)) {
                        HostNetworkHelper.this.mStatus = ns;
                        HostNetworkHelper.this.onNetworkChanged();
                    }
                }

            }
        }
    }

    public interface NetworkInductor {
        void onNetworkChanged(HostNetworkHelper.NetworkStatus var1);
    }

    public static enum NetworkStatus {
        NetworkNotReachable, NetworkReachableViaWWAN, NetworkReachableViaWiFi;

        private NetworkStatus() {}
    }
}
