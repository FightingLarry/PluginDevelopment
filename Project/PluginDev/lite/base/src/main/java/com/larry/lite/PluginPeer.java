package com.larry.lite;

import android.content.Context;

/**
 * Created by Larry on 2017/5/8.
 */

public final class PluginPeer {

    final Context mContext;
    final LaunchStrategy mStrategy;
    final Object mStub;

    public PluginPeer(Context context, LaunchStrategy strategy, Object stub) {
        this.mContext = context;
        this.mStrategy = strategy;
        this.mStub = stub;
    }

    public Context getContext() {
        return this.mContext;
    }

    public LaunchStrategy getStrategy() {
        return this.mStrategy;
    }

    public Object getStub() {
        return this.mStub;
    }
}
