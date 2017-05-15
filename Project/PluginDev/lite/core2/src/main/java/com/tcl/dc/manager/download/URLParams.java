//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.tcl.dc.manager.download;

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