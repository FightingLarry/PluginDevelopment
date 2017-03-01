package com.dh.baseactivity.loader;

public abstract class AbstractResponse<T> {

    private boolean mFailedToLoad;

    public ResponseStatus getApiResponseStatus() {

        if (isOk()) {
            return ResponseStatus.Ok;
        }

        if (mFailedToLoad) {
            return ResponseStatus.Error;
        }

        return ResponseStatus.Loading;

    }

    public void setErrorStatusIfFailedToLoad() {
        if (getApiResponseStatus() == ResponseStatus.Loading) {
            mFailedToLoad = true;
        } else {
            mFailedToLoad = false;
        }
    }


    public abstract String getErrorTitle();

    public abstract String getErrorMessage();

    public abstract void setErrorMessage(String errorMessage);

    public abstract int getMetaCode();

    public abstract T getSuccessObject();


    public abstract boolean isOk();

}
