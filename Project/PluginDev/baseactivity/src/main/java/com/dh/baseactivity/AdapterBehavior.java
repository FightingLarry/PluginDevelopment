package com.dh.baseactivity;

import java.util.List;

public interface AdapterBehavior<T> {

    void clearItem();

    void addItem(T t);

    void addItem(List<T> list);

    T removeItem(int position);

    void addItem(int postion, T t);

    List<T> getList();

    void removeAll(List<T> list);

}
