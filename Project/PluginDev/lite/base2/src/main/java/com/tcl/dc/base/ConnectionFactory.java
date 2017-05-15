package com.tcl.dc.base;

import com.squareup.okhttp.OkHttpClient;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

public interface ConnectionFactory {
    HttpURLConnection openConnection(DCPlugin var1, String var2) throws MalformedURLException;

    OkHttpClient getOkHttpClient();
}
