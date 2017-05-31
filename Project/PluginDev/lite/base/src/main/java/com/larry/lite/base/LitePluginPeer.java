
package com.larry.lite.base;

import android.content.Context;

public final class LitePluginPeer {
    final Context mContext;
    final LiteStrategy mStrategy;
    final Object mStub;

    public LitePluginPeer(Context context, LiteStrategy strategy, Object stub) {
        this.mContext = context;
        this.mStrategy = strategy;
        this.mStub = stub;
    }

    public Context getContext() {
        return this.mContext;
    }

    public LiteStrategy getStrategy() {
        return this.mStrategy;
    }

    public Object getStub() {
        return this.mStub;
    }
}
