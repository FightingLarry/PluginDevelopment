package com.larry.lite.http;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * Created by fanyang.sz on 2016/12/2. 证书信任管理器（用于https请求）
 * 这个证书管理器的作用就是让它信任我们指定的证书，下面的代码意味着信任所有证书，不管是否权威机构颁发。
 */

public class MyX509TrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {


    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {


    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {

        return null;
    }
}
