//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.tcl.dc.manager.download;

import android.text.TextUtils;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.squareup.okhttp.Request.Builder;
import com.tcl.dc.PLog;
import com.tcl.dc.PluginContext;
import com.tcl.dc.PluginException;
import com.tcl.dc.internal.PluginClassLoader;
import com.tcl.dc.manager.PluginEntity;
import com.tcl.dc.manager.download.URLParams.URLParamsCreator;
import com.tcl.dc.utils.FileUtils;
import com.tcl.dc.utils.MD5Util;
import com.tcl.dc.utils.Streams;
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

public class PluginDownloader implements URLParamsCreator<PluginEntity>, Runnable {
    public static final int DE_PROGRESS = 1;
    public static final int DE_COMPLETE = 2;
    public static final int DE_VERIFICATION_COMPLETE = 3;
    public static final int DE_ERROR = 4;
    private static final int BUFF_LEN = 4096;
    protected PluginEntity mEntity = null;
    protected PluginDownloader.OnDownloadListener mOnDownloadListener = null;
    private URLParams<PluginEntity> mParams = null;
    private URLParamsCreator<PluginEntity> mParamsCreator;
    private Thread mDownloadThread;
    private volatile boolean isExecuting = false;
    private OkHttpClient mClient;
    private Call mCall;
    private PluginDownloader.ResumeableContentHandler mContentHandler = new PluginDownloader.ResumeableContentHandler();
    private volatile boolean mCancelled;
    private PluginContext mContext;
    private PluginDownloadTask mTask;
    int oldProgress = 0;

    public PluginDownloader(PluginContext context, PluginDownloadTask task, URLParamsCreator<PluginEntity> creator) {
        this.mContext = context;
        this.mClient = context.getConnectionFactory().getOkHttpClient();
        this.mTask = task;
        this.mEntity = task.getEntity();
        this.mParamsCreator = (URLParamsCreator) (creator != null ? creator : this);
        this.mParams = this.mParamsCreator.createUrlParams(this.mEntity);
        Request request = this.createRequest(this.mParams);
        this.mCall = this.mClient.newCall(request);
    }

    public void start() {
        if (this.isExecuting) {
            PLog.w("already executing...", new Object[0]);
        } else {
            this.isExecuting = true;
            this.mCancelled = false;
            Thread thread = new Thread(this, "plugin-download-" + this.mTask.getEntity().id);
            this.mDownloadThread = thread;
            thread.start();
        }
    }

    public URLParams<PluginEntity> createUrlParams(PluginEntity entity) {
        URLParams<PluginEntity> params = new URLParams();
        params.url = entity.url;
        if (!TextUtils.isEmpty(entity.path) && FileUtils.exists(entity.path)) {
            if (entity.path.endsWith(".wmp")) {
                params.target = new File(entity.path.substring(0, entity.path.length() - ".wmp".length()));
            } else {
                params.target = new File(entity.path);
            }
        } else {
            String filename =
                    String.format(Locale.ENGLISH, "%d_%s.tdp", new Object[] {Integer.valueOf(entity.id), entity.md5});
            params.target = FileUtils.getPluginsFile(this.mContext.getApplicationContext(), filename);
        }

        params.entity = entity;
        return params;
    }

    private Request createRequest(URLParams<PluginEntity> params) {
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

        PLog.i("create okhttp request for url %s", new Object[] {params.url});
        return builder.build();
    }

    public void setOnDownloadListener(PluginDownloader.OnDownloadListener listener) {
        this.mOnDownloadListener = listener;
    }

    protected void notifyDownloadEvent(int event, int param) {
        PluginDownloader.OnDownloadListener listener = this.mOnDownloadListener;
        if (listener != null) {
            listener.onDownload(this.mEntity, event, param);
        }

    }

    protected void onAdvance(int progress) {
        if (this.oldProgress != progress) {
            this.oldProgress = progress;
            this.notifyDownloadEvent(1, progress);
        }
    }

    protected void onError(int cause) {
        this.onAdvance(0);
        this.mParams.target.delete();
        this.notifyDownloadEvent(4, cause);
    }

    protected void onComplete(int cause) {
        this.notifyDownloadEvent(2, cause);
    }

