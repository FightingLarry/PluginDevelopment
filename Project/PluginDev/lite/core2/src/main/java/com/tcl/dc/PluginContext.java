//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.tcl.dc;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import com.tcl.dc.base.ConnectionFactory;
import java.util.HashMap;
import java.util.Map;

public final class PluginContext {
    private final Context mContext;
    private PluginConfiguration mConfiguration;
    private ConnectionFactory mConnectionFactory;
    private Looper mIoLooper;
    private ConfigurationCrawler mCrawler;
    private String mUserAgent;
    private String mChannel;
    private String mPluginConfigUrl;
    private boolean mLocalDebug;
    private Map<String, String> mNetworkCommonParams;
    private final Map<String, Object> mComponents;

    public PluginContext(Context context, Map<String, Object> comps) {
        this.mContext = context.getApplicationContext();
        if (comps != null && !comps.isEmpty()) {
            this.mComponents = new HashMap(comps);
        } else {
            this.mComponents = new HashMap();
        }

    }

    public Context getApplicationContext() {
        return this.mContext;
    }

    public PluginConfiguration getConfiguration() {
        return this.mConfiguration;
    }

    void registerComponent(String name, Object obj) {
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("name should valid for registerComponent!");
        } else if (obj == null) {
            this.mComponents.remove(name);
        } else {
            this.mComponents.put(name, obj);
        }
    }

    public Object getComponent(String name) {
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("name should valid for getComponent!");
        } else {
            return this.mComponents.get(name);
        }
    }

    void setConfiguration(PluginConfiguration mConfiguration) {
        this.mConfiguration = mConfiguration;
    }

    public ConnectionFactory getConnectionFactory() {
        return this.mConnectionFactory;
    }

    void setConnectionFactory(ConnectionFactory mConnectionFactory) {
        this.mConnectionFactory = mConnectionFactory;
    }

    public Looper getIoLooper() {
        return this.mIoLooper;
    }

    void setIoLooper(Looper mIoLooper) {
        this.mIoLooper = mIoLooper;
    }

    public String getUserAgent() {
        return this.mUserAgent;
    }

    public void setUserAgent(String userAgent) {
        this.mUserAgent = userAgent;
    }

    public String getChannel() {
        return this.mChannel;
    }

    public void setChannel(String channel) {
        this.mChannel = channel;
    }

    public String getPluginConfigUrl() {
        return this.mPluginConfigUrl;
    }

    public void setPluginConfigUrl(String pluginConfigUrl) {
        this.mPluginConfigUrl = pluginConfigUrl;
    }

    public boolean isLocalDebug() {
        return this.mLocalDebug;
    }

    public void setLocalDebug(boolean debug) {
        this.mLocalDebug = debug;
    }

    public void setNetworkCommonParams(Map<String, String> params) {
        if (this.mNetworkCommonParams == null) {
            this.mNetworkCommonParams = new HashMap();
        }

        this.mNetworkCommonParams.clear();
        this.mNetworkCommonParams.putAll(params);
    }

    public Map<String, String> getNetworkCommonParams() {
        return this.mNetworkCommonParams;
    }

    public ConfigurationCrawler getConfigurationCrawler() {
        return this.mCrawler;
    }

    void setConfigurationCrawler(ConfigurationCrawler crawler) {
        this.mCrawler = crawler;
    }
}
