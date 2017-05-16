
package com.tcl.dc.network;

import android.content.Context;

public interface NetworkSensor {
    void register(Context var1, NetworkSensor.Callback var2);

    void unregister(Context var1);

    NetworkSensor.NetworkStatus getNetworkStatus();

    public interface Callback {
        void onNetworkChanged(NetworkSensor.NetworkStatus var1);
    }

    public static enum NetworkStatus {
        NetworkNotReachable, NetworkReachableViaWWAN, NetworkReachableViaWiFi, NetworkReachableViaBlueTooth;

        private NetworkStatus() {}

        public String getShortName() {
            String s = this.toString();
            if (this.equals(NetworkNotReachable)) {
                s = s.replace("Network", "");
            } else {
                s = s.replace("NetworkReachableVia", "");
            }

            return s;
        }
    }
}
