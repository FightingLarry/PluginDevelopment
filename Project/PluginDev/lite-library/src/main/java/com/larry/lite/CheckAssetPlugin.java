package com.larry.lite;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Larry on 2017/3/6.
 */

public class CheckAssetPlugin implements ICheckPlugin {

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
                    String from = path + File.separator + fileName;
                    String to = mContext.getFilesDir().getAbsolutePath() + File.separator + ".plugins";
                    File toFile = new File(to);
                    if (toFile != null && !toFile.exists() && !toFile.isDirectory()) {
                        toFile.mkdirs();
                    }

                    File toFileName = new File(toFile, fileName);

                    if (!toFileName.exists()) {
                        copyFile(mContext, from, to, fileName);
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
