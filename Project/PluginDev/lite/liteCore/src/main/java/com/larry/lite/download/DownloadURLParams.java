
package com.larry.lite.download;

import java.io.File;

public class DownloadURLParams<E> {
    String url;
    File target;
    E entity;

    public DownloadURLParams() {}

    public interface URLParamsCreator<T> {
        DownloadURLParams<T> createUrlParams(T var1);
    }
}
