package com.larry.lite.obtain;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.larry.lite.ILiteObtainPlugin;
import com.larry.lite.LiteLog;
import com.larry.lite.LiteContext;
import com.larry.lite.LiteStub;
import com.larry.lite.LitePluginsConfigInfo;
import com.larry.lite.LiteConfigType;
import com.larry.lite.utils.CollectionUtils;
import com.larry.lite.utils.MD5Util;
import com.larry.taskflows.TaskManager;
import com.larry.lite.utils.FileUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class LiteObtainSdCardPlugin implements ILiteObtainPlugin {
    public static final String LOCAL_PLUGIN_DIR = ".plugins";
    public static final String PLUGIN_SUFFIX = ".lite";
    public static final String CONFIG_MANIFEST_FILE = "plugins.json";
    public static final int LOCAL_PLUGIN_ID_BASE = 200000;
    final LiteContext mContext;

    public LiteObtainSdCardPlugin(LiteContext context) {
        this.mContext = context;
    }

    protected void checkResult(File dir, LiteObtainRemotePlugin.ConfigurationResult cr) throws Exception {
        if (cr == null) {
            throw new NullPointerException("configuration result is null!");
        } else if (!CollectionUtils.isEmpty(cr.plugins)) {
            ArrayList<LiteStub> removes = new ArrayList();
            Iterator var4 = cr.plugins.iterator();

            while (var4.hasNext()) {
                LiteStub stub = (LiteStub) var4.next();
                if (stub.id <= LOCAL_PLUGIN_ID_BASE) {
                    removes.add(stub);
                    LiteLog.w("plugin %d path empty or md5 empty", Integer.valueOf(stub.id));
                } else if (!TextUtils.isEmpty(stub.path) && !TextUtils.isEmpty(stub.md5)) {
                    File file = new File(dir, stub.path);
                    if (file.exists() && file.getName().endsWith(PLUGIN_SUFFIX) && file.length() == stub.size) {
                        String md5 = MD5Util.getFileMD5(file.getAbsolutePath());
                        if (!stub.md5.equalsIgnoreCase(md5)) {
                            removes.add(stub);
                            LiteLog.w("plugin %d md5(%s) not match, calc md5 is %s",
                                    new Object[] {Integer.valueOf(stub.id), stub.md5, md5});
                        } else {
                            stub.path = file.getAbsolutePath();
                            stub.ready = true;
                        }
                    } else {
                        removes.add(stub);
                        LiteLog.w("plugin id %d : %s", Integer.valueOf(stub.id),
                                file.exists() ? "file size error: " + file.length() : "file is not exists");
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

    public int obtain(final Callback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback == null!!");
        } else {
            Context context = this.mContext.getApplicationContext();
            File[] dirs = ContextCompat.getExternalFilesDirs(context, LOCAL_PLUGIN_DIR);
            if (dirs != null && dirs.length != 0) {
                File dir = dirs[0];
                File[] files = dir.listFiles(new FileFilter() {
                    public boolean accept(File pathname) {
                        if (pathname.length() <= 100L) {
                            return false;
                        } else {
                            String name = pathname.getName();
                            return name.toLowerCase().endsWith(PLUGIN_SUFFIX);
                        }
                    }
                });
                if (files != null && files.length != 0) {
                    File file = new File(dir, CONFIG_MANIFEST_FILE);
                    if (!file.exists()) {
                        LiteLog.w("local plugins manifest file %s not exists.", CONFIG_MANIFEST_FILE);
                        return ILiteObtainPlugin.ALREADY;
                    } else {
                        try {
                            String s = FileUtils.readString(file);
                            if (TextUtils.isEmpty(s)) {
                                LiteLog.w("manifest file %s content empty.", CONFIG_MANIFEST_FILE);
                                return ILiteObtainPlugin.FAIL_IO;
                            } else {
                                JSONObject jsonObject = new JSONObject(s);
                                final LiteObtainRemotePlugin.ConfigurationResult cr =
                                        LiteObtainRemotePlugin.parseResult(jsonObject);
                                this.checkResult(dir, cr);
                                TaskManager.runWorkerThread(new Runnable() {
                                    public void run() {

                                        LitePluginsConfigInfo litePluginsConfigInfo = new LitePluginsConfigInfo();
                                        litePluginsConfigInfo.setPlugins(cr.plugins);
                                        litePluginsConfigInfo.setTs(cr.ts);
                                        litePluginsConfigInfo.setType(LiteConfigType.SDCard);

                                        callback.onObtainResult(ILiteObtainPlugin.SUCCESS, litePluginsConfigInfo);
                                    }
                                });
                                return ILiteObtainPlugin.SUCCESS;
                            }
                        } catch (JSONException var10) {
                            LiteLog.printStackTrace(var10);
                            return ILiteObtainPlugin.FAIL_IO;
                        } catch (IOException var11) {
                            LiteLog.printStackTrace(var11);
                            return ILiteObtainPlugin.FAIL_IO;
                        } catch (Exception var12) {
                            LiteLog.printStackTrace(var12);
                            return ILiteObtainPlugin.FAIL_IO;
                        }
                    }
                } else {
                    LiteLog.w("none local plugin exists.", new Object[0]);
                    return ILiteObtainPlugin.ALREADY;
                }
            } else {
                LiteLog.w("local plugin dir not exists.", new Object[0]);
                return ILiteObtainPlugin.ALREADY;
            }
        }
    }

    public void cancel() {}
}
