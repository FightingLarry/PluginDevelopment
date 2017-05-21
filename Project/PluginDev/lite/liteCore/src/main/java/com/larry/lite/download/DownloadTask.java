
package com.larry.lite.download;

import com.larry.lite.db.PluginEntity;

public class DownloadTask {
    private final PluginEntity mEntity;
    private Downloader mDownloader;

    public DownloadTask(PluginEntity entity) {
        this.mEntity = entity;
    }

    public PluginEntity getEntity() {
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
