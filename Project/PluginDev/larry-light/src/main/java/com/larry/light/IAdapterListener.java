package com.larry.light;

import android.view.View;

/**
 * Created by larry on 2016/7/8.
 */

public interface IAdapterListener<T> {

    void onItemClick(View view, T t, int position);

}
