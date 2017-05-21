/**
 * Created by Larry on 2016/11/25.
 */
package com.larry.taskflows;

import android.content.Context;
import android.util.Log;


public abstract class Task implements Runnable {

    public static final String TAG = "Task";

    protected Context mContext;

    public Task(Context context) {
        this.mContext = context;
    }

    private Task() {

    }

    public void onStart() {
        Log.i(TAG, getClass().getSimpleName() + "->onStart()->" + Thread.currentThread().getName());
    }

    @Override
    public void run() {
        onStart();
        try {
            onExecute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            onEnd();
        }
    }

    protected abstract void onExecute() throws Exception;

    protected void onEnd() {
        Log.i(TAG, getClass().getSimpleName() + "->onEnd()->" + Thread.currentThread().getName());
    }


}
