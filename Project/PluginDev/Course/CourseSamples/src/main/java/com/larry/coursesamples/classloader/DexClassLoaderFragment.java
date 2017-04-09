package com.larry.coursesamples.classloader;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.text.TextUtilsCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

    public static final String TAG = "DexClassLoaderFragment";
    public static final String FILENAME = "plugin.dex";

    private String mPath;
    private String mOptimizedDirectory;

    private Button btnEquals;
    private EditText etA;
    private EditText etB;
    private TextView tvResult;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            mPath = getDexPath(getActivity());

            String assetPath = "plugins";
            String fileList[] = getActivity().getAssets().list(assetPath);
            if (fileList.length > 0) {// 如果是目录

                String from = assetPath + File.separator + FILENAME;
                String to = mPath;
                File toFile = new File(to);
                if (toFile != null && !toFile.exists() && !toFile.isDirectory()) {
                    toFile.mkdirs();
                }
                File toFileName = new File(toFile, FILENAME);

                Log.d(TAG, "toFileName=" + toFileName.getAbsolutePath());

                if (!toFileName.exists()) {
                    copyFile(getActivity(), from, to, FILENAME);
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);


        etA = (EditText) view.findViewById(R.id.etA);
        etB = (EditText) view.findViewById(R.id.etB);
        tvResult = (TextView) view.findViewById(R.id.tvResult);

        btnEquals = (Button) view.findViewById(R.id.btnEquals);
        btnEquals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String sA = etA.getText().toString();
                if (TextUtils.isEmpty(sA)) {
                    return;
                }

                String sB = etB.getText().toString();
                if (TextUtils.isEmpty(sB)) {
                    return;
                }

                int a;
                int b;

                try {
                    a = Integer.parseInt(sA);
                    b = Integer.parseInt(sB);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return;
                }


                try {

                    mOptimizedDirectory = ContextCompat.getCodeCacheDir(getActivity()) + File.separator + "plugins";
                    File optimizedDirectoryFile = new File(mOptimizedDirectory);
                    if (!optimizedDirectoryFile.exists()) {
                        optimizedDirectoryFile.mkdirs();
                    }

                    String dex = mPath + File.separator + FILENAME;

                    Log.i(TAG, "dex=" + dex);
                    Log.w(TAG, "opf=" + mOptimizedDirectory);

                    DexClassLoader dexClassLoader =
                            new DexClassLoader(dex, mOptimizedDirectory, null, this.getClass().getClassLoader());

                    Class<?> cls = dexClassLoader.loadClass("com.larry.course.lib.LarryLib");
                    Object larryLibInstance = cls.newInstance();

                    Class[] params = new Class[2];
                    params[0] = int.class;
                    params[1] = int.class;
                    Method method = cls.getMethod("add", params);

                    int result = (int) method.invoke(larryLibInstance, a, b);
                    Log.w(TAG, "result =" + result);

                    tvResult.setText(String.valueOf(result));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return view;
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
        return R.layout.fragment_classloader;
    }

}
