package com.larry.light.loader;

import android.content.Context;
import android.support.v4.app.LoaderManager;

public abstract class LightRequest<T> {

    protected final LightRequestCallbacks<T> mAbstractRequestCallbacks;

    protected Context mContext;

    private final int mLoaderId;

    private final LoaderManager mLoaderManager;


    public LightRequest(Context context, LoaderManager loaderManager, int loaderId,
                        LightRequestCallbacks<T> requestCallbacks) {

        mContext = context.getApplicationContext();
        mLoaderManager = loaderManager;
        mLoaderId = loaderId;
        mAbstractRequestCallbacks = requestCallbacks;
        register();
    }


    public void perform() {
        mLoaderManager.restartLoader(mLoaderId, null, constructLoaderCallbacks());
    }


    protected LightLoaderCallbacks<T> constructLoaderCallbacks() {
        return new LightLoaderCallbacks<T>(mContext, mAbstractRequestCallbacks, this);
    }

    public Context getContext() {
        return mContext;
    }

    public int getLoaderId() {
        return mLoaderId;
    }

    public LoaderManager getLoaderManager() {
        return mLoaderManager;
    }

    public void handleErrorInBackground(LightResponse<T> apiResponse) {
    }


    public void preProcessInBackground() throws PreProcessException {
    }

    public abstract T processInBackground(LightResponse<T> response);

    protected void register() {
        mLoaderManager.initLoader(mLoaderId, null, new LightLoaderCallbacks<T>(mContext, mAbstractRequestCallbacks,
                this));
    }

    public boolean shouldShowAlertForRequest(LightResponse<T> apiResponse) {
        return Boolean.FALSE;
    }

    public static class PreProcessException extends Exception {

        private static final long serialVersionUID = 7931249828735794298L;
    }
}
