package com.larry.lite.obtain;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;

import com.larry.lite.ILiteObtainPlugin;
import com.larry.lite.LiteLog;
import com.larry.lite.LiteConfiguration;
import com.larry.lite.LiteContext;
import com.larry.lite.LiteStub;
import com.larry.lite.LitePluginsConfigInfo;
import com.larry.lite.LiteConfigType;
import com.larry.lite.base.LiteConnectionFactory;
import com.larry.lite.base.LiteLaunch;
import com.larry.lite.base.LiteStrategy;
import com.larry.lite.base.LiteNetworkType;
import com.larry.lite.network.NetworkError;
import com.larry.lite.network.NetworkHelper;
import com.larry.lite.utils.AndroidUtil;
import com.larry.lite.utils.TelephonyManagerUtil;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public class LiteObtainRemotePlugin implements ILiteObtainPlugin {
    final LiteContext mContext;
    final OkHttpClient mClient;
    Call mCall;

    public LiteObtainRemotePlugin(LiteContext context, LiteConnectionFactory liteConnectionFactory) {
        this.mContext = context;
        this.mClient = liteConnectionFactory.getOkHttpClient();
    }

    public int obtain(Callback callback) {
        if (!NetworkHelper.sharedHelper().isNetworkAvailable()) {
            LiteLog.w("network not available!", new Object[0]);
            return -11;
        } else {
            String url = this.mContext.getPluginConfigUrl();
            if (TextUtils.isEmpty(url)) {
                throw new RuntimeException("what!!! no plugin config url!!!");
            } else {
                Call call = this.requestPlugins(url, this.createRequestBody());
                call.enqueue(new LiteObtainRemotePlugin.ConfigurationCallBack(
                        new LiteObtainRemotePlugin.WrapCallback(callback)));
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
        LiteLog.v("requestPlugins url: %s", new Object[] {url});
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
        LiteConfiguration old = this.mContext.getConfiguration();
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

    private static LiteStrategy createLaunchStrategy(int limit, String launch, String launchParam, String network) {
        LiteLaunch liteLaunch = LiteLaunch.valueOf(launch);
        LiteNetworkType liteNetworkType = LiteNetworkType.valueOf(network);
        int modeExtra = parseLaunchParam(liteLaunch, launchParam);
        return LiteStrategy.newBuilder().setLimit(limit).setMode(liteLaunch).setNetworkLimit(liteNetworkType)
                .setModeExtra(modeExtra).build();
    }

    private static int parseLaunchParam(LiteLaunch mode, String launchParam) {
        int modeExtra;
        if (mode.equals(LiteLaunch.Periodicity)) {
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
            if (!mode.equals(LiteLaunch.KeyEvent)) {
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

    static LiteObtainRemotePlugin.ConfigurationResult parseResult(JSONObject json) throws JSONException {
        LiteObtainRemotePlugin.ConfigurationResult result = new LiteObtainRemotePlugin.ConfigurationResult();
        if (json.has("status") && json.has("msg")) {
            int status = json.getInt("status");
            String msg = json.optString("msg");
            LiteLog.i("status: %d, msg: %s", new Object[] {Integer.valueOf(status), msg});
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
            List<LiteStub> liteStubs = new ArrayList(length);

            for (int i = 0; i < length; ++i) {
                LiteStub liteStub = new LiteStub();
                JSONObject object = plugins.getJSONObject(i);
                liteStub.id = Integer.parseInt(object.getString("id"));
                liteStub.url = object.optString("url");
                liteStub.size = object.getLong("size");
                liteStub.md5 = object.getString("md5");
                liteStub.name = object.optString("name");
                liteStub.desc = object.optString("description");
                liteStub.path = object.optString("path");
                int limit = object.optInt("limit", 0);
                String launch = object.getString("launch");
                String launchParam = object.getString("launchParam");
                String network = object.optString("network", "WIFI");
                liteStub.strategy = createLaunchStrategy(limit, launch, launchParam, network);
                liteStubs.add(liteStub);
            }

            result.plugins = liteStubs;
        }

        return result;
    }

    private class WrapCallback implements Callback {
        private WeakReference<Callback> reference;

        public WrapCallback(Callback c) {
            this.reference = new WeakReference(c);
        }

        public void onObtainResult(int err, LitePluginsConfigInfo litePluginsConfigInfo) {
            LiteObtainRemotePlugin.this.mCall = null;
            Callback call = (Callback) this.reference.get();
            if (call != null) {
                call.onObtainResult(err, litePluginsConfigInfo);
            }

        }
    }

    private static class ConfigurationCallBack implements com.squareup.okhttp.Callback {
        private final Callback mCallback;

        ConfigurationCallBack(Callback callback) {
            this.mCallback = callback;
        }

        public void onFailure(Request request, IOException e) {
            LiteLog.v("plugin onFailure", new Object[0]);
            LiteLog.printStackTrace(e);
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
            LiteLog.i("plugin config onResponse %s", new Object[] {result});
            if (!TextUtils.isEmpty(result)) {
                try {
                    this.parse(result);
                } catch (Exception var6) {
                    this.onResultStatus(NetworkError.FAIL_IO_ERROR, (List) null, 0L);
                }
            } else {
                this.onResultStatus(NetworkError.FAIL_IO_ERROR, (List) null, 0L);
            }

        }

        private void parse(String result) throws JSONException {
            JSONObject jsonObject = new JSONObject(result);
            LiteObtainRemotePlugin.ConfigurationResult cr = LiteObtainRemotePlugin.parseResult(jsonObject);
            if (cr.status == 0) {
                this.onResultStatus(NetworkError.SUCCESS, cr.plugins, cr.ts);
            } else if (cr.status == -1) {
                this.onResultStatus(ILiteObtainPlugin.ERR_CONFIG_NO_CHANGED, (List) null, 0L);
            } else {
                this.onResultStatus(NetworkError.FAIL_IO_ERROR, (List) null, 0L);
            }

        }

        private void onResultStatus(int err, List<LiteStub> plugins, long timestamp) {
            LiteLog.i("callBack result %d", new Object[] {Integer.valueOf(err)});
            if (this.mCallback != null) {

                LitePluginsConfigInfo litePluginsConfigInfo = new LitePluginsConfigInfo();
                litePluginsConfigInfo.setPlugins(plugins);
                litePluginsConfigInfo.setTs(timestamp);
                litePluginsConfigInfo.setType(LiteConfigType.Remote);

                this.mCallback.onObtainResult(err, litePluginsConfigInfo);
            }

        }
    }

    static class ConfigurationResult {
        public int status;
        public String cause;
        public List<LiteStub> plugins;
        public long ts;

        ConfigurationResult() {}
    }
}
