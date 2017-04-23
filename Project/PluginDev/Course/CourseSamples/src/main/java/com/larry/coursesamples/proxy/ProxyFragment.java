package com.larry.coursesamples.proxy;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.larry.light.IAdapterListener;
import com.larry.light.LightRecycleViewFragment;

import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * Created by Larry on 2017/4/23.
 */

public class ProxyFragment extends LightRecycleViewFragment implements IAdapterListener<String> {

    private ProxyAdapter mProxyAdapter;

    private String[] itemArray = new String[] {"购物", "普通代理购物", "动态代理购物"};

    @Override
    protected void readCacheOrExcuteRequest() {

        getAdapter().addItem(Arrays.asList(itemArray));
        getAdapter().notifyDataSetChanged();
    }

    @Override
    public int getTitle() {
        return 0;
    }

    @Override
    protected ProxyAdapter getAdapter() {
        if (mProxyAdapter == null) {
            mProxyAdapter = new ProxyAdapter(getActivity(), this);
        }
        return mProxyAdapter;
    }

    @Override
    public void onItemClick(View view, String s, int position) {
        if (itemArray[0].equals(s)) {

            OriginShopping shopping = new OriginShopping();
            shopping.shopping(7038);

        } else if (itemArray[1].equals(s)) {

            OriginShopping shopping = new OriginShopping();
            ProxyShopping proxyShopping = new ProxyShopping(shopping);
            proxyShopping.shopping(7820);

        } else if (itemArray[2].equals(s)) {

            IShopping shopping = new OriginShopping();

            // 招代理
            shopping =
                    (IShopping) Proxy.newProxyInstance(IShopping.class.getClassLoader(), shopping.getClass()
                            .getInterfaces(), new ShoppingHandler(shopping));
            shopping.shopping(7820);

        }

    }
}
