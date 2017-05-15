//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.tcl.dc;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.tcl.dc.ConfigurationCrawler.Callback;
import com.tcl.dc.manager.PluginManagerImpl;
import com.tcl.dc.network.NetworkHelper;
import com.tcl.dc.network.PluginLogger;
import com.tcl.dc.network.NetworkHelper.NetworkInductor;
import com.tcl.dc.network.NetworkSensor.NetworkStatus;
import com.tcl.dc.runtime.PluginRuntimeImpl;
import com.tcl.dc.utils.CollectionUtils;
import java.lang.ref.WeakReference;
import java.util.List;

public class PluginEngine implements PluginLifecycleCallback, Callback {
    private static final String TAG = "PluginEngine";
    private static final int RETRY_UPDATE_CONFIG_TIME = 60000;
    private static final int PERIODICITY_UPDATE_CONFIG_ELAPSED = 3600000;
    private static final int RETRY_MAX_TIMES = 3;
    private final PluginContext mContext;
    private PluginManager mManager;
    private PluginRuntime mRuntime;
    private Handler mIoHandler;
    private boolean mInited;
    private PluginStats mStats;
    private boolean mContinueStating;
    private int mRetryTimesForFailed;
    private Runnable mPendingAction = null;
    private NetworkInductor mNetworkInductor;
    private ConfigurationCrawler mCrawler;
    private volatile boolean isUpdatingConfiguration = false;
    private static final int MSG_UPDATE_CONFIGURATION = 1;
    private static final int MSG_PERF_SNAPSHOT = 2;
    private static final int MSG_PREF_REPORT = 3;

    public PluginEngine(PluginContext context) {
        this.mContext = context;
        this.mIoHandler = new PluginEngine.EngineHandler();
        this.mStats = new PluginStats(context.getApplicationContext());
        this.mCrawler = context.getConfigurationCrawler();
    }

    public void setRunLoop(Looper looper) {
        this.mIoHandler = new PluginEngine.EngineHandler(looper);
    }

    public void pumpEvent(TriggerEvent event, Object extra) {
        PluginConfiguration configuration = this.mContext.getConfiguration();
        if (configuration == null) {
            PLog.w("configuration is empty, ignore the event %s!", new Object[] {event});
        } else {
            PluginEngine.EventAction action = new PluginEngine.EventAction(this, event);
            if (extra != null) {
                action.setExtra(extra);
            }

            if (!action.equals(this.mPendingAction)) {
                this.mIoHandler.post(action);
            } else {
                PLog.w("pumped event %s repeatedly", new Object[] {event});
            }

        }
    }

    void printConfiguration() {
        PluginConfiguration configuration = this.mContext.getConfiguration();
        if (configuration == null) {
            PLog.w("configuration is empty!", new Object[0]);
        } else {
            configuration.print();
        }
    }

    public void init(TriggerEvent event) {
        if (this.mInited) {
            PLog.w("engine already inited...", new Object[0]);
        } else {
            this.mManager = this.createManager();
            if (!this.mManager.loadPlugins()) {
                PLog.e("Plugin Manager load plugins failed", new Object[0]);
            } else {
                this.mInited = true;
                Runnable newAction = new PluginEngine.EventAction(this, event);
                Message msg = this.mIoHandler.obtainMessage(1, newAction);
                this.mIoHandler.sendMessage(msg);
                this.mNetworkInductor = new PluginEngine.NetworkChangedDuctor(this);
                NetworkHelper.sharedHelper().addNetworkInductor(this.mNetworkInductor);
            }
        }
    }

    public void destroy() {
        if (this.mInited) {
            NetworkHelper.sharedHelper().removeNetworkInductor(this.mNetworkInductor);
            this.mIoHandler.removeMessages(1);
            this.mIoHandler.removeMessages(2);
            this.mCrawler.cancel();
            this.mRuntime.destroy();
            this.mManager.destroy();
        }
    }

    public void onPluginCreate(PluginStub plugin) {
        PLog.v("%s onPluginCreate", new Object[] {"PluginEngine"});
    }

    public void onPluginStart(PluginStub plugin) {
        PLog.v("%s onPluginStart", new Object[] {"PluginEngine"});
        Message msg = this.mIoHandler.obtainMessage(2, 1, 0, plugin);
        msg.sendToTarget();
    }

    public void onPluginEnd(PluginStub plugin, int err, Object extra) {
        PLog.v("%s onPluginEnd err:%d", new Object[] {"PluginEngine", Integer.valueOf(err)});
        Message msg = this.mIoHandler.obtainMessage(2, 2, err, plugin);
        msg.sendToTarget();
    }

