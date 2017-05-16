
package com.tcl.dc.internal;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.squareup.okhttp.Request.Builder;
import com.tcl.dc.ConfigurationCrawler;
import com.tcl.dc.PLog;
import com.tcl.dc.PluginConfiguration;
import com.tcl.dc.PluginContext;
import com.tcl.dc.PluginStub;
import com.tcl.dc.ConfigurationCrawler.Callback;
import com.tcl.dc.base.ConnectionFactory;
import com.tcl.dc.base.LaunchMode;
import com.tcl.dc.base.LaunchStrategy;
import com.tcl.dc.base.NetworkType;
import com.tcl.dc.network.NetworkHelper;
import com.tcl.dc.utils.AndroidUtil;
import com.tcl.dc.utils.TelephonyManagerUtil;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RemoteConfigurationCrawler implements ConfigurationCrawler {
    final PluginContext mContext;
    final OkHttpClient mClient;
    Call mCall;

    public RemoteConfigurationCrawler(PluginContext context, ConnectionFactory connectionFactory) {
        this.mContext = context;
        this.mClient = connectionFactory.getOkHttpClient();
    }

    public int crawlConfiguration(Callback callback) {
        if (!NetworkHelper.sharedHelper().isNetworkAvailable()) {
            PLog.w("network not available!", new Object[0]);
            return -11;
        } else {
            String url = this.mContext.getPluginConfigUrl();
            if (TextUtils.isEmpty(url)) {
                throw new RuntimeException("what!!! no plugin config url!!!");
            } else {
                Call call = this.requestPlugins(url, this.createRequestBody());
                call.enqueue(new RemoteConfigurationCrawler.ConfigurationCallBack(
                        new RemoteConfigurationCrawler.WrapCallback(callback)));
                this.mCall = call;
                return 0;
            }
        }
    }

    public void cancel() {
        Call call = this.mCall;
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }

    }

    private Call requestPlugins(String url, RequestBody body) {
        PLog.v("requestPlugins url: %s", new Object[] {url});
        Builder requestBuilder = new Builder();
        String ua = this.mContext.getUserAgent();
        if (!TextUtils.isEmpty(ua)) {
            requestBuilder.header("User-Agent", ua);
        }

        requestBuilder.url(url).post(body);
        Request request = requestBuilder.build();
        return this.mClient.newCall(request);
    }

    private RequestBody createRequestBody() {
        Context context = this.mContext.getApplicationContext();
        int vc = AndroidUtil.getVersionCode(context);
        String vn = AndroidUtil.getVersionName(context);
        String model = Build.MODEL;
        PluginConfiguration old = this.mContext.getConfiguration();
        long ts = 0L;
        if (old != null) {
            ts = old.getTs();
        }

        FormEncodingBuilder builder = new FormEncodingBuilder();
        builder.add("from", context.getPackageName());
        builder.add("vc", String.valueOf(vc));
        builder.add("vn", vn);
        builder.add("model", model);
        builder.add("ts", String.valueOf(ts));
        String channel = this.mContext.getChannel();
        if (!TextUtils.isEmpty(channel)) {
            builder.add("channel", channel);
        }

        String net = AndroidUtil.getNetworkTypeName(context);
        builder.add("net", net);
        builder.add("os_vn", VERSION.RELEASE);
        builder.add("os_vc", String.valueOf(VERSION.SDK_INT));
        WindowManager wm = (WindowManager) context.getSystemService("window");
        if (wm != null) {
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            builder.add("sc",
                    String.format(Locale.US, "%d#%d", new Object[] {Integer.valueOf(size.x), Integer.valueOf(size.y)}));
        }

        Configuration cf = context.getResources().getConfiguration();
        Locale locale = cf.locale;
        builder.add("lg", locale.toString());
        String si = TelephonyManagerUtil.getSubscriberId(context);
        builder.add("si", si == null ? "" : si);
        String ei = TelephonyManagerUtil.getDeviceId(context);
        if (!TelephonyManagerUtil.isIMEI(ei)) {
            ei = "";
        }

        builder.add("ei", ei);
        Map<String, String> params = this.mContext.getNetworkCommonParams();
        if (params != null) {
            Iterator var17 = params.entrySet().iterator();

            while (var17.hasNext()) {
                Entry<String, String> entry = (Entry) var17.next();
                builder.add((String) entry.getKey(), (String) entry.getValue());
            }
        }

        return builder.build();
    }

    private static LaunchStrategy createLaunchStrategy(int limit, String launch, String launchParam, String network) {
        LaunchMode launchMode = LaunchMode.valueOf(launch);
        NetworkType networkType = NetworkType.valueOf(network);
        int modeExtra = parseLaunchParam(launchMode, launchParam);
        return LaunchStrategy.newBuilder().setLimit(limit).setMode(launchMode).setNetworkLimit(networkType)
                .setModeExtra(modeExtra).build();
    }

    private static int parseLaunchParam(LaunchMode mode, String launchParam) {
        int modeExtra;
        if (mode.equals(LaunchMode.Periodicity)) {
            modeExtra = Integer.parseInt(launchParam);
            switch (modeExtra) {
                case 1:
                    modeExtra = 3600000;
                    break;
                case 12:
                    modeExtra = 43200000;
                    break;
                case 24:
                    modeExtra = 86400000;
                    break;
                case 168:
                    modeExtra = 604800000;
                    break;
                default:
                    throw new RuntimeException("launchParam " + modeExtra + " not match for mode " + mode);
            }
        } else {
            if (!mode.equals(LaunchMode.KeyEvent)) {
                throw new RuntimeException("Unsupported mode " + mode);
            }

            launchParam = launchParam.toLowerCase();
            if (TextUtils.equals("start", launchParam)) {
                modeExtra = 1;
            } else if (TextUtils.equals("background", launchParam)) {
                modeExtra = 3;
            } else {
                if (!TextUtils.equals("upgrade", launchParam)) {
                    throw new RuntimeException("launchParam " + launchParam + " not match for mode " + mode);
                }

                modeExtra = 2;
            }
        }

        return modeExtra;
    }

    static RemoteConfigurationCrawler.ConfigurationResult parseResult(JSONObject json) throws JSONException {
        RemoteConfigurationCrawler.ConfigurationResult result = new RemoteConfigurationCrawler.ConfigurationResult();
        if (json.has("status") && json.has("msg")) {
            int status = json.getInt("status");
            String msg = json.optString("msg");
            PLog.i("status: %d, msg: %s", new Object[] {Integer.valueOf(status), msg});
            result.status = status;
            result.cause = msg;
        }

        if (result.status == 0) {
            JSONObject data;
            if (json.has("data")) {
                data = json.getJSONObject("data");
            } else {
                data = json;
            }

            result.ts = data.optLong("ts", 0L);
            JSONArray plugins = data.getJSONArray("plugins");
            int length = plugins.length();
            List<PluginStub> pluginStubs = new ArrayList(length);

            for (int i = 0; i < length; ++i) {
                PluginStub pluginStub = new PluginStub();
                JSONObject object = plugins.getJSONObject(i);
                pluginStub.id = Integer.parseInt(object.getString("id"));
                pluginStub.url = object.optString("url");
                pluginStub.size = object.getLong("size");
                pluginStub.md5 = object.getString("md5");
                pluginStub.name = object.optString("name");
                pluginStub.desc = object.optString("description");
                pluginStub.path = object.optString("path");
                int limit = object.optInt("limit", 0);
                String launch = object.getString("launch");
                String launchParam = object.getString("launchParam");
                String network = object.optString("network", "WIFI");
                pluginStub.strategy = createLaunchStrategy(limit, launch, launchParam, network);
                pluginStubs.add(pluginStub);
            }

            result.plugins = pluginStubs;
        }

        return result;
    }

    private class WrapCallback implements Callback {
        private WeakReference<Callback> reference;

        public WrapCallback(Callback c) {
            this.reference = new WeakReference(c);
        }

        public void onConfigurationResult(int err, List<PluginStub> plugins, long timestamp) {
            RemoteConfigurationCrawler.this.mCall = null;
            Callback call = (Callback) this.reference.get();
            if (call != null) {
                call.onConfigurationResult(err, plugins, timestamp);
            }

        }
    }

    private static class ConfigurationCallBack implements com.squareup.okhttp.Callback {
        private final Callback mCallback;

        ConfigurationCallBack(Callback callback) {
            this.mCallback = callback;
        }

        public void onFailure(Request request, IOException e) {
            PLog.v("plugin onFailure", new Object[0]);
            PLog.printStackTrace(e);
            int err = -2;
            this.onResultStatus(err, (List) null, 0L);
        }

        public void onResponse(Response response) throws IOException {
            if (!response.isSuccessful()) {
                byte ret;
                if (response.code() == 404) {
                    ret = -3;
                } else {
                    ret = -1;
                }

                this.onResultStatus(ret, (List) null, 0L);
            }

            ResponseBody body = response.body();
            String result = body.string();
            PLog.i("plugin config onResponse %s", new Object[] {result});
            if (!TextUtils.isEmpty(result)) {
                try {
                    this.parse(result);
                } catch (Exception var6) {
                    this.onResultStatus(-4, (List) null, 0L);
                }
            } else {
                this.onResultStatus(-4, (List) null, 0L);
            }

        }

        private void parse(String result) throws JSONException {
            JSONObject jsonObject = new JSONObject(result);
            RemoteConfigurationCrawler.ConfigurationResult cr = RemoteConfigurationCrawler.parseResult(jsonObject);
            if (cr.status == 0) {
                this.onResultStatus(0, cr.plugins, cr.ts);
            } else if (cr.status == -1) {
                this.onResultStatus(4097, (List) null, 0L);
            } else {
                this.onResultStatus(-4, (List) null, 0L);
            }

        }

        private void onResultStatus(int err, List<PluginStub> plugins, long timestamp) {
            PLog.i("callBack result %d", new Object[] {Integer.valueOf(err)});
            if (this.mCallback != null) {
                this.mCallback.onConfigurationResult(err, plugins, timestamp);
            }

        }
    }

    static class ConfigurationResult {
        public int status;
        public String cause;
        public List<PluginStub> plugins;
        public long ts;

        ConfigurationResult() {}
    }
}
