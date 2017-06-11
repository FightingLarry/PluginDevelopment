package com.larry.lite.db;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.larry.lite.LiteLog;
import com.larry.lite.LiteContext;
import com.larry.lite.LiteStub;
import com.larry.lite.download.DownloadTask;
import com.larry.lite.download.Downloader;
import com.larry.lite.download.DownloadURLParams;
import com.larry.lite.network.NetworkHelper;
import com.larry.lite.network.NetworkLogger;
import com.larry.lite.utils.CollectionUtils;
import com.larry.lite.LiteConfiguration;
import com.larry.lite.LitePluginManager;
import com.larry.lite.utils.FileUtils;
import com.larry.lite.utils.StringUtils;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

public class LitePluginManagerImpl implements LitePluginManager {
    private PriorityQueue<LiteEntity> mQueue = new PriorityQueue();
    private ArrayList<LiteEntity> mPluginList = new ArrayList();
    private final LitePluginsDAO mDao;
    private Downloader downloader;
    private LiteContext mContext;
    private Handler mIoHandler;
    private DownloadTask mRunningTask;
    Downloader.OnDownloadListener onDownloadListener = new Downloader.OnDownloadListener() {
        public void onDownload(LiteEntity entity, int event, int param) {
            switch (event) {
                case Downloader.DE_PROGRESS:
                    LitePluginManagerImpl.this.onProgress(param, entity);
                    break;
                case Downloader.DE_COMPLETE:
                    LitePluginManagerImpl.this.onComplete(param, entity);
                case Downloader.DE_VERIFICATION_COMPLETE:
                default:
                    break;
                case Downloader.DE_ERROR:
                    LitePluginManagerImpl.this.onError(param, entity);
            }

        }
    };
    private static final int MSG_QUEUE = 1;
    private static final int MSG_QUEUE_SINGLE = 2;
    private static final int MSG_SCHEDULE = 3;
    private static final int MSG_COMPLETE = 4;
    private static final int MSG_ERROR = 5;
    private static final int MSG_PROGRESS = 6;

    public LitePluginManagerImpl(LiteContext context) {
        this.mDao = new LitePluginsDAO(context.getApplicationContext());
        this.mContext = context;
        this.mIoHandler = new LitePluginManagerImpl.DownloadHandler(this, context.getIoLooper());
    }

    public void add(LiteStub stub) {}

    public List<LiteStub> syncWithConfiguration(LiteConfiguration configuration) {
        List<LiteStub> stubs = configuration.getPlugins();
        if (CollectionUtils.isEmpty(stubs)) {
            return null;
        } else {
            List<LiteEntity> localStubs = new ArrayList(this.mPluginList);
            ArrayList<LiteEntity> newAdds = new ArrayList();
            ArrayList<LiteStub> copies = new ArrayList();
            Iterator var6 = stubs.iterator();

            while (true) {
                while (var6.hasNext()) {
                    LiteStub stub = (LiteStub) var6.next();
                    int index = localStubs.indexOf(stub);
                    LiteEntity entity;
                    if (index >= 0) {
                        entity = (LiteEntity) localStubs.get(index);
                        if (entity.md5.equalsIgnoreCase(stub.md5)) {
                            entity.priority = 50;
                            entity.strategy = stub.strategy;
                            copies.add(entity);
                            continue;
                        }
                        localStubs.remove(stub);
                        this.clearPlugin(entity);
                    }

                    entity = new LiteEntity(stub);
                    newAdds.add(entity);
                    copies.add(entity);
                }

                if (CollectionUtils.isEmpty(newAdds)) {
                    return copies;
                }

                this.mDao.saveOrUpdateAll(newAdds);
                this.mPluginList.addAll(newAdds);
                LiteLog.d("newAdds size :%d, mPluginList size : %d , stub size : %d",
                        new Object[] {Integer.valueOf(newAdds.size()), Integer.valueOf(this.mPluginList.size()),
                                Integer.valueOf(stubs.size())});
                this.enqueue((List) newAdds);
                return copies;
            }
        }
    }

    void clearPlugin(LiteEntity entity) {
        if (entity.path != null) {
            File file = new File(entity.path);
            if (file.exists()) {
                file.delete();
            }
        }

        Context context = this.mContext.getApplicationContext();
        File dir = context.getExternalCacheDir();
        if (dir == null) {
            dir = context.getCacheDir();
        }

        if (dir != null) {
            File file = new File(dir, String.valueOf(entity.id));
            if (file.exists()) {
                FileUtils.deleteQuietly(file);
            }

        }
    }

