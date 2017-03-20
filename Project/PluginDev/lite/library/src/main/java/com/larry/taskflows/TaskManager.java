package com.larry.taskflows;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

/**
 * Created by larry on 2016/11/25.
 */
public class TaskManager {

    private static final HandlerThread sWorkerThread = new HandlerThread("task-worker-thread");

    static {
        sWorkerThread.start();
    }

    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());

    public static void runOnWorkerThread(Task t) {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            t.run();
        } else {
            // If we are not on the worker thread, then post to the worker
            // handler
            sWorker.post(t);
        }
    }


    private static final HandlerThread sDownloadThread = new HandlerThread("task-download-thread");

    static {
        sDownloadThread.start();
    }

    private static final Handler sDownloadHandler = new Handler(sDownloadThread.getLooper());

    public static void runOnDownloadThread(Task t) {
        if (sDownloadThread.getThreadId() == Process.myTid()) {
            t.run();
        } else {
            // If we are not on the worker thread, then post to the worker
            // handler
            sDownloadHandler.post(t);
        }
    }


}
