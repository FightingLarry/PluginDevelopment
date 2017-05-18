package com.larry.lite.host;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.larry.lite.PluginContext;
import com.larry.lite.PluginModule;
import com.larry.lite.PluginService;
import com.larry.lite.utils.AndroidUtil;

/**
 * Created by yancai.liu on 2016/12/1.
 */

public class TbksPluginModule implements PluginModule {

    @Override
    public void registerComponents(PluginService pluginService) {
        // 注入网络感应器，最好与应用能统一状态
        pluginService.registerComponent("network", new TbksNetWorkSensor());

        // 注入日志打印器
        pluginService.registerComponent("logger", new TbksLogger());

        // 注入统计日志上传接口
        pluginService.registerComponent("uploader", new TbksUploader());
    }

    public void applyOptions(PluginService service, PluginContext context) {

        String url = BuildConfig.DEBUG
                ? "http://tracker-test.tclclouds.com/tracker-api/plugins-info"
                : "http://tracker-global.tclclouds.com/tracker-api/plugins-info";

        context.setPluginConfigUrl(url);
        String useragent = context.getApplicationContext().getPackageName() + AndroidUtil.getVersionName(service);
        context.setUserAgent(useragent);

        String channel = parseChannelFromManifest(context.getApplicationContext());
        if (!TextUtils.isEmpty(channel)) {
            context.setChannel(channel);
        }

        // Map<String, String> params = new HashMap<>();
        // Map<String, Object> data =
        // RequestParam.requestCommonParams(context.getApplicationContext());
        // String dataJson = JSON.toJSONString(data);
        // params.put("data", new StrCryptor().encodeStringAndCompress(dataJson));
        // params.put("e", "v3");
        // context.setNetworkCommonParams(params);
    }



    protected String parseChannelFromManifest(Context context) {
        return getMetaData(context, "CHANNEL");
    }

    public static String getMetaData(Context context, String name) {
        PackageManager packageManager = context.getPackageManager();

        Object value = null;
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 128);
            if ((applicationInfo != null) && (applicationInfo.metaData != null))
                value = applicationInfo.metaData.get(name);
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
        return value == null ? "" : value.toString();
    }

}
