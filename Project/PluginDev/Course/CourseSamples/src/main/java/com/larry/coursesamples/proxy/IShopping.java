package com.larry.coursesamples.proxy;

/**
 * Created by Larry on 2017/4/23.
 */

public interface IShopping {

    Object[] shopping(long money);

    Object getShoppingInfo();

    IShopping getProxy();

}
