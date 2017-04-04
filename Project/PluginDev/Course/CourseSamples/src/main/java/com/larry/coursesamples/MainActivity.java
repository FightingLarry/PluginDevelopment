package com.larry.coursesamples;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.larry.light.LightFragmentActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class MainActivity extends LightFragmentActivity {


    @Override
    protected void initializeStartingFragment() {
        try {
            loadFragment(new MainFragment(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String mPath;
    private String mOptimizedDirectory;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            mPath = getDexPath(this);

            // String fileName = "classes.dex";
            String fileName = "plugin.jar";

            String assetPath = "plugins";
            String fileList[] = this.getAssets().list(assetPath);
            if (fileList.length > 0) {// 如果是目录


                String from = assetPath + File.separator + fileName;
                String to = getDexPath(this);
                File toFile = new File(to);
                if (toFile != null && !toFile.exists() && !toFile.isDirectory()) {
                    toFile.mkdirs();
                }
                File toFileName = new File(toFile, fileName);

                if (!toFileName.exists()) {
                    copyFile(this, from, to, fileName);
                }
            }


            mOptimizedDirectory = this.getCacheDir().getAbsolutePath() + File.separator + "plugins";
            File optimizedDirectoryFile = new File(mOptimizedDirectory);
            if (!optimizedDirectoryFile.exists()) {
                optimizedDirectoryFile.mkdirs();
            }

            String dex = mPath + File.pathSeparator + fileName;
            DexClassLoader dexClassLoader = new DexClassLoader(dex, mOptimizedDirectory, null, getClassLoader());



            // Class<?> cls = dexClassLoader.loadClass("com.larry.lite.plugin.MyPlugin");
            // Object instance = cls.newInstance();
            // Method method = cls.getMethod("onCreated");
            // int result = (int) method.invoke(instance);
            // System.out.print("result =" + result);


            Class<?> cls = dexClassLoader.loadClass("com.larry.course.lib.LarryLib");
            Object larryLibInstance = cls.newInstance();

            Class[] params = new Class[2];
            params[0] = int.class;
            params[1] = int.class;
            Method method = cls.getMethod("add", params);

            int result = (int) method.invoke(larryLibInstance, 12, 36);

            System.out.print("result =" + result);


        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }


    }



    private String getDexPath(Context context) {
        String dexPath = context.getFilesDir().getAbsolutePath() + File.separator + "plugins";
        File dexPathFile = new File(dexPath);
        if (!dexPathFile.exists()) {
            dexPathFile.mkdirs();
        }
        return dexPath;

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
