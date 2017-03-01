package com.dh.baseactivity.loader;

public abstract class AbstractCallbacks<T> {

    public void onRequestStart() {

    }

    public void onRequestFinished() {

    }

    protected void onRequestFail(AbstractResponse<T> response) {

    }

    protected abstract void onSuccess(T t);

}
