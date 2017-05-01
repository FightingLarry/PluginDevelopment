package com.larry.lite.utils;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileUtils {
    public static final String TEMP_SUFFIX = ".wmp";
    public static final String PLUGINS_PATH = "/plugins";

    public static File getTempFile(File file) {
        String path = file.getAbsolutePath() + ".wmp";
        return new File(path);
    }

    public static File getCacheFile(Context context, String path) {
        File dir = context.getDir("cache", 0);
        return new File(dir, path);
    }

    public static File getPluginsFile(Context context, String filename) {
        return getCacheFile(context, "/plugins/" + filename);
    }

    public static byte[] readContent(File file) throws IOException {
        long fileSize = file.length();
        if (fileSize > 2147483647L) {
            throw new IOException("file too big...");
        }

        FileInputStream fi = null;
        try {
            fi = new FileInputStream(file);
            byte[] arrayOfByte = IOUtils.readLeftBytes(fi);
            return arrayOfByte;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Streams.safeClose(fi);
        }
        return null;
    }

    public static String readString(File file) throws IOException {
        byte[] bytes = readContent(file);
        if ((bytes == null) || (bytes.length == 0)) return null;
        return new String(bytes, "utf-8");
    }

    public static boolean exists(String path) {
        File file = new File(path);
        return file.exists();
    }

    public static boolean create(File file) throws IOException {
        if (file.exists()) {
            return true;
        }

        File parent = file.getParentFile();
        parent.mkdirs();
        return file.createNewFile();
    }

    public static boolean deleteQuietly(File file) {
        if (!file.exists()) {
            return true;
        }
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            for (int i = 0; i < subFiles.length; i++) {
                if (!deleteQuietly(subFiles[i])) {
                    return false;
                }
            }
        }

        return file.delete();
    }
}
