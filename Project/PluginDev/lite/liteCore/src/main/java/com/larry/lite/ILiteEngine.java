package com.larry.lite;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.larry.lite.network.NetworkHelper;
import com.larry.lite.network.NetworkLogger;
import com.larry.lite.utils.CollectionUtils;
import com.larry.lite.ILiteObtainPlugin.Callback;
import com.larry.lite.db.LitePluginManagerImpl;
import com.larry.lite.network.NetworkSensor.NetworkStatus;

import java.lang.ref.WeakReference;
import java.util.List;

public class ILiteEngine implements ILiteLifecycleCallback, Callback {
    private static final String TAG = "ILiteEngine";
    private static final int RETRY_UPDATE_CONFIG_TIME = 60000;
    private static final int PERIODICITY_UPDATE_CONFIG_ELAPSED = 3600000;
    private static final int RETRY_MAX_TIMES = 3;
    private final LiteContext mContext;
    private LitePluginManager mManager;
    private LiteRuntime mRuntime;
    private Handler mIoHandler;
    private boolean mInited;
    private LiteStats mStats;
    private boolean mContinueStating;
    private int mRetryTimesForFailed;
    private Runnable mPendingAction = null;
    private NetworkHelper.NetworkInductor mNetworkInductor;
    private ILiteObtainPlugin mCrawler;
    private List<ILiteObtainPlugin> mCrawlerList;
    private volatile boolean isUpdatingConfiguration = false;
    private static final int MSG_UPDATE_CONFIGURATION = 1;
    private static final int MSG_PERF_SNAPSHOT = 2;
    private static final int MSG_PREF_REPORT = 3;

    public ILiteEngine(LiteContext context) {
        this.mContext = context;
        this.mIoHandler = new ILiteEngine.EngineHandler();
        this.mStats = new LiteStats(context.getApplicationContext());
        this.mCrawlerList = context.getConfigurationCrawler();
    }

    public void setRunLoop(Looper looper) {
        this.mIoHandler = new ILiteEngine.EngineHandler(looper);
    }

    public void pumpEvent(LiteEvent event, Object extra) {
        LiteConfiguration configuration = this.mContext.getConfiguration();
        if (configuration == null) {
            LiteLog.w("configuration is empty, ignore the event %s!", new Object[] {event});
        } else {
            ILiteEngine.EventAction action = new ILiteEngine.EventAction(this, event);
            if (extra != null) {
                action.setExtra(extra);
            }

            if (!action.equals(this.mPendingAction)) {
                this.mIoHandler.post(action);
            } else {
                LiteLog.w("pumped event %s repeatedly", new Object[] {event});
            }

        }
    }

    void printConfiguration() {
        LiteConfiguration configuration = this.mContext.getConfiguration();
        if (configuration == null) {
            LiteLog.w("configuration is empty!", new Object[0]);
        } else {
            configuration.print();
        }
    }

