package com.larry.lite.plugin1;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

/**
 * Created by Larry on 2017/6/18.
 */

public class HttpHelp {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static String post(OkHttpClient client, String url, String json) throws Exception {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder().url(url).post(body).build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
