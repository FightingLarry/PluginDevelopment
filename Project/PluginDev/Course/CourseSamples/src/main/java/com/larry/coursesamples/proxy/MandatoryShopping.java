package com.larry.coursesamples.proxy;

import android.util.Log;

/**
 * Created by Larry on 2017/4/23.真实角色
 */

public class MandatoryShopping implements IShopping {

    public static final String TAG = MandatoryShopping.class.getSimpleName();

    private IShopping proxy = null;

    @Override
    public Object[] shopping(long money) {

        if (isProxy()) {
            Log.i(TAG, TAG + "实际花费的钱：" + money);
            return new Object[] {};
        }

        Log.w(TAG, "请使用代理访问shopping");
        return null;
    }

    @Override
    public Object getShoppingInfo() {
        if (isProxy()) {
            Log.i(TAG, TAG + "getShoppingInfo");
            return new Object();
        }
        Log.w(TAG, "请使用代理访问getShoppingInfo");
        return null;
    }

    @Override
    public IShopping getProxy() {
        if (proxy == null) {
            proxy = new MandatoryProxyShopping(this);
        }
        return proxy;
    }

    private boolean isProxy() {
        if (this.proxy == null) {
            return false;
        } else {
            return true;
        }
    }

}
