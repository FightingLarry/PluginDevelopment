package com.larry.coursesamples;


import android.view.View;

import com.larry.coursesamples.classloader.BootClassLoaderFragment;
import com.larry.coursesamples.classloader.DexClassLoaderFragment;
import com.larry.coursesamples.proxy.ProxyFragment;
import com.larry.coursesamples.ref.RefFragment;
import com.larry.light.IAdapterListener;
import com.larry.light.LightFragmentUtils;
import com.larry.light.LightRecycleViewFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Larry on 2017/4/2.
 */

public class MainFragment extends LightRecycleViewFragment implements IAdapterListener<MainInfo> {

    private MainAdapter mMainAdapter;


    @Override
    protected void constructAndPerformRequest(boolean clearOnAdd, boolean readCache, int index) {
        super.constructAndPerformRequest(clearOnAdd, readCache, index);

        if (clearOnAdd) {
            getAdapter().clearItem();
        }
        getAdapter().addItem(getDatas());
        getAdapter().notifyDataSetChanged();
    }


    @Override
    public int getTitle() {
        return R.string.app_name;
    }

    @Override
    protected MainAdapter getAdapter() {
        if (mMainAdapter == null) {
            mMainAdapter = new MainAdapter(getActivity());
            mMainAdapter.setAdapterListener(this);
        }
        return mMainAdapter;
    }


    public List<MainInfo> getDatas() {
        List<MainInfo> list = new ArrayList<>();

        MainInfo info = new MainInfo();
        info.setMainItemType(MainItemType.Line);
        info.setContext("一、插件化开发概述");
        list.add(info);

        info = new MainInfo();
        info.setMainItemType(MainItemType.Item);
        info.setContext("BootClassLoader");
        list.add(info);

        info = new MainInfo();
        info.setMainItemType(MainItemType.Item);
        info.setContext("ClassLoader机制");
        list.add(info);

        info = new MainInfo();
        info.setMainItemType(MainItemType.Item);
        info.setContext("Java反射机制");
        list.add(info);

        info = new MainInfo();
        info.setMainItemType(MainItemType.Item);
        info.setContext("Java代理模式");
        list.add(info);

        info = new MainInfo();
        info.setMainItemType(MainItemType.Line);
        info.setContext("二、轻量级插件框架");
        list.add(info);

        info = new MainInfo();
        info.setMainItemType(MainItemType.Line);
        info.setContext("三、代理插件框架");
        list.add(info);

        info = new MainInfo();
        info.setMainItemType(MainItemType.Line);
        info.setContext("四、Hook机制框架");
        list.add(info);

        return list;
    }

    @Override
    public void onItemClick(View view, MainInfo mainInfo, int position) {
        if (position == 1) {
            LightFragmentUtils.navigateToInNewActivity(getActivity(), BootClassLoaderFragment.class, null, view);
        } else if (position == 2) {
            LightFragmentUtils.navigateToInNewActivity(getActivity(), DexClassLoaderFragment.class, null, view);
        } else if (position == 3) {
            LightFragmentUtils.navigateToInNewActivity(getActivity(), RefFragment.class, null, view);
        } else if (position == 4) {
            LightFragmentUtils.navigateToInNewActivity(getActivity(), ProxyFragment.class, null, view);

        }
    }
}
