package com.larry.lite;

import android.content.Context;
import android.util.Log;

import com.larry.taskflows.Task;

import java.io.File;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * Created by Larry on 2017/3/7.
 */

public class PluginTask extends Task {

    public static final String TAG = "PluginTask";

    private Context mContext;

    private String mPath;
    private String mOptimizedDirectory;

    public PluginTask(Context context) {
        this.mContext = context;

        mPath = mContext.getFilesDir().getAbsolutePath() + File.separator + ".plugins";
        mOptimizedDirectory = mContext.getCacheDir().getAbsolutePath() + File.separator + ".plugins";
        File optimizedDirectoryFile = new File(mOptimizedDirectory);
        if (!optimizedDirectoryFile.exists()) {
            optimizedDirectoryFile.mkdirs();
        }
        Log.d(TAG, "mPath=" + mPath);
        Log.d(TAG, "mOptimizedDirectory=" + mOptimizedDirectory);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    protected void onExecute() throws Exception {

        new CheckAssetPlugin(mContext).check();
        new CheckNetPlugin(mContext).check();

        String dex = mPath + "/classes.dex";
        DexClassLoader dexClassLoader =
                new DexClassLoader(dex, mOptimizedDirectory, null, this.getClass().getClassLoader());
        try {

            Class<?> cls = dexClassLoader.loadClass("com.larry.lite.plugin.MyPlugin");

            ILitePlugin iLitePlugin = (ILitePlugin) cls.newInstance();
            iLitePlugin.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
