package com.larry.coursesamples.proxy;

import android.util.Log;

/**
 * Created by Larry on 2017/4/23.
 */

public class VirtualProxyShopping implements IShopping {

    private static final String TAG = "VirtualProxyShopping";

    double fee = 0.1;

    IShopping shopping;

    public VirtualProxyShopping() {

        if (shopping == null) {
            shopping = new OriginShopping();
        }

    }

    @Override
    public Object[] shopping(long money) {

        if (shopping == null) {
            shopping = new OriginShopping();
        }


        long realPay = (long) (money * (1 - fee));

        Object[] shoping = shopping.shopping(realPay);

        Log.w(TAG, "ProxyShopping花费的钱：" + money + " ,手续费：" + (long) (money * fee));

        return shoping;
    }

    @Override
    public Object getShoppingInfo() {

        if (shopping == null) {
            shopping = new OriginShopping();
        }

        return shopping.getShoppingInfo();
    }

    @Override
    public IShopping getProxy() {
        return null;
    }
}
