
package com.larry.lite;

import com.larry.lite.LitePluginManager.PluginReadyCallback;
import com.larry.lite.base.LitePlugin;
import com.larry.lite.base.LitePluginPeer;

public class LiteRunnable implements Runnable {
    private final LiteContext mContext;
    private final LiteManager mTask;
    private final LiteRuntime mRuntime;
    private final LitePluginPeer mPeer;
    private final LitePluginManager mManager;
    private ILiteLifecycleCallback mCallback;
    private Thread mThread;
    private volatile boolean isExecuting;
    private volatile boolean mStopped;
    private boolean mShouldWaiting;
    private PluginReadyCallback readyCallback = new PluginReadyCallback() {
        public void onReady(LiteStub stub) {
            synchronized (stub) {
                LiteRunnable.this.mShouldWaiting = false;
                stub.notify();
            }
        }

        public void onFail(LiteStub stub) {
            synchronized (stub) {
                LiteRunnable.this.mShouldWaiting = false;
                stub.notify();
            }
        }
    };

    LiteRunnable(LiteContext context, LiteManager task, LiteRuntime runtime, LitePluginManager manager) {
        this.mContext = context;
        this.mTask = task;
        this.mRuntime = runtime;
        LiteStub stub = this.mTask.getStub();
        this.mPeer = new LitePluginPeer(context.getApplicationContext(), stub.strategy, stub);
        this.mManager = manager;
    }

    public void setLifecycleCallback(ILiteLifecycleCallback callback) {
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

    private void waitReady(LiteStub stub) throws InterruptedException {
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
        LiteStub stub = this.mTask.getStub();
        int state = this.mTask.getState();
        LitePlugin entity = null;
        ClassLoader cl = null;
        int err = -5;
        if (!this.mStopped) {
            label237: {
                try {
                    this.waitReady(stub);
                } catch (InterruptedException var19) {
                    LiteLog.printStackTrace(var19);
                    break label237;
                }

                if (!stub.ready) {
                    LiteLog.w("plugin not ready, won't execute!", new Object[0]);
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
                                entity.onCreate(this.mPeer);
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
                    } catch (LiteException var14) {
                        LiteLog.printStackTrace(var14);
                        err = var14.errorCode();
                    } catch (SecurityException var15) {
                        LiteLog.printStackTrace(var15);
                        err = 1;
                    } catch (Exception var16) {
                        LiteLog.printStackTrace(var16);
                        err = 2;
                    } catch (OutOfMemoryError var17) {
                        LiteLog.printStackTrace(var17);
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
        ((LiteRuntimeImpl) this.mRuntime).onComplete(stub, state, err);
    }

    public void execute() {
        if (this.isExecuting) {
            LiteLog.w("already executing...", new Object[0]);
        } else {
            this.isExecuting = true;
            this.mStopped = false;
            Thread thread = new Thread(this, "plugin-runtime-" + this.mTask.getStub().id);
            this.mThread = thread;
            thread.start();
        }
    }

    void onPluginCreated(LiteStub plugin) {
        LiteLog.v("LiteRunnable onPluginCreated", new Object[0]);
        ILiteLifecycleCallback callback = this.mCallback;
        if (callback != null) {
            callback.onPluginCreate(plugin);
        }

    }

    void onPluginStart(LiteStub plugin) {
        LiteLog.v("LiteRunnable onPluginStart", new Object[0]);
        ILiteLifecycleCallback callback = this.mCallback;
        if (callback != null) {
            callback.onPluginStart(plugin);
        }

    }

    void onPluginEnd(LiteStub plugin, int err, Object extra) {
        LiteLog.v("LiteRunnable onPluginEnd err: %d", new Object[] {Integer.valueOf(err)});
        ILiteLifecycleCallback callback = this.mCallback;
        if (callback != null) {
            callback.onPluginEnd(plugin, err, extra);
        }

    }

    void onPluginDestroy(LiteStub plugin) {
        LiteLog.v("LiteRunnable onPluginDestroy", new Object[0]);
        ILiteLifecycleCallback callback = this.mCallback;
        if (callback != null) {
            callback.onPluginDestroy(plugin);
        }

    }
}
