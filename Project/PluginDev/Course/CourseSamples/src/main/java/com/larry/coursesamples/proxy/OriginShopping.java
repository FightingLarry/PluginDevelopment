package com.larry.coursesamples.proxy;

import android.util.Log;

/**
 * Created by Larry on 2017/4/23.
 */

public class OriginShopping implements IShopping {

    public static final String TAG = "OriginShopping";

    @Override
    public Object[] shopping(long money) {

        Log.i(TAG, "实际花费的钱：" + money);

        return new Object[] {};
    }

    @Override
    public Object getShoppingInfo() {
        return null;
    }

}
