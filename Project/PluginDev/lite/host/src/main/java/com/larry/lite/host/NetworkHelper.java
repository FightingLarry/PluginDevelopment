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

public class NetworkHelper {
    private static String TAG = "NetworkHelper";
    boolean mRegistered;
    NetworkHelper.NetworkStatus mStatus;
    NetworkHelper.NetworkBroadcastReceiver mReceiver;
    List<WeakReference<NetworkInductor>> mInductors;

    public static NetworkHelper sharedHelper() {
        return NetworkHelper.HelperHolder.helper;
    }

    private NetworkHelper() {
        this.mRegistered = false;
        this.mStatus = NetworkHelper.NetworkStatus.NetworkNotReachable;
        this.mReceiver = new NetworkHelper.NetworkBroadcastReceiver();
        this.mInductors = new LinkedList();
    }

    public void registerNetworkSensor(Context context) {
        if (!this.mRegistered) {
            this.mRegistered = true;
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService("connectivity");
            NetworkInfo info = manager.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                if (info.getType() == 0) {
                    this.mStatus = NetworkHelper.NetworkStatus.NetworkReachableViaWWAN;
                } else if (info.getType() == 1) {
                    this.mStatus = NetworkHelper.NetworkStatus.NetworkReachableViaWiFi;
                }
            } else {
                this.mStatus = NetworkHelper.NetworkStatus.NetworkNotReachable;
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

    public NetworkHelper.NetworkStatus getNetworkStatus() {
        return this.mStatus;
    }

    public boolean isWifiActive() {
        return this.mStatus.equals(NetworkHelper.NetworkStatus.NetworkReachableViaWiFi);
    }

    public boolean isNetworkAvailable() {
        return !this.mStatus.equals(NetworkHelper.NetworkStatus.NetworkNotReachable);
    }

    public void addNetworkInductor(NetworkHelper.NetworkInductor inductor) {
        List<WeakReference<NetworkHelper.NetworkInductor>> list = new ArrayList(this.mInductors);

        for (int i = 0; i < list.size(); ++i) {
            WeakReference<NetworkHelper.NetworkInductor> inductorRef = (WeakReference) list.get(i);
            NetworkHelper.NetworkInductor ind = (NetworkHelper.NetworkInductor) inductorRef.get();
            if (ind == inductor) {
                return;
            }

            if (ind == null) {
                this.mInductors.remove(inductorRef);
            }
        }

        this.mInductors.add(new WeakReference(inductor));
    }

    public void removeNetworkInductor(NetworkHelper.NetworkInductor inductor) {
        List<WeakReference<NetworkHelper.NetworkInductor>> list = new ArrayList(this.mInductors);

        for (int i = 0; i < list.size(); ++i) {
            WeakReference<NetworkHelper.NetworkInductor> inductorRef = (WeakReference) list.get(i);
            NetworkHelper.NetworkInductor ind = (NetworkHelper.NetworkInductor) inductorRef.get();
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
            List<WeakReference<NetworkHelper.NetworkInductor>> list = new ArrayList(this.mInductors);

            for (int i = 0; i < list.size(); ++i) {
                WeakReference<NetworkHelper.NetworkInductor> inductorRef = (WeakReference) list.get(i);
                NetworkHelper.NetworkInductor inductor = (NetworkHelper.NetworkInductor) inductorRef.get();
                if (inductor != null) {
                    inductor.onNetworkChanged(this.mStatus);
                } else {
                    this.mInductors.remove(inductorRef);
                }
            }

        }
    }

    private static class HelperHolder {
        private static final NetworkHelper helper = new NetworkHelper();

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
                    NetworkHelper.NetworkStatus ns = NetworkHelper.NetworkStatus.NetworkNotReachable;
                    if (info != null && info.isAvailable()) {
                        if (info.getType() == 0) {
                            ns = NetworkHelper.NetworkStatus.NetworkReachableViaWWAN;
                        } else if (info.getType() == 1) {
                            ns = NetworkHelper.NetworkStatus.NetworkReachableViaWiFi;
                        }
                    } else {
                        ns = NetworkHelper.NetworkStatus.NetworkNotReachable;
                    }

                    if (!NetworkHelper.this.mStatus.equals(ns)) {
                        NetworkHelper.this.mStatus = ns;
                        NetworkHelper.this.onNetworkChanged();
                    }
                }

            }
        }
    }

    public interface NetworkInductor {
        void onNetworkChanged(NetworkHelper.NetworkStatus var1);
    }

    public static enum NetworkStatus {
        NetworkNotReachable, NetworkReachableViaWWAN, NetworkReachableViaWiFi;

        private NetworkStatus() {}
    }
}
