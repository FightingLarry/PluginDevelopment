
package com.larry.lite.download;

import com.larry.lite.db.LiteEntity;

public class DownloadTask {
    private final LiteEntity mEntity;
    private Downloader mDownloader;

    public DownloadTask(LiteEntity entity) {
        this.mEntity = entity;
    }

    public LiteEntity getEntity() {
        return this.mEntity;
    }

    public void attach(Downloader downloader) {
        this.mDownloader = downloader;
    }

    public void detach() {
        this.mDownloader = null;
    }

    public void start(Downloader downloader) {
        if (downloader == null) {
            throw new IllegalStateException("downloader can not be null");
        } else {
            this.attach(downloader);
            downloader.start();
        }
    }

    public void stop() {
        Downloader downloader = this.mDownloader;
        if (downloader != null) {
            downloader.cancel();
        }
    }
}
