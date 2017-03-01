package com.dh.baseactivity.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public class AsyncTaskDataLoader<D> extends AsyncTaskLoader<D> {

    public AsyncTaskDataLoader(Context context) {
        super(context);
    }

    @Override
    public void deliverResult(D d) {
        if (!isReset()) {
            super.deliverResult(d);
        }
    }

    @Override
    public D loadInBackground() {
        return null;
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
    }
}
