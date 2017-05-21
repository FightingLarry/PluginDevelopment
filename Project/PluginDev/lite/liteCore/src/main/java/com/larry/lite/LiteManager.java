
package com.larry.lite;

public class LiteManager {
    public static final int STATE_INITED = 0;
    public static final int STATE_ATTACHED = 1;
    public static final int STATE_READY = 2;
    public static final int STATE_PREPARED = 3;
    public static final int STATE_EXECUTING = 4;
    public static final int STATE_EXECUTED = 5;
    public static final int STATE_DETACHED = 6;
    private final LiteStub mStub;
    private LiteRunnable mLiteRunnable;
    private int mState;

    public LiteManager(LiteStub stub) {
        this.mStub = stub;
        this.mState = 0;
    }

    public LiteStub getStub() {
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

    public void attach(LiteRunnable io) {
        this.mLiteRunnable = io;
        this.setState(1);
    }

    public void detach() {
        this.setState(6);
        this.mLiteRunnable = null;
    }

    public void start(LiteRunnable liteRunnable) {
        if (liteRunnable == null) {
            throw new IllegalStateException("not attach to valid runtime io");
        } else {
            this.attach(liteRunnable);
            if (this.getState() != 1) {
                throw new IllegalStateException("state is not attached before start");
            } else {
                this.mLiteRunnable.execute();
            }
        }
    }

    public void stop() {
        LiteRunnable liteRunnable = this.mLiteRunnable;
        if (liteRunnable != null) {
            if (this.getState() >= 1) {
                liteRunnable.cancel();
            }
        }
    }
}