    public void run() {
        byte err;
        PluginEntity entity;
        URLParams params;
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
                                PLog.w("no available network!", new Object[0]);
                                this.onError(4);
                                return;
                            }
                        }
                    }

                    if (this.mCancelled) {
                        this.onError(5);
                        return;
                    }

                    PluginDownloader.ResumeableContentHandler contentHandler = this.mContentHandler;
                    long total = body.contentLength();
                    if (total > 0L) {
                        total += entity.downloaded;
                    }

                    if (!contentHandler.prepare(entity.downloaded, total)) {
                        PLog.w("content-length not invalid!", new Object[0]);
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
                                PLog.printStackTrace(var25);
                                ret = 6;
                            }
                        } finally {
                            Streams.safeClose(inputStream);
                        }
                    }

                    PLog.d("current length: %d", new Object[] {Long.valueOf(current)});
                    ret = !this.mCancelled ? ret : 5;
                    contentHandler.complete(ret, (int) current);
                }
            } catch (Exception var27) {
                PLog.printStackTrace(var27);
            }
        }

        this.onComplete(0);
        PLog.i("download completed, error = %d", new Object[] {Integer.valueOf(err)});
        String md5 = MD5Util.getFileMD5(params.target.getAbsolutePath());
        if (entity.md5.equalsIgnoreCase(md5)) {
            try {
                PluginClassLoader.verificationManifest(params.target);
                this.onComplete(2);
                PLog.d("verification manifest success! %d", new Object[] {Integer.valueOf(entity.id)});
            } catch (PluginException var24) {
                PLog.d("verification manifest fail! %s", new Object[] {md5});
                this.onError(8);
                var24.printStackTrace();
            }
        } else {
            this.onError(7);
            PLog.d("verification md5 fail!", new Object[] {Integer.valueOf(entity.id)});
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
            PLog.v("complete cause = %d, length = %d", new Object[] {Integer.valueOf(cause), Integer.valueOf(curpos)});
            if (this.mAccessFile != null) {
                Streams.safeClose(this.mAccessFile);
                this.mAccessFile = null;
            }

            if (cause == 0) {
                boolean ret = this.mCacheFile.renameTo(PluginDownloader.this.mParams.target);
                PluginDownloader.this.mEntity.path = PluginDownloader.this.mParams.target.getAbsolutePath();
                PLog.i("cache file renameTo %s, result:%b",
                        new Object[] {Long.valueOf(PluginDownloader.this.mEntity.downloaded), Boolean.valueOf(ret)});
            }

        }

        public boolean handle(byte[] datas, int offset, int len) {
            if (PluginDownloader.this.mCancelled) {
                return false;
            } else {
                if (this.mAccessFile != null && len > 0) {
                    try {
                        this.mAccessFile.write(datas, offset, len);
                        this.mCurrentLength += (long) len;
                        PluginDownloader.this.mEntity.downloaded = this.mCurrentLength;
                        int progress = (int) (this.mCurrentLength * 100L / this.mTotalLength);
                        PluginDownloader.this.onAdvance(progress);
                    } catch (IOException var5) {
                        PLog.printStackTrace(var5);
                        Streams.safeClose(this.mAccessFile);
                        this.mAccessFile = null;
                        return false;
                    }
                }

                return true;
            }
        }

        public boolean prepare(long curpos, long total) {
            PLog.v("prepare total = %d, curpos = %d", new Object[] {Long.valueOf(total), Long.valueOf(curpos)});
            if (total <= 0L) {
                return false;
            } else {
                String path = PluginDownloader.this.mEntity.path;
                if (!TextUtils.isEmpty(path)) {
                    this.mCacheFile = new File(path);
                } else {
                    this.mCacheFile = FileUtils.getTempFile(PluginDownloader.this.mParams.target);
                    PluginDownloader.this.mEntity.path = this.mCacheFile.getAbsolutePath();
                }

                if (!this.mCacheFile.exists()) {
                    try {
                        FileUtils.create(this.mCacheFile);
                    } catch (Exception var9) {
                        PLog.printStackTrace(var9);
                        return false;
                    }
                } else if (this.mCacheFile.length() != curpos) {
                    this.mCacheFile.delete();

                    try {
                        FileUtils.create(this.mCacheFile);
                    } catch (Exception var8) {
                        PLog.printStackTrace(var8);
                        return false;
                    }
                }

                try {
                    this.mAccessFile = new RandomAccessFile(this.mCacheFile, "rw");
                    this.mCurrentLength = curpos;
                    this.mTotalLength = total;
                    PluginDownloader.this.mEntity.size = this.mTotalLength;
                    PluginDownloader.this.mEntity.downloaded = this.mCurrentLength;
                    if (this.mCurrentLength > 0L) {
                        this.mAccessFile.seek(this.mCurrentLength);
                    }

                    return true;
                } catch (IOException var7) {
                    PLog.printStackTrace(var7);
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
