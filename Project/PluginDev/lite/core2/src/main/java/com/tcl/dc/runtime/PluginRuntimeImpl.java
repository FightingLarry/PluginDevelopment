//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.tcl.dc.runtime;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;
import com.tcl.dc.PLog;
import com.tcl.dc.PluginConfiguration;
import com.tcl.dc.PluginContext;
import com.tcl.dc.PluginEngine;
import com.tcl.dc.PluginLifecycleCallback;
import com.tcl.dc.PluginManager;
import com.tcl.dc.PluginRuntime;
import com.tcl.dc.PluginStub;
import com.tcl.dc.TriggerEvent;
import com.tcl.dc.base.DCPlugin;
import com.tcl.dc.base.LaunchMode;
import com.tcl.dc.base.LaunchStrategy;
import com.tcl.dc.base.NetworkType;
import com.tcl.dc.internal.PluginClassLoader;
import com.tcl.dc.network.NetworkHelper;
import com.tcl.dc.network.NetworkSensor.NetworkStatus;
import com.tcl.dc.utils.CollectionUtils;
import com.tcl.dc.utils.Comparators;
import com.tcl.dc.utils.SparseArrays;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class PluginRuntimeImpl implements PluginRuntime {
    private final PluginContext mContext;
    private final PluginManager mManager;
    private Handler mIoHandler;
    private final PluginEngine mEngine;
    private SparseArray<PluginStub> mWaitingQueue;
    private PluginTask mRunningPlugin;
    private boolean mAllowSchedule = true;
    private PluginLifecycleCallback mCallback = new PluginLifecycleCallback() {
        public void onPluginCreate(PluginStub plugin) {
            PluginRuntimeImpl.this.mEngine.onPluginCreate(plugin);
        }

        public void onPluginStart(PluginStub plugin) {
            PluginRuntimeImpl.this.mEngine.onPluginStart(plugin);
        }

        public void onPluginEnd(PluginStub plugin, int err, Object extra) {
            PluginRuntimeImpl.this.mEngine.onPluginEnd(plugin, err, extra);
        }

        public void onPluginDestroy(PluginStub plugin) {
            PluginRuntimeImpl.this.mEngine.onPluginDestroy(plugin);
            System.gc();
            if (PluginRuntimeImpl.this.currentRef != null) {
                if (PluginRuntimeImpl.this.currentRef.isEnqueued()) {
                    PLog.i("Plugin %d release classloader", new Object[] {Integer.valueOf(plugin.id)});
                } else {
                    PLog.i("Plugin %d didn't release classloader", new Object[] {Integer.valueOf(plugin.id)});
                }

                try {
                    PluginRuntimeImpl.this.refQueue.remove(2000L);
                } catch (InterruptedException var3) {
                    PLog.printStackTrace(var3);
                }

                PluginRuntimeImpl.this.currentRef = null;
            }

        }
    };
    ReferenceQueue<ClassLoader> refQueue = new ReferenceQueue();
    PhantomReference<ClassLoader> currentRef = null;
    private static final int MSG_QUEUE = 1;
    private static final int MSG_SCHEDULE = 2;
    private static final int MSG_COMPLETE = 3;

    public PluginRuntimeImpl(PluginContext context, PluginManager manager, PluginEngine engine) {
        this.mContext = context;
        this.mManager = manager;
        this.mEngine = engine;
        if (context.getIoLooper() == null) {
            throw new IllegalStateException("no io looper for runtime");
        } else {
            this.mIoHandler = new PluginRuntimeImpl.RuntimeHandler(this, context.getIoLooper());
            this.mWaitingQueue = new SparseArray(10);
        }
    }

    private boolean match(TriggerEvent event, PluginStub stub, Object extra) {
        LaunchMode mode = stub.strategy.mode;
        int param = stub.strategy.modeExtra;
        if (event.equals(TriggerEvent.KeyEventImmediate)) {
            if (extra != null && extra instanceof Integer) {
                int id = ((Integer) extra).intValue();
                return id == -1 || stub.id == id;
            } else {
                return false;
            }
        } else if (mode.equals(LaunchMode.Periodicity)) {
            long last = stub.lastLaunchTime;
            long expired = last + (long) stub.strategy.modeExtra;
            return expired <= System.currentTimeMillis();
        } else {
            return event.equals(TriggerEvent.Periodicity)
                    ? false
                    : (event.equals(TriggerEvent.KeyEventStart)
                            ? param == 1
                            : (!event.equals(TriggerEvent.KeyEventUpgrade) ? param == 3 : param == 1 || param == 2));
        }
    }

    private boolean isCurrentIoThread() {
        return Thread.currentThread() == this.mIoHandler.getLooper().getThread();
    }

    void enqueue(List<PluginStub> executes) {
        if (!this.isCurrentIoThread()) {
            Message msg = this.mIoHandler.obtainMessage(1, executes);
            msg.sendToTarget();
        } else {
            boolean running = false;
            PluginTask task = this.mRunningPlugin;
            if (task != null) {
                executes.remove(task.getStub());
                running = true;
            }

            if (!CollectionUtils.isEmpty(executes)) {
                Iterator var4 = executes.iterator();

                while (var4.hasNext()) {
                    PluginStub ps = (PluginStub) var4.next();
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

    private PluginStub next(List<PluginStub> tasks) {
        if (CollectionUtils.isEmpty(tasks)) {
            return null;
        } else if (tasks.size() == 1) {
            return (PluginStub) tasks.get(0);
        } else {
            PluginStub task = (PluginStub) Collections.min(tasks, new Comparator<PluginStub>() {
                public int compare(PluginStub lhs, PluginStub rhs) {
                    return lhs.ready
                            ? -1
                            : (rhs.ready ? 1 : Comparators.compare(lhs.lastLaunchTime, rhs.lastLaunchTime));
                }
            });
            return task;
        }
    }

    private void schedule() {
        PLog.v("runtime schedule", new Object[0]);
        if (this.mRunningPlugin == null && this.mWaitingQueue.size() != 0) {
            NetworkStatus networkStatus = NetworkHelper.sharedHelper().getNetworkStatus();
            PluginStub plugin = null;
            List<PluginStub> plugins = SparseArrays.toList(this.mWaitingQueue);
            plugin = this.next(plugins);
            if (!accept(plugin.strategy, networkStatus)) {
                PLog.i("schedule cannot accept network %s, next", new Object[] {networkStatus});
                this.mWaitingQueue.remove(plugin.id);
                if (this.mWaitingQueue.size() == 0) {
                    PLog.w("waiting queue is empty", new Object[0]);
                    return;
                }
            }

            PluginTask task = new PluginTask(plugin);
            this.mRunningPlugin = task;
            this.mWaitingQueue.remove(plugin.id);
            RuntimeIo runtimeIo = new RuntimeIo(this.mContext, task, this, this.mManager);
            runtimeIo.setLifecycleCallback(this.mCallback);
            task.start(runtimeIo);
        }
    }

    void stopAll(int cause) {
        PLog.v("stopAll cause= %d", new Object[] {Integer.valueOf(cause)});
        this.mWaitingQueue.clear();
        if (this.mRunningPlugin != null) {
            this.mRunningPlugin.stop();
            this.mRunningPlugin = null;
        }

    }

    void onComplete(PluginStub stub, int state, int err) {
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

    static boolean accept(LaunchStrategy strategy, NetworkStatus network) {
        return strategy == null
                ? false
                : (network.equals(NetworkStatus.NetworkNotReachable)
                        ? strategy.networkLimit.equals(NetworkType.ALL)
                        : (network.equals(NetworkStatus.NetworkReachableViaWiFi)
                                ? strategy.networkLimit.compareTo(NetworkType.WWAN) >= 0
                                : (!network.equals(NetworkStatus.NetworkReachableViaWWAN)
                                        ? false
                                        : strategy.networkLimit.equals(NetworkType.WWAN)
                                                || strategy.networkLimit.equals(NetworkType.ALL))));
    }

    public void checkPluginsForLaunch(TriggerEvent event, Object extra) {
        PluginConfiguration configuration = this.mContext.getConfiguration();
        if (!CollectionUtils.isEmpty(configuration.getPlugins())) {
            ArrayList<PluginStub> plugins = new ArrayList(configuration.getPlugins());
            ArrayList<PluginStub> executes = new ArrayList(plugins.size());
            NetworkStatus network = NetworkHelper.sharedHelper().getNetworkStatus();
            Iterator var7 = plugins.iterator();

            while (var7.hasNext()) {
                PluginStub ps = (PluginStub) var7.next();
                if (this.match(event, ps, extra) && accept(ps.strategy, network)) {
                    executes.add(ps);
                }
            }

            if (!CollectionUtils.isEmpty(executes)) {
                this.enqueue(executes);
            } else {
                PLog.w("none any plugins should launched for event %s", new Object[] {event});
            }

        }
    }

    public ClassLoader createClassLoader(PluginStub plugin) throws Exception {
        PluginClassLoader classLoader = new PluginClassLoader(this.mContext, plugin, this.getClass().getClassLoader());
        PhantomReference<ClassLoader> ref = new PhantomReference(classLoader, this.refQueue);
        this.currentRef = ref;
        PLog.i("Plugin %d create classloader", new Object[] {Integer.valueOf(plugin.id)});
        return classLoader;
    }

    public DCPlugin loadPlugin(ClassLoader cl, PluginStub stub) throws Exception {
        if (cl instanceof PluginClassLoader) {
            PluginClassLoader pcl = (PluginClassLoader) cl;
            return pcl.loadPlugin();
        } else {
            throw new IllegalArgumentException("invalid param classloader");
        }
    }

    public void destroy() {
        this.stopAll(-5);
    }

    private static class RuntimeHandler extends Handler {
        WeakReference<PluginRuntimeImpl> mRef;

        public RuntimeHandler(PluginRuntimeImpl impl, Looper looper) {
            super(looper);
            this.mRef = new WeakReference(impl);
        }

        public void handleMessage(Message msg) {
            PluginRuntimeImpl impl = (PluginRuntimeImpl) this.mRef.get();
            if (impl != null) {
                switch (msg.what) {
                    case 1:
                        List<PluginStub> plugins = (List) msg.obj;
                        impl.enqueue(plugins);
                        break;
                    case 2:
                        this.removeMessages(2);
                        if (impl.mAllowSchedule) {
                            impl.schedule();
                        }
                        break;
                    case 3:
                        PluginStub stub = (PluginStub) msg.obj;
                        impl.onComplete(stub, msg.arg1, msg.arg2);
                }

            }
        }
    }
}
