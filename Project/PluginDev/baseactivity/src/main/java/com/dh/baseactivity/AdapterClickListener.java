package com.dh.baseactivity;

import android.view.View;

/**
 * Created by yancai.liu on 2016/7/8.
 */

public interface AdapterClickListener<T> {

    void onItemClick(View view, T t, int position);

}