    public List<LiteStub> getAllPlugins() {
        return (List) this.mPluginList.clone();
    }

    public boolean savePlugin(LiteStub stub) {
        if (stub instanceof LiteEntity) {
            try {
                this.mDao.saveOrUpdate((LiteEntity) stub);
                return true;
            } catch (Exception var3) {
                return false;
            }
        } else {
            throw new IllegalArgumentException("stub not entity instance");
        }
    }

    public boolean loadPlugins() {
        List<LiteEntity> all = this.mDao.queryAll();
        if (all != null) {
            this.mPluginList.addAll(all);
        }

        ArrayList<LiteEntity> unFinishList = new ArrayList();
        Iterator var3 = this.mPluginList.iterator();

        while (var3.hasNext()) {
            LiteEntity entity = (LiteEntity) var3.next();
            if (!entity.ready) {
                unFinishList.add(entity);
            }
        }

        this.enqueue((List) unFinishList);
        return true;
    }

    public boolean requestReady(LiteStub stub, PluginReadyCallback callback) {
        int index = this.mPluginList.indexOf(stub);
        if (index < 0) {
            throw new RuntimeException("plugin not exist");
        } else {
            LiteEntity entity = (LiteEntity) this.mPluginList.get(index);
            if (entity.ready) {
                return false;
            } else {
                if (entity.state == 1) {
                    entity.state = 0;
                    this.mDao.saveOrUpdate(entity);
                }

                entity.callback = new WeakReference(callback);
                entity.priority = 100;
                this.enqueue(entity);
                return true;
            }
        }
    }

    public void destroy() {
        if (this.mRunningTask != null) {
            this.mRunningTask.detach();
            this.mRunningTask.stop();
        }

    }

    private void enqueue(LiteEntity entity) {
        if (!this.isCurrentIoThread()) {
            Message msg = this.mIoHandler.obtainMessage(MSG_QUEUE_SINGLE, entity);
            msg.sendToTarget();
        } else if (!entity.ready && !StringUtils.isNull(entity.url) && !StringUtils.isNull(entity.md5)) {
            DownloadTask task = this.mRunningTask;
            if (task == null) {
                this.mQueue.add(entity);
                this.start();
            } else if (!task.getEntity().equals(entity)) {
                this.mQueue.add(entity);
            }

        }
    }

    private void enqueue(List<LiteEntity> tasks) {
        if (!this.isCurrentIoThread()) {
            Message msg = this.mIoHandler.obtainMessage(MSG_QUEUE, tasks);
            msg.sendToTarget();
        } else {
            boolean running = false;
            DownloadTask task = this.mRunningTask;
            if (task != null) {
                tasks.remove(task.getEntity());
                running = true;
            }

            if (!CollectionUtils.isEmpty(tasks)) {
                Iterator var4 = tasks.iterator();

                while (var4.hasNext()) {
                    LiteEntity entity = (LiteEntity) var4.next();
                    if (!entity.ready && !StringUtils.isNull(entity.url) && !StringUtils.isNull(entity.md5)) {
                        this.mQueue.add(entity);
                    }
                }

                LiteLog.d("mQueue size : %d", new Object[] {Integer.valueOf(this.mQueue.size())});
                if (!running) {
                    this.start();
                }

            }
        }
    }

    private LiteEntity dequeue() {
        return (LiteEntity) this.mQueue.poll();
    }

    private boolean isCurrentIoThread() {
        return Thread.currentThread() == this.mIoHandler.getLooper().getThread();
    }

    void onError(int cause, LiteEntity entity) {
        if (this.isCurrentIoThread()) {
            String desc = String.valueOf(cause);
            switch (cause) {
                case 3:
                case 4:
                case 9:
                    this.printErrorLog(entity, "req", 1, desc);
                    break;
                case 5:
                case 6:
                    this.printErrorLog(entity, "down", 1, desc);
                    break;
                case 7:
                    this.printErrorLog(entity, "install", 0, desc);
                    break;
                case 8:
                    this.printErrorLog(entity, "install", 2, desc);
            }

            if (this.mRunningTask != null) {
                this.mRunningTask.detach();
                this.mRunningTask = null;
            }

            if (++entity.retry < 3) {
                this.enqueue(entity);
            } else if (entity.callback != null) {
                LiteLog.d("callback not null", new Object[0]);
                PluginReadyCallback callback = (PluginReadyCallback) entity.callback.get();
                if (callback != null) {
                    LiteLog.d("callback success", new Object[0]);
                    callback.onFail(entity);
                }
            }

            this.postSchedule();
        } else {
            Message msg = this.mIoHandler.obtainMessage(MSG_ERROR, entity);
            msg.sendToTarget();
        }

    }

