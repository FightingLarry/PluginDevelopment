package com.larry.lite;

import android.content.Context;

import java.io.File;

/**
 * Created by Larry on 2017/3/6.
 */

public class PluginFileUtil {

    public static String getDexPath(Context context) {
        String dexPath = context.getFilesDir().getAbsolutePath() + File.separator + ".plugins";
        File dexPathFile = new File(dexPath);
        if (!dexPathFile.exists()) {
            dexPathFile.mkdirs();
        }
        return dexPath;

    }

    public static String getDexFile(Context context) {
        String dexFile =
                context.getFilesDir().getAbsolutePath() + File.separator + ".plugins" + File.separator + "classes.dex";
        return dexFile;

    }

    public static String getOptimizedDirectory(Context context) {
        String optimizedDirectory = context.getCacheDir().getAbsolutePath() + File.separator + ".plugins";
        File optimizedDirectoryFile = new File(optimizedDirectory);
        if (!optimizedDirectoryFile.exists()) {
            optimizedDirectoryFile.mkdirs();
        }
        return optimizedDirectory;

    }
}
