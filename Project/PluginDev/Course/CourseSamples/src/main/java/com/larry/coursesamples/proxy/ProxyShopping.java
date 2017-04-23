package com.larry.coursesamples.proxy;

import android.util.Log;

/**
 * Created by Larry on 2017/4/23.
 */

public class ProxyShopping implements IShopping {

    private static final String TAG = "ProxyShopping";
    double fee = 0.1;

    IShopping shopping;

    ProxyShopping(IShopping shopping) {
        this.shopping = shopping;
    }

    @Override
    public Object[] shopping(long money) {

        long realPay = (long) (money * (1 - fee));

        Object[] shoping = shopping.shopping(realPay);

        Log.w(TAG, "ProxyShopping花费的钱：" + money + " ,手续费：" + (long) (money * fee));

        return shoping;
    }

    @Override
    public Object getShoppingInfo() {
        return null;
    }
}
