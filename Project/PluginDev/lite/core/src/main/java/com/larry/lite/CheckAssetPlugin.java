package com.larry.lite;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.larry.lite.utils.FileUtils;
import com.larry.lite.utils.GZipUtils;
import com.larry.lite.utils.L;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Larry on 2017/3/6.
 */

public class CheckAssetPlugin implements ICheckPlugin {

    public static final String SUFFIX = "lz";
    private static final String TAG = "CheckAssetPlugin";

    private Context mContext;

    public CheckAssetPlugin(Context context) {
        this.mContext = context;
    }

    @Override
    public void check() {

        try {
            String path = getUrl();
            String fileList[] = mContext.getAssets().list(path);
            if (fileList.length > 0) {// 如果是目录
                for (String fileName : fileList) {
                    if (fileName.toLowerCase().endsWith(SUFFIX)) {
                        String from = path + File.separator + fileName;
                        String to = PluginFileUtil.getDexPath(mContext);

                        if (TextUtils.isEmpty(to)) {
                            Log.w(TAG, "to path is empty!!");
                            return;
                        }

                        File toFile = new File(to);
                        if (toFile != null && !toFile.exists()) {
                            toFile.mkdirs();
                        }
                        File toFileName = new File(toFile, fileName);

                        if (!toFileName.exists()) {
                            copyFile(mContext, from, to, fileName);

                            File file = new File(to, fileName);
                            File pluginPath = new File(to, "larry");

                            L.w(TAG,
                                    String.format("to:%s,outFile:%s", file.getAbsolutePath(),
                                            pluginPath.getAbsolutePath()));

                            if (!pluginPath.exists()) {
                                pluginPath.mkdirs();
                            }

                            try {
                                // 解压
                                GZipUtils.unZipFiles(file.getAbsolutePath(), pluginPath.getAbsolutePath());

                                File pluginsJson = new File(pluginPath, "plugins.json");
                                if (!pluginsJson.exists()) {
                                    Log.w(TAG, "plugins.json file  not exists.");
                                    return;
                                }

                                String pluginsJsonString = FileUtils.readString(pluginsJson);
                                if (TextUtils.isEmpty(pluginsJsonString)) {
                                    Log.w(TAG, "File plugins.json string is empty.");
                                    return;
                                }

                                JSONObject jsonObject = new JSONObject(pluginsJsonString);

                                // RemoteConfigurationCrawler.ConfigurationResult cr =
                                // RemoteConfigurationCrawler.parseResult(jsonObject);
                                //
                                // checkResult(dir, cr);


                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    @Override
    public String getUrl() {
        return "plugins";
    }


    private static void copyFile(Context context, String from, String to, String name) {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = context.getAssets().open(from);
            fos = new FileOutputStream(new File(to, name));
            byte[] buffer = new byte[1024];
            int count = 0;
            while (true) {
                count++;
                int len = is.read(buffer);
                if (len == -1) {
                    break;
                }
                fos.write(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
