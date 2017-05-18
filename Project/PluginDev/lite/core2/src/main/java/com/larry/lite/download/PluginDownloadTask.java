
package com.larry.lite.download;

import com.larry.lite.db.PluginEntity;

public class PluginDownloadTask {
    private final PluginEntity mEntity;
    private PluginDownloader mDownloader;

    public PluginDownloadTask(PluginEntity entity) {
        this.mEntity = entity;
    }

    public PluginEntity getEntity() {
        return this.mEntity;
    }

    public void attach(PluginDownloader downloader) {
        this.mDownloader = downloader;
    }

    public void detach() {
        this.mDownloader = null;
    }

    public void start(PluginDownloader downloader) {
        if (downloader == null) {
            throw new IllegalStateException("downloader can not be null");
        } else {
            this.attach(downloader);
            downloader.start();
        }
    }

    public void stop() {
        PluginDownloader downloader = this.mDownloader;
        if (downloader != null) {
            downloader.cancel();
        }
    }
}
