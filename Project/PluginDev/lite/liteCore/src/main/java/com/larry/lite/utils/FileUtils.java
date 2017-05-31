package com.larry.lite.utils;

import android.content.Context;
import android.os.Build.VERSION;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileUtils {
    public static final String TEMP_SUFFIX = ".wmp";
    public static final String PLUGINS_PATH = "/plugins";

    public FileUtils() {}

    public static File getTempFile(File file) {
        String path = file.getAbsolutePath() + ".wmp";
        return new File(path);
    }

    public static File getCacheFile(Context context, String path) {
        File dir = context.getDir("cache", 0);
        return new File(dir, path);
    }

    public static boolean hasWriteExternalPermission(Context context) {
        if (VERSION.SDK_INT < 23) {
            return true;
        } else {
            int hasPermission = context.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE");
            return hasPermission == 0;
        }
    }

    public static File getPluginsFile(Context context, String filename) {
        return getCacheFile(context, "/plugins/" + filename);
    }

    public static byte[] readContent(File file) throws IOException {
        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE) {
            throw new IOException("file too big...");
        } else {
            FileInputStream fi = null;

            byte[] var4;
            try {
                fi = new FileInputStream(file);
                var4 = IOUtils.readLeftBytes(fi);
            } finally {
                Streams.safeClose(fi);
            }

            return var4;
        }
    }

    public static String readString(File file) throws IOException {
        byte[] bytes = readContent(file);
        return bytes != null && bytes.length != 0 ? new String(bytes, "utf-8") : null;
    }

    public static boolean exists(String path) {
        File file = new File(path);
        return file.exists();
    }

    public static boolean create(File file) throws IOException {
        if (file.exists()) {
            return true;
        } else {
            File parent = file.getParentFile();
            parent.mkdirs();
            return file.createNewFile();
        }
    }

    public static boolean deleteQuietly(File file) {
        if (!file.exists()) {
            return true;
        } else {
            if (file.isDirectory()) {
                File[] subFiles = file.listFiles();

                for (int i = 0; i < subFiles.length; ++i) {
                    if (!deleteQuietly(subFiles[i])) {
                        return false;
                    }
                }
            }

            return file.delete();
        }
    }
}
