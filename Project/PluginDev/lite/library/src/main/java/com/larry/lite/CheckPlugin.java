package com.larry.lite;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Larry on 2017/3/10.
 */
class CheckPlugin implements ICheckPlugin {

    private List<ICheckPlugin> mCheckPluginList = new ArrayList<>(2);

    void addCheckPlugin(ICheckPlugin checkPlugin) {
        if (checkPlugin != null && !mCheckPluginList.contains(checkPlugin)) {
            mCheckPluginList.add(checkPlugin);
        }
    }

    void clear() {
        mCheckPluginList.clear();
    }

    @Override
    public void check() {
        for (ICheckPlugin c : mCheckPluginList) {
            c.check();
        }
    }

    @Override
    public String getUrl() {
        return null;
    }
}
