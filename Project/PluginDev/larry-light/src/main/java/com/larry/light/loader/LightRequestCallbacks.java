package com.larry.light.loader;

public abstract class LightRequestCallbacks<T> {

    public void onRequestStart() {

    }

    public void onRequestFinished() {

    }

    protected void onRequestFail(LightResponse<T> response) {

    }

    protected abstract void onSuccess(T t);

}
