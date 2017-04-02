package com.larry.coursesamples;


import android.view.View;

import com.larry.light.LightRecycleViewFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Larry on 2017/4/2.
 */

public class MainFragment extends LightRecycleViewFragment {

    private MainAdapter mMainAdapter;

    @Override
    protected void initViews(View v) {
        super.initViews(v);

    }


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
        }
        return mMainAdapter;
    }


    public List<MainInfo> getDatas() {
        List<MainInfo> list = new ArrayList<>();

        MainInfo info = new MainInfo();
        info.setContext("ClassLoader机制");
        list.add(info);

        info = new MainInfo();
        info.setContext("Java反射机制");
        list.add(info);

        info = new MainInfo();
        info.setContext("Java代理模式");
        list.add(info);

        return list;
    }
}
