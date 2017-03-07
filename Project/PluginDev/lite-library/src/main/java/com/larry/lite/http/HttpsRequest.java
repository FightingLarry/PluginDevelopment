package com.larry.lite.http;


import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;


public class HttpsRequest {

    private static final String TAG = "HttpsRequest";

    public static HttpsResponse httpsRequest(String requestUrl, String requestMethod, Map<String, String> params) {

        HttpsResponse response = new HttpsResponse();

        if (TextUtils.isEmpty(requestUrl)) {
            response.setCode(HttpsResponse.FAIL);
            response.setMsg("url is empty");
            return response;
        }

        HttpURLConnection httpUrlConn = null;
        StringBuilder buffer = new StringBuilder();
        OutputStream outputStream = null;

        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;

        try {

            // 打开连接
            URL url = new URL(requestUrl);

            if (requestUrl.startsWith("https")) {
                // 创建SSLContext对象，并使用我们指定的信任管理器初始化
                TrustManager[] tm = {new MyX509TrustManager()};
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, tm, new java.security.SecureRandom());

                // 从上述SSLContext对象中得到SSLSocketFactory对象
                SSLSocketFactory ssf = sslContext.getSocketFactory();
                httpUrlConn = (HttpsURLConnection) url.openConnection();
                ((HttpsURLConnection) httpUrlConn).setSSLSocketFactory(ssf);
                ((HttpsURLConnection) httpUrlConn).setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String arg0, SSLSession arg1) {
                        return true;
                    }
                });

            } else {
                httpUrlConn = (HttpURLConnection) url.openConnection();
            }

            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);
            httpUrlConn.setConnectTimeout(30 * 1000);
            httpUrlConn.setReadTimeout(30 * 1000);

            // 设置请求方式（GET/POST）
            httpUrlConn.setRequestMethod(requestMethod);

            if ("GET".equalsIgnoreCase(requestMethod)) {
                httpUrlConn.connect();
            }

            // 当有数据需要提交时
            if (params != null) {
                String outputStr = HttpUtil.encodeToURLParams(params);
                outputStream = httpUrlConn.getOutputStream();
                // 注意编码格式，防止中文乱码
                outputStream.write(outputStr.getBytes("UTF-8"));
                // outputStream.close()
            }

            if (httpUrlConn.getResponseCode() != HttpURLConnection.HTTP_OK) throw new Exception();

            // 将返回的输入流转换成字符串
            inputStream = httpUrlConn.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            response.setCode(HttpsResponse.SUCCESS);
            response.setMsg(buffer.toString());

        } catch (ConnectException ce) {
            ce.printStackTrace();
            response.setCode(HttpsResponse.TIMEOUT);
            response.setMsg(ce.getMessage());
        } catch (IOException ie) {
            ie.printStackTrace();
            response.setCode(HttpsResponse.IOFAIL);
            response.setMsg(ie.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.setCode(HttpsResponse.FAIL);
            response.setMsg(e.getMessage());
        } finally {

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (httpUrlConn != null) {
                httpUrlConn.disconnect();
            }
        }
        return response;
    }


}
