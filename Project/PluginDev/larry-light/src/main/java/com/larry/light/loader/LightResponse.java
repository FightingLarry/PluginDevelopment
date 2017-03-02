package com.larry.light.loader;

public abstract class LightResponse<T> {

    private boolean mFailedToLoad;

    public LightResponseStatus getApiResponseStatus() {

        if (isOk()) {
            return LightResponseStatus.Ok;
        }

        if (mFailedToLoad) {
            return LightResponseStatus.Error;
        }

        return LightResponseStatus.Loading;

    }

    public void setErrorStatusIfFailedToLoad() {
        if (getApiResponseStatus() == LightResponseStatus.Loading) {
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
