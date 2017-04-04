package com.larry.coursesamples.classloader;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.larry.coursesamples.R;
import com.larry.light.LightFragment;

/**
 * Created by Larry on 2017/4/3.
 */

public class BootClassLoaderFragment extends LightFragment {

    private static final String TAG = "BootClassLoaderFragment";

    private TextView tvBoot;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = super.onCreateView(inflater, container, savedInstanceState);
        tvBoot = (TextView) view.findViewById(R.id.tvBoot);

        ClassLoader classLoader = getActivity().getClassLoader();

        StringBuilder text = new StringBuilder();
        if (classLoader != null) {
            Log.i(TAG, "classLoader->" + classLoader.toString());
            text.append("classLoader->").append(classLoader.toString()).append("\n");
            while (classLoader.getParent() != null) {
                classLoader = classLoader.getParent();
                Log.i(TAG, "classLoader->" + classLoader.toString());

                text.append("classLoader->").append(classLoader.toString()).append("\n");
            }
        }

        tvBoot.setText(text.toString());

        return view;
    }

    @Override
    public int getTitle() {
        return 0;
    }


}
