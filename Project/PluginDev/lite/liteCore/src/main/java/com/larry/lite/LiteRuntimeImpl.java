
package com.larry.lite;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;

import com.larry.lite.network.NetworkHelper;
import com.larry.lite.utils.CollectionUtils;
import com.larry.lite.utils.Comparators;
import com.larry.lite.base.LitePlugin;
import com.larry.lite.base.LiteLaunch;
import com.larry.lite.base.LiteStrategy;
import com.larry.lite.base.LiteNetworkType;
import com.larry.lite.obtain.LiteClassLoader;
import com.larry.lite.network.NetworkSensor.NetworkStatus;
import com.larry.lite.utils.SparseArrays;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class LiteRuntimeImpl implements LiteRuntime {
    private final LiteContext mContext;
    private final LitePluginManager mManager;
    private Handler mIoHandler;
    private final LiteManager mEngine;
    private SparseArray<LiteStub> mWaitingQueue;
    private LiteStubManager mRunningPlugin;
    private boolean mAllowSchedule = true;
    private ILiteLifecycleCallback mCallback = new ILiteLifecycleCallback() {
        public void onPluginCreate(LiteStub plugin) {
            LiteRuntimeImpl.this.mEngine.onPluginCreate(plugin);
        }

        public void onPluginStart(LiteStub plugin) {
            LiteRuntimeImpl.this.mEngine.onPluginStart(plugin);
        }

        public void onPluginEnd(LiteStub plugin, int err, Object extra) {
            LiteRuntimeImpl.this.mEngine.onPluginEnd(plugin, err, extra);
        }

        public void onPluginDestroy(LiteStub plugin) {
            LiteRuntimeImpl.this.mEngine.onPluginDestroy(plugin);
            System.gc();
            if (LiteRuntimeImpl.this.currentRef != null) {
                if (LiteRuntimeImpl.this.currentRef.isEnqueued()) {
                    LiteLog.i("Plugin %d release classloader", new Object[] {Integer.valueOf(plugin.id)});
                } else {
                    LiteLog.i("Plugin %d didn't release classloader", new Object[] {Integer.valueOf(plugin.id)});
                }

                try {
                    LiteRuntimeImpl.this.refQueue.remove(2000L);
                } catch (InterruptedException var3) {
                    LiteLog.printStackTrace(var3);
                }

                LiteRuntimeImpl.this.currentRef = null;
            }

        }
    };
    ReferenceQueue<ClassLoader> refQueue = new ReferenceQueue();
    PhantomReference<ClassLoader> currentRef = null;
    private static final int MSG_QUEUE = 1;
    private static final int MSG_SCHEDULE = 2;
    private static final int MSG_COMPLETE = 3;

    public LiteRuntimeImpl(LiteContext context, LitePluginManager manager, LiteManager engine) {
        this.mContext = context;
        this.mManager = manager;
        this.mEngine = engine;
        if (context.getIoLooper() == null) {
            throw new IllegalStateException("no io looper for runtime");
        } else {
            this.mIoHandler = new LiteRuntimeImpl.RuntimeHandler(this, context.getIoLooper());
            this.mWaitingQueue = new SparseArray(10);
        }
    }

    private boolean match(LiteEvent event, LiteStub stub, Object extra) {
        LiteLaunch mode = stub.strategy.getMode();
        int param = stub.strategy.getModeExtra();
        if (event.equals(LiteEvent.KeyEventImmediate)) {
            if (extra != null && extra instanceof Integer) {
                int id = ((Integer) extra).intValue();
                return id == -1 || stub.id == id;
            } else {
                return false;
            }
        } else if (mode.equals(LiteLaunch.Periodicity)) {
            long last = stub.lastLaunchTime;
            long expired = last + (long) stub.strategy.getModeExtra();
            return expired <= System.currentTimeMillis();
        } else {
            return event.equals(LiteEvent.Periodicity)
                    ? false
                    : (event.equals(LiteEvent.KeyEventStart)
                            ? param == 1
                            : (!event.equals(LiteEvent.KeyEventUpgrade) ? param == 3 : param == 1 || param == 2));
        }
    }

    private boolean isCurrentIoThread() {
        return Thread.currentThread() == this.mIoHandler.getLooper().getThread();
    }

    void enqueue(List<LiteStub> executes) {
        if (!this.isCurrentIoThread()) {
            Message msg = this.mIoHandler.obtainMessage(1, executes);
            msg.sendToTarget();
        } else {
            boolean running = false;
            LiteStubManager task = this.mRunningPlugin;
            if (task != null) {
                executes.remove(task.getStub());
                running = true;
            }

            if (!CollectionUtils.isEmpty(executes)) {
                Iterator var4 = executes.iterator();

                while (var4.hasNext()) {
                    LiteStub ps = (LiteStub) var4.next();
                    this.mWaitingQueue.put(ps.id, ps);
                }

                if (!running) {
                    this.start();
                }

            }
        }
    }

    void start() {
        this.mAllowSchedule = true;
        this.postSchedule();
    }

    private void postSchedule() {
        if (this.mAllowSchedule) {
            this.mIoHandler.removeMessages(2);
            this.mIoHandler.sendEmptyMessageDelayed(2, 100L);
        }
    }

    private LiteStub next(List<LiteStub> tasks) {
        if (CollectionUtils.isEmpty(tasks)) {
            return null;
        } else if (tasks.size() == 1) {
            return (LiteStub) tasks.get(0);
        } else {
            LiteStub task = (LiteStub) Collections.min(tasks, new Comparator<LiteStub>() {
                public int compare(LiteStub lhs, LiteStub rhs) {
                    return lhs.ready
                            ? -1
                            : (rhs.ready ? 1 : Comparators.compare(lhs.lastLaunchTime, rhs.lastLaunchTime));
                }
            });
            return task;
        }
    }

    private void schedule() {
        LiteLog.v("runtime schedule", new Object[0]);
        if (this.mRunningPlugin == null && this.mWaitingQueue.size() != 0) {
            NetworkStatus networkStatus = NetworkHelper.sharedHelper().getNetworkStatus();
            LiteStub plugin = null;
            List<LiteStub> plugins = SparseArrays.toList(this.mWaitingQueue);
            plugin = this.next(plugins);
            if (!accept(plugin.strategy, networkStatus)) {
                LiteLog.i("schedule cannot accept network %s, next", new Object[] {networkStatus});
                this.mWaitingQueue.remove(plugin.id);
                if (this.mWaitingQueue.size() == 0) {
                    LiteLog.w("waiting queue is empty", new Object[0]);
                    return;
                }
            }

            LiteStubManager task = new LiteStubManager(plugin);
            this.mRunningPlugin = task;
            this.mWaitingQueue.remove(plugin.id);
            LiteRunnable liteRunnable = new LiteRunnable(this.mContext, task, this, this.mManager);
            liteRunnable.setLifecycleCallback(this.mCallback);
            task.start(liteRunnable);
        }
    }

    void stopAll(int cause) {
        LiteLog.v("stopAll cause= %d", new Object[] {Integer.valueOf(cause)});
        this.mWaitingQueue.clear();
        if (this.mRunningPlugin != null) {
            this.mRunningPlugin.stop();
            this.mRunningPlugin = null;
        }

    }

    void onComplete(LiteStub stub, int state, int err) {
        if (this.isCurrentIoThread()) {
            if (this.mRunningPlugin != null) {
                this.mRunningPlugin.detach();
                this.mRunningPlugin = null;
            }

            this.mEngine.onPluginComplete(stub, state, err);
            this.postSchedule();
        } else {
            Message msg = this.mIoHandler.obtainMessage(3, state, err, stub);
            msg.sendToTarget();
        }

    }

    static boolean accept(LiteStrategy strategy, NetworkStatus network) {
        return strategy == null
                ? false
                : (network.equals(NetworkStatus.NetworkNotReachable)
                        ? strategy.getNetworkLimit().equals(LiteNetworkType.ALL)
                        : (network.equals(NetworkStatus.NetworkReachableViaWiFi)
                                ? strategy.getNetworkLimit().compareTo(LiteNetworkType.WWAN) >= 0
                                : (!network.equals(NetworkStatus.NetworkReachableViaWWAN)
                                        ? false
                                        : strategy.getNetworkLimit().equals(LiteNetworkType.WWAN)
                                                || strategy.getNetworkLimit().equals(LiteNetworkType.ALL))));
    }

    public void checkPluginsForLaunch(LiteEvent event, Object extra) {
        LiteConfiguration configuration = this.mContext.getConfiguration();
        if (!CollectionUtils.isEmpty(configuration.getPlugins())) {
            ArrayList<LiteStub> plugins = new ArrayList(configuration.getPlugins());
            ArrayList<LiteStub> executes = new ArrayList(plugins.size());
            NetworkStatus network = NetworkHelper.sharedHelper().getNetworkStatus();
            Iterator var7 = plugins.iterator();

            while (var7.hasNext()) {
                LiteStub ps = (LiteStub) var7.next();
                if (this.match(event, ps, extra) && accept(ps.strategy, network)) {
                    executes.add(ps);
                }
            }

            if (!CollectionUtils.isEmpty(executes)) {
                this.enqueue(executes);
            } else {
                LiteLog.w("none any plugins should launched for event %s", new Object[] {event});
            }

        }
    }

    public ClassLoader createClassLoader(LiteStub plugin) throws Exception {
        LiteClassLoader classLoader = new LiteClassLoader(this.mContext, plugin, this.getClass().getClassLoader());
        PhantomReference<ClassLoader> ref = new PhantomReference(classLoader, this.refQueue);
        this.currentRef = ref;
        LiteLog.i("Plugin %d create classloader", new Object[] {Integer.valueOf(plugin.id)});
        return classLoader;
    }

    public LitePlugin loadPlugin(ClassLoader cl, LiteStub stub) throws Exception {
        if (cl instanceof LiteClassLoader) {
            LiteClassLoader pcl = (LiteClassLoader) cl;
            return pcl.loadPlugin();
        } else {
            throw new IllegalArgumentException("invalid param classloader");
        }
    }

    public void destroy() {
        this.stopAll(-5);
    }

    private static class RuntimeHandler extends Handler {
        WeakReference<LiteRuntimeImpl> mRef;

        public RuntimeHandler(LiteRuntimeImpl impl, Looper looper) {
            super(looper);
            this.mRef = new WeakReference(impl);
        }

        public void handleMessage(Message msg) {
            LiteRuntimeImpl impl = (LiteRuntimeImpl) this.mRef.get();
            if (impl != null) {
                switch (msg.what) {
                    case 1:
                        List<LiteStub> plugins = (List) msg.obj;
                        impl.enqueue(plugins);
                        break;
                    case 2:
                        this.removeMessages(2);
                        if (impl.mAllowSchedule) {
                            impl.schedule();
                        }
                        break;
                    case 3:
                        LiteStub stub = (LiteStub) msg.obj;
                        impl.onComplete(stub, msg.arg1, msg.arg2);
                }

            }
        }
    }
}
