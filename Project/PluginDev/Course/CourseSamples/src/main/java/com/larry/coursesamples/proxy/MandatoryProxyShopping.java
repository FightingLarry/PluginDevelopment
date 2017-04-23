package com.larry.coursesamples.proxy;

import android.util.Log;

/**
 * Created by Larry on 2017/4/23.代理角色
 */

public class MandatoryProxyShopping implements IShopping {

    private static final String TAG = MandatoryProxyShopping.class.getSimpleName();

    double fee = 0.1;

    IShopping shopping;

    public MandatoryProxyShopping(IShopping shopping) {
        this.shopping = shopping;
    }

    @Override
    public Object[] shopping(long money) {

        long realPay = (long) (money * (1 - fee));

        Object[] shoping = shopping.shopping(realPay);

        if (shoping != null) {
            Log.w(TAG, TAG + "花费的钱：" + money + " ,手续费：" + (long) (money * fee));
            return shoping;
        }
        Log.w(TAG, "shopping失败");
        return null;

    }

    @Override
    public Object getShoppingInfo() {

        Object shoppingInfo = shopping.getShoppingInfo();
        if (shoppingInfo != null) {
            return new Object();
        }
        Log.w(TAG, "shopping失败");
        return null;
    }

    @Override
    public IShopping getProxy() {
        return this;
    }
}
