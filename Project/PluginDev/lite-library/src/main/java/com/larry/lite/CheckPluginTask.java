package com.larry.lite;

import android.content.Context;

import com.larry.taskflows.Task;

/**
 * Created by Larry on 2017/3/7.
 */

public class CheckPluginTask extends Task {

    private Context mContext;

    public CheckPluginTask(Context context) {
        this.mContext = context;
    }

    @Override
    protected void onExecute() throws Exception {

        new CheckAssetPlugin(mContext).check();
        new CheckNetPlugin(mContext).check();

    }


}
