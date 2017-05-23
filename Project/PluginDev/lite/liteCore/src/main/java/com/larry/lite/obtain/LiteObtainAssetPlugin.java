package com.larry.lite.obtain;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.text.TextUtils;

import com.larry.lite.ILiteObtainPlugin;
import com.larry.lite.LiteLog;
import com.larry.lite.LiteContext;
import com.larry.lite.LiteStub;
import com.larry.lite.LitePluginsConfigInfo;
import com.larry.lite.LiteConfigType;
import com.larry.lite.utils.CollectionUtils;
import com.larry.lite.utils.IOUtils;
import com.larry.lite.utils.MD5Util;
import com.larry.lite.utils.Streams;
import com.larry.taskflows.TaskManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Larry on 2017/5/17.
 */

public class LiteObtainAssetPlugin extends LiteObtainSdCardPlugin {
    public static final String ASSERT_PLUGIN_DIR = "plugins";

    public LiteObtainAssetPlugin(LiteContext context) {
        super(context);
    }

    @Override
    protected void checkResult(File dir, LiteObtainRemotePlugin.ConfigurationResult cr) throws Exception {
        if (cr == null) {
            throw new NullPointerException("configuration result is null!!");
        } else if (!CollectionUtils.isEmpty(cr.plugins)) {
            ArrayList<LiteStub> removes = new ArrayList();
            Iterator iterator = cr.plugins.iterator();

            while (iterator.hasNext()) {
                LiteStub stub = (LiteStub) iterator.next();
                if (stub.id <= LOCAL_PLUGIN_ID_BASE) {
                    removes.add(stub);
                    LiteLog.w("plugin %d path empty or md5 empty", Integer.valueOf(stub.id));
                } else if (!TextUtils.isEmpty(stub.path) && !TextUtils.isEmpty(stub.md5)) {
                    String assertPath = String.format("%s%s%s", ASSERT_PLUGIN_DIR, File.separator, stub.path);
                    // TODO
                    File file = new File(new URI(String.format("file:///android_asset/%s", assertPath)));
                    if (file != null && file.length() == stub.size) {
                        String md5 = MD5Util.getFileMD5(file.getAbsolutePath());
                        if (!stub.md5.equalsIgnoreCase(md5)) {
                            removes.add(stub);
                            LiteLog.w("plugin %d md5(%s) not match, calc md5 is %s",
                                    new Object[] {Integer.valueOf(stub.id), stub.md5, md5});
                        } else {
                            stub.path = file.getAbsolutePath();
                            stub.ready = true;
                        }
                        removes.add(stub);
                        LiteLog.w("assert plugin id %d : %s", Integer.valueOf(stub.id),
                                file != null ? "file size error: " + file.length() : "assert file is not exists");
                    }

                } else {
                    removes.add(stub);
                    LiteLog.w("plugin %d path empty or md5 empty", new Object[] {Integer.valueOf(stub.id)});
                }
            }

            if (removes.size() > 0) {
                cr.plugins.removeAll(removes);
            }
            return;
        }
    }

    @Override
    public int obtain(final Callback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback == null!!");
        } else {
            Context context = this.mContext.getApplicationContext();

            try {
                String fileNameList[] = context.getAssets().list(ASSERT_PLUGIN_DIR);
                if (fileNameList != null && fileNameList.length != 0) {
                    String pluginsJson = null;
                    for (String fileName : fileNameList) {
                        if (fileName.equals(CONFIG_MANIFEST_FILE)) {
                            pluginsJson = fileName;
                            break;
                        }
                    }

                    if (!TextUtils.isEmpty(pluginsJson)) {

                        InputStream is = null;
                        String from = ASSERT_PLUGIN_DIR + File.separator + pluginsJson;

                        byte[] contentBytes;
                        try {
                            is = context.getAssets().open(from);
                            contentBytes = IOUtils.readLeftBytes(is);
                        } finally {
                            Streams.safeClose(is);
                        }

                        String content = contentBytes != null && contentBytes.length != 0
                                ? new String(contentBytes, "utf-8")
                                : null;
                        if (TextUtils.isEmpty(content)) {
                            LiteLog.w("manifest file %s content empty.", new Object[] {CONFIG_MANIFEST_FILE});
                            return ILiteObtainPlugin.FAIL_IO;
                        } else {
                            JSONObject jsonObject = new JSONObject(content);
                            final LiteObtainRemotePlugin.ConfigurationResult cr =
                                    LiteObtainRemotePlugin.parseResult(jsonObject);
                            this.checkResult(null, cr);
                            TaskManager.runWorkerThread(new Runnable() {
                                public void run() {
                                    LitePluginsConfigInfo litePluginsConfigInfo = new LitePluginsConfigInfo();
                                    litePluginsConfigInfo.setPlugins(cr.plugins);
                                    litePluginsConfigInfo.setTs(cr.ts);
                                    litePluginsConfigInfo.setType(LiteConfigType.Assert);
                                    callback.onObtainResult(ILiteObtainPlugin.SUCCESS, litePluginsConfigInfo);
                                }
                            });
                            return ILiteObtainPlugin.SUCCESS;
                        }

                    } else {
                        LiteLog.w("plugins.json is not exists.");
                        return ILiteObtainPlugin.ALREADY;
                    }

                } else {
                    LiteLog.w("local plugin dir not exists.", new Object[0]);
                    return ILiteObtainPlugin.ALREADY;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.obtain(callback);

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
