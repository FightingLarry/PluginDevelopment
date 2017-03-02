package com.larry.light.loader;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

public class LightLoaderCallbacks<T> implements LoaderCallbacks<LightResponse<T>> {

    private final LightRequestCallbacks<T> mApiCallbacks;

    protected final Context mContext;

    private LightRequest<T> mRequest;

    public LightLoaderCallbacks(Context context, LightRequestCallbacks<T> apiCallbacks,
                                LightRequest<T> request) {

        mContext = context.getApplicationContext();
        mApiCallbacks = apiCallbacks;
        mRequest = request;
    }

    public static void handleRequestServerErrorMessage(final String errorTitle,
                                                       final String errorMessage) {
        //TODO
    }

    public LightRequestCallbacks<T> getApiCallbacks() {
        return this.mApiCallbacks;
    }

    public Context getContext() {
        return this.mContext;
    }

    @Override
    public Loader<LightResponse<T>> onCreateLoader(int loadId, Bundle bundle) {
        return new LightAsyncTaskLoader<LightResponse<T>>(mContext);
    }

    @Override
    public void onLoadFinished(Loader<LightResponse<T>> loader, LightResponse<T> response) {

        mApiCallbacks.onRequestFinished();

        if (response == null) {
            mApiCallbacks.onRequestFail(response);
            return;
        }

        if (response.getMetaCode() != LightResponseCode.META_CODE_OK) {


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
    public void onLoaderReset(Loader<LightResponse<T>> loader) {
    }

}
