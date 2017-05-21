
package com.larry.lite.download;

import android.text.TextUtils;

import com.larry.lite.LiteLog;
import com.larry.lite.LiteContext;
import com.larry.lite.utils.MD5Util;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.squareup.okhttp.Request.Builder;
import com.larry.lite.LiteException;
import com.larry.lite.obtain.LiteObtainSdCardPlugin;
import com.larry.lite.obtain.LiteClassLoader;
import com.larry.lite.db.PluginEntity;
import com.larry.lite.utils.FileUtils;
import com.larry.lite.utils.Streams;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Downloader implements DownloadURLParams.URLParamsCreator<PluginEntity>, Runnable {
    public static final int DE_PROGRESS = 1;
    public static final int DE_COMPLETE = 2;
    public static final int DE_VERIFICATION_COMPLETE = 3;
    public static final int DE_ERROR = 4;
    private static final int BUFF_LEN = 4096;
    protected PluginEntity mEntity = null;
    protected Downloader.OnDownloadListener mOnDownloadListener = null;
    private DownloadURLParams<PluginEntity> mParams = null;
    private DownloadURLParams.URLParamsCreator<PluginEntity> mParamsCreator;
    private Thread mDownloadThread;
    private volatile boolean isExecuting = false;
    private OkHttpClient mClient;
    private Call mCall;
    private Downloader.ResumeableContentHandler mContentHandler = new Downloader.ResumeableContentHandler();
    private volatile boolean mCancelled;
    private LiteContext mContext;
    private DownloadTask mTask;
    int oldProgress = 0;

    public Downloader(LiteContext context, DownloadTask task,
                      DownloadURLParams.URLParamsCreator<PluginEntity> creator) {
        this.mContext = context;
        this.mClient = context.getConnectionFactory().getOkHttpClient();
        this.mTask = task;
        this.mEntity = task.getEntity();
        this.mParamsCreator = (DownloadURLParams.URLParamsCreator) (creator != null ? creator : this);
        this.mParams = this.mParamsCreator.createUrlParams(this.mEntity);
        Request request = this.createRequest(this.mParams);
        this.mCall = this.mClient.newCall(request);
    }

    public void start() {
        if (this.isExecuting) {
            LiteLog.w("already executing...", new Object[0]);
        } else {
            this.isExecuting = true;
            this.mCancelled = false;
            Thread thread = new Thread(this, "plugin-download-" + this.mTask.getEntity().id);
            this.mDownloadThread = thread;
            thread.start();
        }
    }

    public DownloadURLParams<PluginEntity> createUrlParams(PluginEntity entity) {
        DownloadURLParams<PluginEntity> params = new DownloadURLParams();
        params.url = entity.url;
        if (!TextUtils.isEmpty(entity.path) && FileUtils.exists(entity.path)) {
            if (entity.path.endsWith(".wmp")) {
                params.target = new File(entity.path.substring(0, entity.path.length() - ".wmp".length()));
            } else {
                params.target = new File(entity.path);
            }
        } else {
            String filename = String.format(Locale.ENGLISH, "%d_%s%s",
                    new Object[] {Integer.valueOf(entity.id), entity.md5, LiteObtainSdCardPlugin.PLUGIN_SUFFIX});
            params.target = FileUtils.getPluginsFile(this.mContext.getApplicationContext(), filename);
        }

        params.entity = entity;
        return params;
    }

    private Request createRequest(DownloadURLParams<PluginEntity> params) {
        Map<String, String> headers = null;
        long pos = ((PluginEntity) params.entity).downloaded;
        long len = ((PluginEntity) params.entity).size;
        if (len > 0L) {
            headers = new HashMap();
            headers.put("RANGE", "bytes=" + pos + "-" + (pos + len - 1L));
        } else if (pos > 0L) {
            headers = new HashMap();
            headers.put("RANGE", "bytes=" + pos + "-");
        }

        Builder builder = (new Builder()).header("User-Agent", this.mContext.getUserAgent()).url(params.url);
        if (headers != null) {
            Set<Entry<String, String>> entries = headers.entrySet();
            Iterator var9 = entries.iterator();

            while (var9.hasNext()) {
                Entry<String, String> entry = (Entry) var9.next();
                builder.header((String) entry.getKey(), (String) entry.getValue());
            }
        }

        LiteLog.i("create okhttp request for url %s", new Object[] {params.url});
        return builder.build();
    }

    public void setOnDownloadListener(Downloader.OnDownloadListener listener) {
        this.mOnDownloadListener = listener;
    }

    protected void notifyDownloadEvent(int event, int param) {
        Downloader.OnDownloadListener listener = this.mOnDownloadListener;
        if (listener != null) {
            listener.onDownload(this.mEntity, event, param);
        }

    }

    protected void onAdvance(int progress) {
        if (this.oldProgress != progress) {
            this.oldProgress = progress;
            this.notifyDownloadEvent(DE_PROGRESS, progress);
        }
    }

    protected void onError(int cause) {
        this.onAdvance(0);
        this.mParams.target.delete();
        this.notifyDownloadEvent(DE_ERROR, cause);
    }

    protected void onComplete(int cause) {
        this.notifyDownloadEvent(DE_COMPLETE, cause);
    }

    public void run() {
        byte err;
        PluginEntity entity;
        DownloadURLParams params;
        label341: {
            err = 0;
            entity = this.mEntity;
            params = this.mParams;
            boolean change = false;
            File file;
            boolean exists;
            if (entity.downloaded == entity.size && entity.size > 0L) {
                file = params.target;
                exists = file.exists();
                boolean cached = false;
                if (!exists) {
                    file = FileUtils.getTempFile(file);
                    exists = file.exists();
                    cached = true;
                }

                if (exists && file.length() == entity.size) {
                    if (cached) {
                        file.renameTo(params.target);
                    }

                    err = 0;
                    break label341;
                }

                if (exists) {
                    file.delete();
                }

                entity.size = 0L;
                entity.downloaded = 0L;
                change = true;
            } else if (entity.downloaded > entity.size) {
                file = params.target;
                exists = file.exists();
                if (!exists) {
                    file = FileUtils.getTempFile(this.mParams.target);
                    exists = file.exists();
                }

                if (exists && file.length() != entity.downloaded) {
                    file.delete();
                    entity.size = 0L;
                    entity.downloaded = 0L;
                    change = true;
                } else if (!exists) {
                    entity.size = 0L;
                    entity.downloaded = 0L;
                    change = true;
                }
            } else {
                file = FileUtils.getTempFile(this.mParams.target);
                if (file.exists()) {
                    if (entity.size != file.length()) {
                        entity.size = file.length();
                        change = true;
                    }
                } else if (entity.downloaded != 0L) {
                    entity.downloaded = 0L;
                    change = true;
                }
            }

            try {
                if (change) {
                    Request request = this.createRequest(params);
                    this.mCall = this.mClient.newCall(request);
                }

                if (this.mCancelled) {
                    err = 5;
                } else {
                    Response response = this.mCall.execute();
                    int ret = 0;
                    if (!response.isSuccessful()) {
                        if (response.code() == 404) {
                            ret = 3;
                        } else {
                            ret = 9;
                        }

                        this.onError(ret);
                        return;
                    }

                    this.onComplete(1);
                    String[] exceptionContentType = new String[] {"text", "html"};
                    ResponseBody body = response.body();
                    MediaType mediaType = body.contentType();
                    if (mediaType != null && !TextUtils.isEmpty(mediaType.type())) {
                        String type = mediaType.type().toLowerCase();
                        String[] var11 = exceptionContentType;
                        int var12 = exceptionContentType.length;

                        for (int var13 = 0; var13 < var12; ++var13) {
                            String s = var11[var13];
                            if (s.equals(type)) {
                                LiteLog.w("no available network!", new Object[0]);
                                this.onError(4);
                                return;
                            }
                        }
                    }

                    if (this.mCancelled) {
                        this.onError(5);
                        return;
                    }

                    Downloader.ResumeableContentHandler contentHandler = this.mContentHandler;
                    long total = body.contentLength();
                    if (total > 0L) {
                        total += entity.downloaded;
                    }

                    if (!contentHandler.prepare(entity.downloaded, total)) {
                        LiteLog.w("content-length not invalid!", new Object[0]);
                        this.onError(6);
                        return;
                    }

                    long current = entity.downloaded;
                    if (this.mCancelled) {
                        ret = 5;
                    } else {
                        byte[] buf = new byte[4096];
                        InputStream inputStream = body.byteStream();

                        try {
                            int len;
                            try {
                                while (!this.mCancelled && (len = inputStream.read(buf)) != -1) {
                                    if (len != 0) {
                                        if (!contentHandler.handle(buf, 0, len)) {
                                            ret = 6;
                                            break;
                                        }

                                        current += (long) len;
                                    }
                                }
                            } catch (Exception var25) {
                                LiteLog.printStackTrace(var25);
                                ret = 6;
                            }
                        } finally {
                            Streams.safeClose(inputStream);
                        }
                    }

                    LiteLog.d("current length: %d", new Object[] {Long.valueOf(current)});
                    ret = !this.mCancelled ? ret : 5;
                    contentHandler.complete(ret, (int) current);
                }
            } catch (Exception var27) {
                LiteLog.printStackTrace(var27);
            }
        }

        this.onComplete(0);
        LiteLog.i("download completed, error = %d", new Object[] {Integer.valueOf(err)});
        String md5 = MD5Util.getFileMD5(params.target.getAbsolutePath());
        if (entity.md5.equalsIgnoreCase(md5)) {
            try {
                LiteClassLoader.verificationManifest(params.target);
                this.onComplete(2);
                LiteLog.d("verification manifest success! %d", new Object[] {Integer.valueOf(entity.id)});
            } catch (LiteException var24) {
                LiteLog.d("verification manifest fail! %s", new Object[] {md5});
                this.onError(8);
                var24.printStackTrace();
            }
        } else {
            this.onError(7);
            LiteLog.d("verification md5 fail!", new Object[] {Integer.valueOf(entity.id)});
        }

        this.isExecuting = false;
        this.mDownloadThread = null;
        this.mTask.detach();
        this.mTask = null;
    }

    public void cancel() {
        if (!this.mCancelled) {
            this.mCancelled = true;
            Thread thread = this.mDownloadThread;
            if (thread != null && thread.isAlive()) {
                thread.interrupt();

                try {
                    thread.join(20L);
                } catch (InterruptedException var3) {
                    ;
                }
            }

        }
    }

    private class ResumeableContentHandler {
        File mCacheFile;
        long mCurrentLength;
        long mTotalLength;
        RandomAccessFile mAccessFile;

        private ResumeableContentHandler() {
            this.mCurrentLength = 0L;
            this.mTotalLength = 0L;
        }

        public void complete(int cause, int curpos) {
            LiteLog.v("complete cause = %d, length = %d", new Object[] {Integer.valueOf(cause), Integer.valueOf(curpos)});
            if (this.mAccessFile != null) {
                Streams.safeClose(this.mAccessFile);
                this.mAccessFile = null;
            }

            if (cause == 0) {
                boolean ret = this.mCacheFile.renameTo(Downloader.this.mParams.target);
                Downloader.this.mEntity.path = Downloader.this.mParams.target.getAbsolutePath();
                LiteLog.i("cache file renameTo %s, result:%b",
                        new Object[] {Long.valueOf(Downloader.this.mEntity.downloaded), Boolean.valueOf(ret)});
            }

        }

        public boolean handle(byte[] datas, int offset, int len) {
            if (Downloader.this.mCancelled) {
                return false;
            } else {
                if (this.mAccessFile != null && len > 0) {
                    try {
                        this.mAccessFile.write(datas, offset, len);
                        this.mCurrentLength += (long) len;
                        Downloader.this.mEntity.downloaded = this.mCurrentLength;
                        int progress = (int) (this.mCurrentLength * 100L / this.mTotalLength);
                        Downloader.this.onAdvance(progress);
                    } catch (IOException var5) {
                        LiteLog.printStackTrace(var5);
                        Streams.safeClose(this.mAccessFile);
                        this.mAccessFile = null;
                        return false;
                    }
                }

                return true;
            }
        }

        public boolean prepare(long curpos, long total) {
            LiteLog.v("prepare total = %d, curpos = %d", new Object[] {Long.valueOf(total), Long.valueOf(curpos)});
            if (total <= 0L) {
                return false;
            } else {
                String path = Downloader.this.mEntity.path;
                if (!TextUtils.isEmpty(path)) {
                    this.mCacheFile = new File(path);
                } else {
                    this.mCacheFile = FileUtils.getTempFile(Downloader.this.mParams.target);
                    Downloader.this.mEntity.path = this.mCacheFile.getAbsolutePath();
                }

                if (!this.mCacheFile.exists()) {
                    try {
                        FileUtils.create(this.mCacheFile);
                    } catch (Exception var9) {
                        LiteLog.printStackTrace(var9);
                        return false;
                    }
                } else if (this.mCacheFile.length() != curpos) {
                    this.mCacheFile.delete();

                    try {
                        FileUtils.create(this.mCacheFile);
                    } catch (Exception var8) {
                        LiteLog.printStackTrace(var8);
                        return false;
                    }
                }

                try {
                    this.mAccessFile = new RandomAccessFile(this.mCacheFile, "rw");
                    this.mCurrentLength = curpos;
                    this.mTotalLength = total;
                    Downloader.this.mEntity.size = this.mTotalLength;
                    Downloader.this.mEntity.downloaded = this.mCurrentLength;
                    if (this.mCurrentLength > 0L) {
                        this.mAccessFile.seek(this.mCurrentLength);
                    }

                    return true;
                } catch (IOException var7) {
                    LiteLog.printStackTrace(var7);
                    Streams.safeClose(this.mAccessFile);
                    return false;
                }
            }
        }
    }

    public interface OnDownloadListener {
        void onDownload(PluginEntity var1, int var2, int var3);
    }
}
