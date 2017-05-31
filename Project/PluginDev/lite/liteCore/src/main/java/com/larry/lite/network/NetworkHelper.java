
package com.larry.lite.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.larry.lite.LiteLog;
import com.larry.lite.network.NetworkSensor.Callback;
import com.larry.lite.network.NetworkSensor.NetworkStatus;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class NetworkHelper implements Callback {
    List<WeakReference<NetworkHelper.NetworkInductor>> mInductors;
    NetworkSensor mSensor;

    public static NetworkHelper sharedHelper() {
        return NetworkHelper.HelperHolder.helper;
    }

    private NetworkHelper() {
        this.mInductors = new ArrayList();
    }

    public void registerNetworkSensor(Context context) {
        this.registerNetworkSensor(context, (NetworkSensor) null);
    }

    public synchronized void registerNetworkSensor(Context context, NetworkSensor ns) {
        LiteLog.i("registerNetworkSensor", new Object[0]);
        if (this.mSensor != ns) {
            if (this.mSensor != null) {
                this.unregisterNetworkSensor(context);
            }

            NetworkSensor sensor = ns == null ? new NetworkHelper.DefaultNetworkSenor() : ns;
            ((NetworkSensor) sensor).register(context, this);
            this.mSensor = (NetworkSensor) sensor;
        }
    }

    public synchronized void unregisterNetworkSensor(Context context) {
        NetworkSensor sensor = this.mSensor;
        if (sensor != null) {
            this.mSensor = null;
            sensor.unregister(context);
        }

    }

    public NetworkStatus getNetworkStatus() {
        if (this.mSensor == null) {
            throw new IllegalStateException("should register valid sensor first!");
        } else {
            return this.mSensor.getNetworkStatus();
        }
    }

    public boolean isWifiActive() {
        return NetworkStatus.NetworkReachableViaWiFi.equals(this.getNetworkStatus());
    }

    public boolean isMobileActive() {
        return NetworkStatus.NetworkReachableViaWWAN.equals(this.getNetworkStatus());
    }

    public boolean isBluetoothActive() {
        return NetworkStatus.NetworkReachableViaBlueTooth.equals(this.getNetworkStatus());
    }

    public boolean isNetworkAvailable() {
        return !NetworkStatus.NetworkNotReachable.equals(this.getNetworkStatus());
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

    public void onNetworkChanged(NetworkStatus status) {
        if (this.mInductors.size() != 0) {
            List<WeakReference<NetworkHelper.NetworkInductor>> list = new ArrayList(this.mInductors);

            for (int i = 0; i < list.size(); ++i) {
                WeakReference<NetworkHelper.NetworkInductor> inductorRef = (WeakReference) list.get(i);
                NetworkHelper.NetworkInductor inductor = (NetworkHelper.NetworkInductor) inductorRef.get();
                if (inductor != null) {
                    inductor.onNetworkChanged(status);
                } else {
                    this.mInductors.remove(inductorRef);
                }
            }

        }
    }

    private static class DefaultNetworkSenor implements NetworkSensor {
        final NetworkHelper.NetworkBroadcastReceiver mReceiver;
        Callback mCallback;
        NetworkStatus mStatus;

        DefaultNetworkSenor() {
            this.mStatus = NetworkStatus.NetworkNotReachable;
            this.mReceiver = new NetworkHelper.NetworkBroadcastReceiver(this);
        }

        public void register(Context context, Callback callback) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService("connectivity");
            NetworkInfo info = manager.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                if (info.getType() == 0) {
                    LiteLog.i("network reachable via wwan", new Object[0]);
                    this.mStatus = NetworkStatus.NetworkReachableViaWWAN;
                } else if (info.getType() == 1) {
                    LiteLog.i("network reachable via wifi", new Object[0]);
                    this.mStatus = NetworkStatus.NetworkReachableViaWiFi;
                } else if (info.getType() == 7) {
                    LiteLog.i("network reachable via bluetooth", new Object[0]);
                    this.mStatus = NetworkStatus.NetworkReachableViaBlueTooth;
                }
            } else {
                LiteLog.i("network not reachable", new Object[0]);
                this.mStatus = NetworkStatus.NetworkNotReachable;
            }

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            context.registerReceiver(this.mReceiver, intentFilter);
            this.mCallback = callback;
        }

        public void unregister(Context context) {
            this.mCallback = null;
            context.unregisterReceiver(this.mReceiver);
        }

        public NetworkStatus getNetworkStatus() {
            return this.mStatus;
        }

        void networkChanged(NetworkStatus status) {
            if (!status.equals(this.mStatus)) {
                this.mStatus = status;
                Callback callback = this.mCallback;
                if (callback != null) {
                    callback.onNetworkChanged(status);
                }

            }
        }
    }

    protected static class NetworkBroadcastReceiver extends BroadcastReceiver {
        WeakReference<NetworkHelper.DefaultNetworkSenor> ref;

        public NetworkBroadcastReceiver(NetworkHelper.DefaultNetworkSenor sensor) {
            this.ref = new WeakReference(sensor);
        }

        public void onReceive(Context context, Intent intent) {
            LiteLog.i("onReceive", new Object[0]);
            if (intent != null) {
                NetworkHelper.DefaultNetworkSenor sensor = (NetworkHelper.DefaultNetworkSenor) this.ref.get();
                if (sensor != null) {
                    ConnectivityManager manager = (ConnectivityManager) context.getSystemService("connectivity");
                    String action = intent.getAction();
                    if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                        NetworkInfo info = manager.getActiveNetworkInfo();
                        NetworkStatus ns = NetworkStatus.NetworkNotReachable;
                        if (info != null && info.isAvailable()) {
                            if (info.getType() == 0) {
                                LiteLog.i("network reachable via wwan", new Object[0]);
                                ns = NetworkStatus.NetworkReachableViaWWAN;
                            } else if (info.getType() == 1) {
                                LiteLog.i("network reachable via wifi", new Object[0]);
                                ns = NetworkStatus.NetworkReachableViaWiFi;
                            } else if (info.getType() == 7) {
                                LiteLog.i("network reachable via bluetooth", new Object[0]);
                                ns = NetworkStatus.NetworkReachableViaBlueTooth;
                            }
                        } else {
                            LiteLog.i("network not reachable", new Object[0]);
                            ns = NetworkStatus.NetworkNotReachable;
                        }

                        sensor.networkChanged(ns);
                    }

                }
            }
        }
    }

    private static class HelperHolder {
        private static final NetworkHelper helper = new NetworkHelper();

        private HelperHolder() {}
    }

    public interface NetworkInductor {
        void onNetworkChanged(NetworkStatus var1);
    }
}
