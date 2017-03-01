package com.dh.baseactivity.loader;

import android.content.Context;
import android.support.v4.app.LoaderManager;

public abstract class AbstractRequest<T> {

    protected final AbstractCallbacks<T> mApiCallbacks;

    protected Context mContext;

    private final int mLoaderId;

    private final LoaderManager mLoaderManager;


    public AbstractRequest(Context context, LoaderManager loaderManager, int loaderId,
                           AbstractCallbacks<T> apiCallbacks) {

        mContext = context.getApplicationContext();
        mLoaderManager = loaderManager;
        mLoaderId = loaderId;
        mApiCallbacks = apiCallbacks;
        register();
    }


    public void perform() {
        mLoaderManager.restartLoader(mLoaderId, null, constructLoaderCallbacks());
    }


    protected BaseLoaderCallbacks<T> constructLoaderCallbacks() {
        return new BaseLoaderCallbacks<T>(mContext, mApiCallbacks, this);
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

    public void handleErrorInBackground(AbstractResponse<T> apiResponse) {
    }


    public void preProcessInBackground() throws PreProcessException {
    }

    public abstract T processInBackground(AbstractResponse<T> response);

    protected void register() {
        mLoaderManager.initLoader(mLoaderId, null, new BaseLoaderCallbacks<T>(mContext, mApiCallbacks,
                this));
    }

    public boolean shouldShowAlertForRequest(AbstractResponse<T> apiResponse) {
        return Boolean.FALSE;
    }

    public static class PreProcessException extends Exception {

        private static final long serialVersionUID = 7931249828735794298L;
    }
}
