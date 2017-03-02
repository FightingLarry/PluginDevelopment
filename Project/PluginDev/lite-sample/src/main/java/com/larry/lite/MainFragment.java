package com.larry.lite;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.larry.light.LightRecycleViewFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by larry on 2017/3/2.
 */

public class MainFragment extends LightRecycleViewFragment {

    private MainAdapter mMainAdapter;

    @Override
    protected MainAdapter getAdapter() {
        if (mMainAdapter == null) {
            mMainAdapter = new MainAdapter(getActivity());
        }
        return mMainAdapter;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getRecyclerView().setLoadingMoreEnabled(false);
        //getRecyclerView().setPullRefreshEnabled(false);

    }

    @Override
    public int getTitle() {
        return 0;
    }

    @Override
    protected void constructAndPerformRequest(boolean clearOnAdd, boolean readCache, int index) {
        super.constructAndPerformRequest(clearOnAdd, readCache, index);


        List<MainInfo> plugins = new ArrayList<>();
        MainInfo info = new MainInfo();
        info.setInfo("plugin1");
        plugins.add(info);

        info = new MainInfo();
        info.setInfo("plugin2");
        plugins.add(info);


        getAdapter().addItem(plugins);
    }
}
