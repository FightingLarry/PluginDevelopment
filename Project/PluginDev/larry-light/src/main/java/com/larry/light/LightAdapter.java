package com.larry.light;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class LightAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected List<T> mList;

    protected Context mContext;

    protected LightAdapter(Context context) {
        this.mContext = context;
        mList = new ArrayList<>();
    }

    public T getItem(int position) {
        return mList.get(position);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void clearItem() {
        mList.clear();
    }

    public void addItem(T t) {
        mList.add(t);
    }

    public void addItem(List<T> list) {
        mList.addAll(list);
    }

    public T removeItem(int position) {
        return mList.remove(position);
    }

    public void addItem(int postion, T t) {
        mList.add(postion, t);
    }

    public List<T> getList() {
        return mList;
    }

    public void removeAll(List<T> list) {
        mList.removeAll(list);
    }

}
