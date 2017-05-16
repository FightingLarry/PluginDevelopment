
package com.tcl.dc.runtime;

import com.tcl.dc.PluginStub;

public class PluginTask {
    public static final int STATE_INITED = 0;
    public static final int STATE_ATTACHED = 1;
    public static final int STATE_READY = 2;
    public static final int STATE_PREPARED = 3;
    public static final int STATE_EXECUTING = 4;
    public static final int STATE_EXECUTED = 5;
    public static final int STATE_DETACHED = 6;
    private final PluginStub mStub;
    private RuntimeIo mRuntimeIo;
    private int mState;

    public PluginTask(PluginStub stub) {
        this.mStub = stub;
        this.mState = 0;
    }

    public PluginStub getStub() {
        return this.mStub;
    }

    int setState(int state) {
        synchronized (this) {
            this.mState = state;
            return state;
        }
    }

    int getState() {
        return this.mState;
    }

    public void attach(RuntimeIo io) {
        this.mRuntimeIo = io;
        this.setState(1);
    }

    public void detach() {
        this.setState(6);
        this.mRuntimeIo = null;
    }

    public void start(RuntimeIo runtimeIo) {
        if (runtimeIo == null) {
            throw new IllegalStateException("not attach to valid runtime io");
        } else {
            this.attach(runtimeIo);
            if (this.getState() != 1) {
                throw new IllegalStateException("state is not attached before start");
            } else {
                this.mRuntimeIo.execute();
            }
        }
    }

    public void stop() {
        RuntimeIo runtimeIo = this.mRuntimeIo;
        if (runtimeIo != null) {
            if (this.getState() >= 1) {
                runtimeIo.cancel();
            }
        }
    }
}
