package com.larry.lite.host;

import android.content.Context;

import com.larry.lite.network.LiteNetworkHelper;
import com.larry.lite.network.NetworkSensor;

/**
 * Created by Larry on 2016/12/1.
 */

public class NetWorkComponent implements NetworkSensor, LiteNetworkHelper.NetworkInductor {
    Callback mCallback;
    final LiteNetworkHelper mHelper;

    public NetWorkComponent() {
        mHelper = LiteNetworkHelper.sharedHelper();
    }

    @Override
    public void register(Context context, Callback callback) {
        final LiteNetworkHelper helper = mHelper;
        helper.registerNetworkSensor(context);
        mCallback = callback;
        helper.addNetworkInductor(this);
    }

    @Override
    public void unregister(Context context) {
        mCallback = null;
        LiteNetworkHelper.sharedHelper().removeNetworkInductor(this);
    }

    @Override
    public NetworkStatus getNetworkStatus() {
        return toNetworkStatus(mHelper.getNetworkStatus());
    }

    private NetworkStatus toNetworkStatus(LiteNetworkHelper.NetworkStatus status) {
        int value = status.ordinal();
        NetworkStatus[] all = NetworkStatus.values();
        if (all.length <= value) throw new IllegalStateException("invalid network status");
        return all[value];
    }

    @Override
    public void onNetworkChanged(LiteNetworkHelper.NetworkStatus status) {
        final Callback callback = mCallback;
        if (callback != null) {
            callback.onNetworkChanged(toNetworkStatus(status));
        }
    }
}
