package com.larry.lite.collection;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;
import com.tcl.lite.base.ConnectionFactory;
import com.tcl.lite.base.LitePlugin;
import com.larry.lite.utils.Singleton;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Connections implements ConnectionFactory {
    private static final int CONN_TIMEOUT = 30000;
    private static final int READ_TIMEOUT = 30000;
    private static Singleton<OkHttpClient> SINGLE_CLIENT = new Singleton<OkHttpClient>() {
        protected OkHttpClient create() {
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(CONN_TIMEOUT, TimeUnit.MILLISECONDS);
            client.setReadTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS);
            Connections.supportHttps(client);
            return client;
        }
    };
    private OkUrlFactory mFactory;

    public Connections() {}

    public HttpURLConnection openConnection(LitePlugin plugin, String url) throws MalformedURLException {
        if (this.mFactory == null) {
            this.mFactory = new OkUrlFactory(this.getOkHttpClient());
        }

        return this.mFactory.open(new URL(url));
    }

    public OkHttpClient getOkHttpClient() {
        return SINGLE_CLIENT.get();
    }

    private static void supportHttps(OkHttpClient client) {
        try {
            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            SSLContext e = SSLContext.getInstance("TLS");
            e.init((KeyManager[]) null, new TrustManager[] {tm}, (SecureRandom) null);
            client.setSslSocketFactory(e.getSocketFactory());
        } catch (Exception e) {
            throw new RuntimeException("supportHttps failed", e);
        }
    }
}
