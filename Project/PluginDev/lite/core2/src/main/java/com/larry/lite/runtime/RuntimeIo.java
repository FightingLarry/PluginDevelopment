
package com.larry.lite.runtime;

import com.larry.lite.PluginContext;
import com.larry.lite.PLog;
import com.larry.lite.PluginException;
import com.larry.lite.PluginLifecycleCallback;
import com.larry.lite.PluginManager;
import com.larry.lite.PluginRuntime;
import com.larry.lite.PluginStub;
import com.larry.lite.PluginManager.PluginReadyCallback;
import com.tcl.lite.base.LitePlugin;
import com.tcl.lite.base.PluginPeer;

public class RuntimeIo implements Runnable {
    private final PluginContext mContext;
    private final PluginTask mTask;
    private final PluginRuntime mRuntime;
    private final PluginPeer mPeer;
    private final PluginManager mManager;
    private PluginLifecycleCallback mCallback;
    private Thread mThread;
    private volatile boolean isExecuting;
    private volatile boolean mStopped;
    private boolean mShouldWaiting;
    private PluginReadyCallback readyCallback = new PluginReadyCallback() {
        public void onReady(PluginStub stub) {
            synchronized (stub) {
                RuntimeIo.this.mShouldWaiting = false;
                stub.notify();
            }
        }

        public void onFail(PluginStub stub) {
            synchronized (stub) {
                RuntimeIo.this.mShouldWaiting = false;
                stub.notify();
            }
        }
    };

    RuntimeIo(PluginContext context, PluginTask task, PluginRuntime runtime, PluginManager manager) {
        this.mContext = context;
        this.mTask = task;
        this.mRuntime = runtime;
        PluginStub stub = this.mTask.getStub();
        this.mPeer = new PluginPeer(context.getApplicationContext(), stub.strategy, stub);
        this.mManager = manager;
    }

    public void setLifecycleCallback(PluginLifecycleCallback callback) {
        this.mCallback = callback;
    }

    public void cancel() {
        if (!this.mStopped) {
            this.mStopped = true;
            Thread thread = this.mThread;
            if (thread != null && thread.isAlive()) {
                thread.interrupt();

                try {
                    thread.join(20L);
                } catch (InterruptedException var3) {
                    ;
                }
            }

        }
    }

    private void waitReady(PluginStub stub) throws InterruptedException {
        if (!stub.ready) {
            if (this.mManager.requestReady(stub, this.readyCallback)) {
                synchronized (stub) {
                    this.mShouldWaiting = true;

                    while (this.mShouldWaiting) {
                        stub.wait(20L);
                    }

                }
            }
        }
    }

    public void run() {
        PluginStub stub = this.mTask.getStub();
        int state = this.mTask.getState();
        LitePlugin entity = null;
        ClassLoader cl = null;
        int err = -5;
        if (!this.mStopped) {
            label237: {
                try {
                    this.waitReady(stub);
                } catch (InterruptedException var19) {
                    PLog.printStackTrace(var19);
                    break label237;
                }

                if (!stub.ready) {
                    PLog.w("plugin not ready, won't execute!", new Object[0]);
                    err = 4;
                } else {
                    try {
                        state = this.mTask.setState(2);
                        cl = this.mRuntime.createClassLoader(stub);
                        if (!this.mStopped) {
                            entity = this.mRuntime.loadPlugin(cl, stub);
                            if (!this.mStopped) {
                                state = this.mTask.setState(3);
                                this.onPluginCreated(stub);
                                entity.onCreated(this.mPeer);
                                if (!this.mStopped) {
                                    this.onPluginStart(stub);
                                    state = this.mTask.setState(4);
                                    err = entity.execute(this.mPeer, this.mContext.getConnectionFactory());
                                    state = this.mTask.setState(5);
                                    if (this.mStopped) {
                                        err = -5;
                                    }
                                }
                            }
                        }
                    } catch (PluginException var14) {
                        PLog.printStackTrace(var14);
                        err = var14.errorCode();
                    } catch (SecurityException var15) {
                        PLog.printStackTrace(var15);
                        err = 1;
                    } catch (Exception var16) {
                        PLog.printStackTrace(var16);
                        err = 2;
                    } catch (OutOfMemoryError var17) {
                        PLog.printStackTrace(var17);
                        err = 3;
                    } finally {
                        if (state >= 3) {
                            if (state >= 2) {
                                this.onPluginEnd(stub, err, this.mPeer);
                            }

                            if (entity != null) {
                                entity.onDestroy(this.mPeer);
                            }

                            entity = null;
                            cl = null;
                            this.onPluginDestroy(stub);
                        }

                    }
                }
            }
        }

        this.isExecuting = false;
        this.mThread = null;
        this.mTask.detach();
        ((PluginRuntimeImpl) this.mRuntime).onComplete(stub, state, err);
    }

    public void execute() {
        if (this.isExecuting) {
            PLog.w("already executing...", new Object[0]);
        } else {
            this.isExecuting = true;
            this.mStopped = false;
            Thread thread = new Thread(this, "plugin-runtime-" + this.mTask.getStub().id);
            this.mThread = thread;
            thread.start();
        }
    }

    void onPluginCreated(PluginStub plugin) {
        PLog.v("RuntimeIo onPluginCreated", new Object[0]);
        PluginLifecycleCallback callback = this.mCallback;
        if (callback != null) {
            callback.onPluginCreate(plugin);
        }

    }

    void onPluginStart(PluginStub plugin) {
        PLog.v("RuntimeIo onPluginStart", new Object[0]);
        PluginLifecycleCallback callback = this.mCallback;
        if (callback != null) {
            callback.onPluginStart(plugin);
        }

    }

    void onPluginEnd(PluginStub plugin, int err, Object extra) {
        PLog.v("RuntimeIo onPluginEnd err: %d", new Object[] {Integer.valueOf(err)});
        PluginLifecycleCallback callback = this.mCallback;
        if (callback != null) {
            callback.onPluginEnd(plugin, err, extra);
        }

    }

    void onPluginDestroy(PluginStub plugin) {
        PLog.v("RuntimeIo onPluginDestroy", new Object[0]);
        PluginLifecycleCallback callback = this.mCallback;
        if (callback != null) {
            callback.onPluginDestroy(plugin);
        }

    }
}