    public void onPluginDestroy(PluginStub plugin) {
        PLog.v("%s onPluginDestroy", new Object[] {"PluginEngine"});
        Message msg = this.mIoHandler.obtainMessage(2, 3, 0, plugin);
        msg.sendToTarget();
    }

    private boolean isRuntimeError(int err) {
        return err >= 6 && err <= 13;
    }

    private void onPluginError(PluginStub plugin, int err) {
        if (this.isRuntimeError(err)) {
            PLog.w("plugin %d happened runtime error(%d)",
                    new Object[] {Integer.valueOf(plugin.id), Integer.valueOf(err)});
            PluginConfiguration configuration = this.mContext.getConfiguration();
            if (configuration != null) {
                configuration.delete(plugin);
            }
        }

    }

    public void onPluginComplete(PluginStub plugin, int state, int err) {
        if (err != -5 && err != 0) {
            this.onPluginError(plugin, err);
        }

        if (this.mStats.error != err) {
            this.mStats.error = err;
        }

        this.mStats.state = state;
        plugin.lastLaunchTime = System.currentTimeMillis();
        this.mManager.savePlugin(plugin);
        Message msg = this.mIoHandler.obtainMessage(3, plugin);
        msg.sendToTarget();
    }

    private boolean configExpired() {
        PluginConfiguration pc = this.mContext.getConfiguration();
        return pc == null || pc.getLastUpdateTimestamp() + 3600000L <= System.currentTimeMillis();
    }

    private boolean isCurrentIoThread() {
        return Thread.currentThread() == this.mIoHandler.getLooper().getThread();
    }

    int updateConfiguration(boolean ignoreExpired) {
        if (this.isUpdatingConfiguration) {
            PLog.w("engine already updating configuration", new Object[0]);
            return 1;
        } else if (!this.isCurrentIoThread()) {
            Message msg = this.mIoHandler.obtainMessage(1, ignoreExpired ? 1 : 0, 0);
            msg.sendToTarget();
            return 3;
        } else if (!ignoreExpired && !this.configExpired()) {
            PLog.w("config not expired! last update time: %s",
                    new Object[] {Long.valueOf(this.mContext.getConfiguration().getLastUpdateTimestamp())});
            return 2;
        } else {
            int err = this.mCrawler.crawlConfiguration(this);
            PLog.i("crawl configuration result: %d", new Object[] {Integer.valueOf(err)});
            if (err == 0) {
                this.isUpdatingConfiguration = true;
            }

            return err;
        }
    }

    public void onConfigurationResult(int err, List<PluginStub> plugins, long timestamp) {
        this.isUpdatingConfiguration = false;
        if (err != 0 && err != 4097) {
            if (!NetworkHelper.sharedHelper().isNetworkAvailable()) {
                PLog.w("network not available, won't retry update configuration", new Object[0]);
            } else {
                ++this.mRetryTimesForFailed;
                if (this.mRetryTimesForFailed >= 3) {
                    PLog.w("retry too many times: %d", new Object[] {Integer.valueOf(this.mRetryTimesForFailed)});
                } else {
                    Message msg = this.mIoHandler.obtainMessage(1, this.mPendingAction);
                    this.mIoHandler.sendMessageDelayed(msg, (long) ('\uea60' << this.mRetryTimesForFailed - 1));
                }
            }
        } else {
            if (err == 0) {
                PluginConfiguration old = this.mContext.getConfiguration();
                boolean changed;
                if (old == null) {
                    old = new PluginConfiguration();
                    this.mContext.setConfiguration(old);
                    changed = true;
                } else {
                    changed = old.getTs() == timestamp;
                }

                if (changed) {
                    old.update(plugins, timestamp);
                    this.onConfigurationChanged(old);
                }
            }

            Runnable action = this.mPendingAction;
            this.mPendingAction = null;
            if (action != null) {
                this.mIoHandler.post(action);
            }

            this.mRetryTimesForFailed = 0;
            Runnable newAction = new PluginEngine.EventAction(this, TriggerEvent.Periodicity);
            Message msg = this.mIoHandler.obtainMessage(1, newAction);
            this.mIoHandler.sendMessageDelayed(msg, 3600000L);
        }
    }

    protected void onConfigurationChanged(PluginConfiguration newConfiguration) {
        newConfiguration.syncTo(this.mManager);
        if (!CollectionUtils.isEmpty(newConfiguration.getPlugins())) {
            this.createRuntime();
        }

    }

    protected PluginManager createManager() {
        PluginManager manager = new PluginManagerImpl(this.mContext);
        return manager;
    }

