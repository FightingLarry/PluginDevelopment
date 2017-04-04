package com.larry.coursesamples.classloader;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.larry.coursesamples.R;
import com.larry.light.LightFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * Created by Larry on 2017/4/3.
 */

public class DexClassLoaderFragment extends LightFragment {

    private String mPath;
    private String mOptimizedDirectory;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            mPath = getDexPath(getActivity());

            String fileName = "classes.dex";

            String path = "plugins";
            String fileList[] = getActivity().getAssets().list(path);
            if (fileList.length > 0) {// 如果是目录


                String from = path + File.separator + fileName;
                String to = getDexPath(getActivity());
                File toFile = new File(to);
                if (toFile != null && !toFile.exists() && !toFile.isDirectory()) {
                    toFile.mkdirs();
                }
                File toFileName = new File(toFile, fileName);

                if (!toFileName.exists()) {
                    copyFile(getActivity(), from, to, fileName);
                }
            }


            mOptimizedDirectory = getActivity().getCacheDir().getAbsolutePath() + File.separator + "plugins";
            File optimizedDirectoryFile = new File(mOptimizedDirectory);
            if (!optimizedDirectoryFile.exists()) {
                optimizedDirectoryFile.mkdirs();
            }

            String dex = mPath + File.pathSeparator + fileName;
            DexClassLoader dexClassLoader =
                    new DexClassLoader(dex, mOptimizedDirectory, null, this.getClass().getClassLoader());



            Class<?> cls = dexClassLoader.loadClass("com.larry.lite.plugin.MyPlugin");
            Object instance = cls.newInstance();
            Method method = cls.getMethod("onCreated");
            int result = (int) method.invoke(instance);
            System.out.print("result =" + result);


            // Class<?> cls = dexClassLoader.loadClass("com.larry.course.lib.LarryLib");
            // Object larryLibInstance = cls.newInstance();
            //
            // Class[] params = new Class[2];
            // params[0] = int.class;
            // params[1] = int.class;
            // Method method = cls.getMethod("add", params);
            //
            // int result = (int) method.invoke(larryLibInstance, 12, 36);
            //
            // System.out.print("result =" + result);


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

    @Override
    public int getTitle() {
        return 0;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

}
