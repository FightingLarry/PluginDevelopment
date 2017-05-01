package com.larry.lite.module;

/**
 * Created by Larry on 2017/5/1.
 */

public class PluginInfo {

    private String id;

    private String plugin;

    private String url;

    /**
     * bytes
     */
    private long size;

    private String md5;

    private int mode;

    private String modePram;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getModePram() {
        return modePram;
    }

    public void setModePram(String modePram) {
        this.modePram = modePram;
    }
}
