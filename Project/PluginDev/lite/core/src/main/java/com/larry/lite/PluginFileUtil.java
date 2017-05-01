package com.larry.lite;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import java.io.File;

/**
 * Created by Larry on 2017/3/6.
 */

public class PluginFileUtil {

    public static String getDexPath(Context context) {

        File[] dirs = ContextCompat.getExternalFilesDirs(context, ".plugins");

        if ((dirs == null) || (dirs.length == 0)) {
            return "";
        }
        File dexPathFile = dirs[0];
        if (!dexPathFile.exists()) {
            dexPathFile.mkdirs();
        }
        return dexPathFile.getAbsolutePath();

    }

    public static String getDexFile(Context context) {
        String dexFile = getDexPath(context) + File.separator + "classes.dex";
        return dexFile;

    }

    public static String getOptimizedDirectory(Context context) {
        File cacheDir = ContextCompat.getCodeCacheDir(context);
        if (cacheDir == null) {
            return "";
        }
        String optimizedDirectory = cacheDir.getAbsolutePath() + File.separator + ".plugins";
        File optimizedDirectoryFile = new File(optimizedDirectory);
        if (!optimizedDirectoryFile.exists()) {
            optimizedDirectoryFile.mkdirs();
        }
        return optimizedDirectory;

    }
}