    void onProgress(int progress, LiteEntity entity) {
        if (this.isCurrentIoThread()) {
            LiteLog.d("progress %d", new Object[] {Integer.valueOf(progress)});
            this.mDao.saveOrUpdate(entity);
        } else {
            Message msg = this.mIoHandler.obtainMessage(MSG_PROGRESS, entity);
            msg.arg1 = progress;
            msg.sendToTarget();
        }

    }

    void onComplete(int cause, LiteEntity entity) {
        if (this.isCurrentIoThread()) {
            if (cause != 1) {
                if (cause == 0) {
                    LiteLog.d("download success", new Object[0]);
                    entity.state = 1;
                    this.mDao.saveOrUpdate(entity);
                } else if (cause == 2) {
                    LiteLog.d("verification success", new Object[0]);
                    entity.ready = true;
                    this.mDao.saveOrUpdate(entity);
                    if (entity.callback != null) {
                        LiteLog.d("callback not null", new Object[0]);
                        PluginReadyCallback callback = (PluginReadyCallback) entity.callback.get();
                        if (callback != null) {
                            LiteLog.d("callback success", new Object[0]);
                            callback.onReady(entity);
                        }
                    }

                    if (this.mRunningTask != null) {
                        this.mRunningTask.detach();
                        this.mRunningTask = null;
                    }

                    this.postSchedule();
                }
            }
        } else {
            Message msg = this.mIoHandler.obtainMessage(MSG_COMPLETE, entity);
            msg.arg1 = cause;
            msg.sendToTarget();
        }

    }

    void start() {
        this.postSchedule();
    }

    private void postSchedule() {
        this.mIoHandler.removeMessages(MSG_SCHEDULE);
        this.mIoHandler.sendEmptyMessageDelayed(MSG_SCHEDULE, 100L);
    }

    private void schedule() {
        if (this.mRunningTask == null && this.mQueue.size() != 0) {
            if (!NetworkHelper.sharedHelper().isNetworkAvailable()) {
                LiteLog.w("schedule execute no avalibale network", new Object[0]);
                this.stopAll(4);
            } else {
                LiteEntity entity = this.dequeue();
                if (entity == null) {
                    LiteLog.d("queue has no entity", new Object[0]);
                } else {
                    LiteLog.v("download schedule %d ", new Object[] {Integer.valueOf(entity.id)});
                    DownloadTask task = new DownloadTask(entity);
                    this.mRunningTask = task;
                    this.downloader = new Downloader(this.mContext, task, (DownloadURLParams.URLParamsCreator) null);
                    this.downloader.setOnDownloadListener(this.onDownloadListener);
                    task.start(this.downloader);
                }
            }
        }
    }

    void stopAll(int cause) {
        LiteLog.v("stopAll cause= %d", new Object[] {Integer.valueOf(cause)});
        this.mQueue.clear();
        if (this.mRunningTask != null) {
            this.mRunningTask.stop();
            this.mRunningTask = null;
        }

    }

    private void printErrorLog(LiteEntity entity, String type, int sta, String desc) {
        NetworkLogger.reportDownloaded(this.mContext, entity.id, entity.md5, type, sta, desc);
    }

    private static class DownloadHandler extends Handler {
        WeakReference<LitePluginManagerImpl> mRef;

        public DownloadHandler(LitePluginManagerImpl impl, Looper looper) {
            super(looper);
            this.mRef = new WeakReference(impl);
        }

        public void handleMessage(Message msg) {
            LitePluginManagerImpl impl = (LitePluginManagerImpl) this.mRef.get();
            if (impl != null) {
                LiteEntity entity;
                switch (msg.what) {
                    case MSG_QUEUE:
                        List<LiteEntity> plugins = (List) msg.obj;
                        impl.enqueue(plugins);
                        break;
                    case MSG_QUEUE_SINGLE:
                        entity = (LiteEntity) msg.obj;
                        impl.enqueue(entity);
                        break;
                    case MSG_SCHEDULE:
                        this.removeMessages(MSG_SCHEDULE);
                        impl.schedule();
                        break;
                    case MSG_COMPLETE:
                        entity = (LiteEntity) msg.obj;
                        impl.onComplete(msg.arg1, entity);
                        break;
                    case MSG_ERROR:
                        entity = (LiteEntity) msg.obj;
                        impl.onError(msg.arg1, entity);
                        break;
                    case 6:
                        entity = (LiteEntity) msg.obj;
                        impl.onProgress(msg.arg1, entity);
                }

            }
        }
    }
}
