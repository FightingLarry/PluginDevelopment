package com.larry.coursesamples.proxy;

import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ShoppingHandler implements InvocationHandler {

    private static final String TAG = "ShoppingHandler";
    // 被代理的原始对象
    Object base;

    double fee = 0.1;


    public ShoppingHandler(Object base) {
        this.base = base;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if ("shopping".equals(method.getName())) {

            long money = (long) args[0];

            // 实际花费的钱
            long realPay = (long) (money * (1 - fee));


            // 帮忙买东西
            Object[] something = (Object[]) method.invoke(base, realPay);

            Log.i(TAG, String.format("ShoppingHandler花费的钱：%s，手续费：%s", money, money * fee));

            if (something != null && something.length > 1) {
                something[0] = "被掉包的东西!!";
            }

            return something;
        }

        if ("getShoppingInfo".equals(method.getName())) {
            Log.i(TAG, "ShoppingHandler:getShoppingInfo");
            return null;
        }

        return null;
    }
}
