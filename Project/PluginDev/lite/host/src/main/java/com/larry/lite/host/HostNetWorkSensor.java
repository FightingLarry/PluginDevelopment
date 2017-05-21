package com.larry.lite.host;

import android.content.Context;

import com.larry.lite.network.NetworkSensor;

/**
 * Created by yancai.liu on 2016/12/1.
 */

public class HostNetWorkSensor implements NetworkSensor, HostNetworkHelper.NetworkInductor {
    Callback mCallback;
    final HostNetworkHelper mHelper;

    public HostNetWorkSensor() {
        mHelper = HostNetworkHelper.sharedHelper();
    }

    @Override
    public void register(Context context, Callback callback) {
        final HostNetworkHelper helper = mHelper;
        helper.registerNetworkSensor(context);
        mCallback = callback;
        helper.addNetworkInductor(this);
    }

    @Override
    public void unregister(Context context) {
        mCallback = null;
        HostNetworkHelper.sharedHelper().removeNetworkInductor(this);
    }

    @Override
    public NetworkStatus getNetworkStatus() {
        return toNetworkStatus(mHelper.getNetworkStatus());
    }

    private NetworkStatus toNetworkStatus(HostNetworkHelper.NetworkStatus status) {
        int value = status.ordinal();
        NetworkStatus[] all = NetworkStatus.values();
        if (all.length <= value) throw new IllegalStateException("invalid network status");
        return all[value];
    }

    @Override
    public void onNetworkChanged(HostNetworkHelper.NetworkStatus status) {
        final Callback callback = mCallback;
        if (callback != null) {
            callback.onNetworkChanged(toNetworkStatus(status));
        }
    }
}