    protected PluginRuntime createRuntime() {
        if (this.mRuntime != null) {
            return this.mRuntime;
        } else {
            PluginRuntime runtime = new PluginRuntimeImpl(this.mContext, this.mManager, this);
            this.mRuntime = runtime;
            return runtime;
        }
    }

    private void reportStats(PluginStub plugin) {
        PluginStats stats = new PluginStats(this.mStats);
        int time = 0;
        boolean released = true;
        if (stats.releaseTime > 0L) {
            time = (int) (System.currentTimeMillis() - stats.releaseTime);
            released = time < 2000;
        }

        PLog.i("plugin %d stats: err=%d, state=%d, cpu=%d%%, mem=%dKB, duration=%dms, send: %dB, recv: %dB",
                new Object[] {Integer.valueOf(plugin.id), Integer.valueOf(stats.error), Integer.valueOf(stats.state),
                        Integer.valueOf(stats.maxCpuUsage), Long.valueOf(stats.maxMemUsed),
                        Long.valueOf(stats.duration), Long.valueOf(stats.sendBytes), Long.valueOf(stats.recvBytes)});
        PLog.i("plugin %d release: %b, clean time: %d",
                new Object[] {Integer.valueOf(plugin.id), Boolean.valueOf(released), Integer.valueOf(time)});
        PluginLogger.uploadPluginMonitorLog(this.mContext, String.valueOf(plugin.id), plugin.md5, stats);
    }

    private static class NetworkChangedDuctor implements NetworkInductor {
        WeakReference<PluginEngine> mEngine;

        public NetworkChangedDuctor(PluginEngine engine) {
            this.mEngine = new WeakReference(engine);
        }

        public void onNetworkChanged(NetworkStatus networkStatus) {
            if (!networkStatus.equals(NetworkStatus.NetworkNotReachable)) {
                PluginEngine engine = (PluginEngine) this.mEngine.get();
                if (engine != null) {
                    engine.updateConfiguration(false);
                }

            }
        }
    }

    private static class EventAction implements Runnable {
        TriggerEvent mEvent;
        Object mExtra;
        WeakReference<PluginEngine> mEngine;

        EventAction(PluginEngine engine, TriggerEvent event) {
            this.mEngine = new WeakReference(engine);
            this.mEvent = event;
        }

        public void setExtra(Object o) {
            this.mExtra = o;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o != null && this.getClass() == o.getClass()) {
                PluginEngine.EventAction that = (PluginEngine.EventAction) o;
                return this.mEvent == that.mEvent;
            } else {
                return false;
            }
        }

        public int hashCode() {
            return this.mEvent != null ? this.mEvent.hashCode() : 0;
        }

        public void run() {
            PluginEngine engine = (PluginEngine) this.mEngine.get();
            if (engine != null) {
                PluginRuntime runtime = engine.mRuntime;
                if (runtime != null) {
                    runtime.checkPluginsForLaunch(this.mEvent, this.mExtra);
                }

            }
        }
    }

    private class EngineHandler extends Handler {
        EngineHandler() {}

        EngineHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                PluginEngine.this.mPendingAction = (Runnable) msg.obj;
                boolean ignore = msg.arg1 > 0;
                int ret = PluginEngine.this.updateConfiguration(ignore);
                if (ret <= -1 && ret > -10) {
                    PluginEngine.this.mRetryTimesForFailed++;
                    if (PluginEngine.this.mRetryTimesForFailed >= 3) {
                        PLog.w("retry too many times: %d",
                                new Object[] {Integer.valueOf(PluginEngine.this.mRetryTimesForFailed)});
                        return;
                    }

                    Message newmsg = Message.obtain(msg);
                    this.sendMessageDelayed(newmsg, (long) ('\uea60' << PluginEngine.this.mRetryTimesForFailed - 1));
                }
            } else if (msg.what == 2) {
                this.removeMessages(2);
                if (msg.arg1 == 1) {
                    PluginEngine.this.mStats.start();
                    PluginEngine.this.mContinueStating = true;
                } else if (msg.arg1 == 2) {
                    PluginEngine.this.mContinueStating = false;
                    PluginEngine.this.mStats.end(msg.arg2);
                } else if (msg.arg1 == 3) {
                    PluginEngine.this.mContinueStating = false;
                    PluginEngine.this.mStats.beginRelease();
                } else {
                    PluginEngine.this.mStats.updatePerfs();
                }

                if (PluginEngine.this.mContinueStating) {
                    this.sendEmptyMessageDelayed(2, 200L);
                }
            } else if (msg.what == 3) {
                PluginStub stub = (PluginStub) msg.obj;
                PluginEngine.this.reportStats(stub);
            }

        }
    }
}
