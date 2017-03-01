package com.dh.baseactivity.loader;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

public class BaseLoaderCallbacks<T> implements LoaderCallbacks<AbstractResponse<T>> {

    private final AbstractCallbacks<T> mApiCallbacks;

    protected final Context mContext;

    private AbstractRequest<T> mRequest;

    public BaseLoaderCallbacks(Context context, AbstractCallbacks<T> apiCallbacks,
                               AbstractRequest<T> request) {

        mContext = context.getApplicationContext();
        mApiCallbacks = apiCallbacks;
        mRequest = request;
    }

    public static void handleRequestServerErrorMessage(final String errorTitle,
                                                       final String errorMessage) {
        //TODO
    }

    public AbstractCallbacks<T> getApiCallbacks() {
        return this.mApiCallbacks;
    }

    public Context getContext() {
        return this.mContext;
    }

    @Override
    public Loader<AbstractResponse<T>> onCreateLoader(int loadId, Bundle bundle) {
        return new AsyncTaskDataLoader<AbstractResponse<T>>(mContext);
    }

    @Override
    public void onLoadFinished(Loader<AbstractResponse<T>> loader, AbstractResponse<T> response) {

        mApiCallbacks.onRequestFinished();

        if (response == null) {
            mApiCallbacks.onRequestFail(response);
            return;
        }

        if (response.getMetaCode() != ResponseCode.META_CODE_OK) {


            if (mRequest.shouldShowAlertForRequest(response)) {
                handleRequestServerErrorMessage(response.getErrorTitle(),
                        response.getErrorMessage());
            }

            response.setErrorStatusIfFailedToLoad();
            mApiCallbacks.onRequestFail(response);
        }

        if (response.isOk()) {
            mApiCallbacks.onSuccess(response.getSuccessObject());
        }
    }

    @Override
    public void onLoaderReset(Loader<AbstractResponse<T>> loader) {
    }

}