    public void init(LiteEvent event) {
        if (this.mInited) {
            LiteLog.w("engine already inited...", new Object[0]);
        } else {
            this.mManager = this.createManager();
            if (!this.mManager.loadPlugins()) {
                LiteLog.e("Plugin Manager load plugins failed", new Object[0]);
            } else {
                this.mInited = true;
                Runnable newAction = new ILiteEngine.EventAction(this, event);
                Message msg = this.mIoHandler.obtainMessage(1, newAction);
                this.mIoHandler.sendMessage(msg);
                this.mNetworkInductor = new ILiteEngine.NetworkChangedDuctor(this);
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

    public void onPluginCreate(LiteStub plugin) {
        LiteLog.v("%s onPluginCreate", new Object[] {"ILiteEngine"});
    }

    public void onPluginStart(LiteStub plugin) {
        LiteLog.v("%s onPluginStart", new Object[] {"ILiteEngine"});
        Message msg = this.mIoHandler.obtainMessage(2, 1, 0, plugin);
        msg.sendToTarget();
    }

    public void onPluginEnd(LiteStub plugin, int err, Object extra) {
        LiteLog.v("%s onPluginEnd err:%d", new Object[] {"ILiteEngine", Integer.valueOf(err)});
        Message msg = this.mIoHandler.obtainMessage(2, 2, err, plugin);
        msg.sendToTarget();
    }

    public void onPluginDestroy(LiteStub plugin) {
        LiteLog.v("%s onPluginDestroy", new Object[] {"ILiteEngine"});
        Message msg = this.mIoHandler.obtainMessage(2, 3, 0, plugin);
        msg.sendToTarget();
    }

    private boolean isRuntimeError(int err) {
        return err >= 6 && err <= 13;
    }

    private void onPluginError(LiteStub plugin, int err) {
        if (this.isRuntimeError(err)) {
            LiteLog.w("plugin %d happened runtime error(%d)",
                    new Object[] {Integer.valueOf(plugin.id), Integer.valueOf(err)});
            LiteConfiguration configuration = this.mContext.getConfiguration();
            if (configuration != null) {
                configuration.delete(plugin);
            }
        }

    }

    public void onPluginComplete(LiteStub plugin, int state, int err) {
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
        LiteConfiguration pc = this.mContext.getConfiguration();
        return pc == null || pc.getLastUpdateTimestamp() + 3600000L <= System.currentTimeMillis();
    }

    private boolean isCurrentIoThread() {
        return Thread.currentThread() == this.mIoHandler.getLooper().getThread();
    }

    int updateConfiguration(boolean ignoreExpired) {
        if (this.isUpdatingConfiguration) {
            LiteLog.w("engine already updating configuration", new Object[0]);
            return 1;
        } else if (!this.isCurrentIoThread()) {
            Message msg = this.mIoHandler.obtainMessage(1, ignoreExpired ? 1 : 0, 0);
            msg.sendToTarget();
            return 3;
        } else if (!ignoreExpired && !this.configExpired()) {
            LiteLog.w("config not expired! last update time: %s", this.mContext.getConfiguration()
                    .getLastUpdateTimestamp());
            return 2;
        } else {

            if (CollectionUtils.isEmpty(mCrawlerList)) {
                return ILiteObtainPlugin.FAIL_NULL;
            }

            int err = ILiteObtainPlugin.SUCCESS;
            for (ILiteObtainPlugin ILiteObtainPlugin : mCrawlerList) {
                err = ILiteObtainPlugin.obtain(this);
                LiteLog.i("crawl configuration result: %d", new Object[] {Integer.valueOf(err)});
                if (err == ILiteObtainPlugin.SUCCESS) {
                    this.isUpdatingConfiguration = true;
                }
            }
            // int err = this.mCrawler.obtain(this);
            // LiteLog.i("crawl configuration result: %d", new Object[] {Integer.valueOf(err)});
            // if (err == 0) {
            // this.isUpdatingConfiguration = true;
            // }
            if (isUpdatingConfiguration) {
                return ILiteObtainPlugin.SUCCESS;
            }
            return err;

        }
    }

    public void onObtainResult(int err, LitePluginsConfigInfo litePluginsConfigInfo) {
        this.isUpdatingConfiguration = false;
        if (err != 0 && err != 4097) {
            if (!NetworkHelper.sharedHelper().isNetworkAvailable()) {
                LiteLog.w("network not available, won't retry update configuration", new Object[0]);
            } else {
                ++this.mRetryTimesForFailed;
                if (this.mRetryTimesForFailed >= 3) {
                    LiteLog.w("retry too many times: %d", new Object[] {Integer.valueOf(this.mRetryTimesForFailed)});
                } else {
                    Message msg = this.mIoHandler.obtainMessage(1, this.mPendingAction);
                    this.mIoHandler.sendMessageDelayed(msg, (long) ('\uea60' << this.mRetryTimesForFailed - 1));
                }
            }
        } else {
            if (err == 0) {
                LiteConfiguration old = this.mContext.getConfiguration();
                boolean changed;
                if (old == null) {
                    old = new LiteConfiguration();
                    this.mContext.setConfiguration(old);
                    changed = true;
                } else {
                    changed = old.getTs() == litePluginsConfigInfo.getTs();
                }

                if (changed) {
                    old.update(litePluginsConfigInfo.getPlugins(), litePluginsConfigInfo.getTs());
                    this.onConfigurationChanged(old);
                }
            }

            Runnable action = this.mPendingAction;
            this.mPendingAction = null;
            if (action != null) {
                this.mIoHandler.post(action);
            }

            this.mRetryTimesForFailed = 0;
            Runnable newAction = new ILiteEngine.EventAction(this, LiteEvent.Periodicity);
            Message msg = this.mIoHandler.obtainMessage(1, newAction);
            this.mIoHandler.sendMessageDelayed(msg, 3600000L);
        }
    }

    protected void onConfigurationChanged(LiteConfiguration newConfiguration) {
        newConfiguration.syncTo(this.mManager);
        if (!CollectionUtils.isEmpty(newConfiguration.getPlugins())) {
            this.createRuntime();
        }

    }

    protected LitePluginManager createManager() {
        LitePluginManager manager = new LitePluginManagerImpl(this.mContext);
        return manager;
    }

    protected LiteRuntime createRuntime() {
        if (this.mRuntime != null) {
            return this.mRuntime;
        } else {
            LiteRuntime runtime = new LiteRuntimeImpl(this.mContext, this.mManager, this);
            this.mRuntime = runtime;
            return runtime;
        }
    }

    private void reportStats(LiteStub plugin) {
        LiteStats stats = new LiteStats(this.mStats);
        int time = 0;
        boolean released = true;
        if (stats.releaseTime > 0L) {
            time = (int) (System.currentTimeMillis() - stats.releaseTime);
            released = time < 2000;
        }

        LiteLog.i("plugin %d stats: err=%d, state=%d, cpu=%d%%, mem=%dKB, duration=%dms, send: %dB, recv: %dB",
                new Object[] {Integer.valueOf(plugin.id), Integer.valueOf(stats.error), Integer.valueOf(stats.state),
                        Integer.valueOf(stats.maxCpuUsage), Long.valueOf(stats.maxMemUsed),
                        Long.valueOf(stats.duration), Long.valueOf(stats.sendBytes), Long.valueOf(stats.recvBytes)});
        LiteLog.i("plugin %d release: %b, clean time: %d",
                new Object[] {Integer.valueOf(plugin.id), Boolean.valueOf(released), Integer.valueOf(time)});
        NetworkLogger.uploadPluginMonitorLog(this.mContext, String.valueOf(plugin.id), plugin.md5, stats);
    }

    private static class NetworkChangedDuctor implements NetworkHelper.NetworkInductor {
        WeakReference<ILiteEngine> mEngine;

        public NetworkChangedDuctor(ILiteEngine engine) {
            this.mEngine = new WeakReference(engine);
        }

        public void onNetworkChanged(NetworkStatus networkStatus) {
            if (!networkStatus.equals(NetworkStatus.NetworkNotReachable)) {
                ILiteEngine engine = (ILiteEngine) this.mEngine.get();
                if (engine != null) {
                    engine.updateConfiguration(false);
                }

            }
        }
    }

    private static class EventAction implements Runnable {
        LiteEvent mEvent;
        Object mExtra;
        WeakReference<ILiteEngine> mEngine;

        EventAction(ILiteEngine engine, LiteEvent event) {
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
                ILiteEngine.EventAction that = (ILiteEngine.EventAction) o;
                return this.mEvent == that.mEvent;
            } else {
                return false;
            }
        }

        public int hashCode() {
            return this.mEvent != null ? this.mEvent.hashCode() : 0;
        }

        public void run() {
            ILiteEngine engine = (ILiteEngine) this.mEngine.get();
            if (engine != null) {
                LiteRuntime runtime = engine.mRuntime;
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
            if (msg.what == MSG_UPDATE_CONFIGURATION) {
                ILiteEngine.this.mPendingAction = (Runnable) msg.obj;
                boolean ignore = msg.arg1 > 0;
                int ret = ILiteEngine.this.updateConfiguration(ignore);
                if (ret <= -1 && ret > -10) {
                    ILiteEngine.this.mRetryTimesForFailed++;
                    if (ILiteEngine.this.mRetryTimesForFailed >= 3) {
                        LiteLog.w("retry too many times: %d",
                                new Object[] {Integer.valueOf(ILiteEngine.this.mRetryTimesForFailed)});
                        return;
                    }

                    Message newmsg = Message.obtain(msg);
                    this.sendMessageDelayed(newmsg, (long) ('\uea60' << ILiteEngine.this.mRetryTimesForFailed - 1));
                }
            } else if (msg.what == MSG_PERF_SNAPSHOT) {
                this.removeMessages(2);
                if (msg.arg1 == 1) {
                    ILiteEngine.this.mStats.start();
                    ILiteEngine.this.mContinueStating = true;
                } else if (msg.arg1 == 2) {
                    ILiteEngine.this.mContinueStating = false;
                    ILiteEngine.this.mStats.end(msg.arg2);
                } else if (msg.arg1 == 3) {
                    ILiteEngine.this.mContinueStating = false;
                    ILiteEngine.this.mStats.beginRelease();
                } else {
                    ILiteEngine.this.mStats.updatePerfs();
                }

                if (ILiteEngine.this.mContinueStating) {
                    this.sendEmptyMessageDelayed(2, 200L);
                }
            } else if (msg.what == MSG_PREF_REPORT) {
                LiteStub stub = (LiteStub) msg.obj;
                ILiteEngine.this.reportStats(stub);
            }

        }
    }
}
