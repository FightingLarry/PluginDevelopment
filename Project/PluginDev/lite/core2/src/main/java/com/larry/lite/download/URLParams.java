
package com.larry.lite.download;

import java.io.File;

public class URLParams<E> {
    String url;
    File target;
    E entity;

    public URLParams() {}

    public interface URLParamsCreator<T> {
        URLParams<T> createUrlParams(T var1);
    }
}
