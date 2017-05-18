package com.tcl.lite.base;

import com.squareup.okhttp.OkHttpClient;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

public interface ConnectionFactory {

    HttpURLConnection openConnection(LitePlugin var1, String var2) throws MalformedURLException;

    OkHttpClient getOkHttpClient();
}
