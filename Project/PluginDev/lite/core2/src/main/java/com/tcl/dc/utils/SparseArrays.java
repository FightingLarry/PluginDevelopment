
package com.tcl.dc.utils;

import android.util.SparseArray;
import java.util.ArrayList;
import java.util.List;

public final class SparseArrays {
    public SparseArrays() {}

    public static <T> List<T> toList(SparseArray<T> array) {
        if (array != null && array.size() != 0) {
            int size = array.size();
            List<T> resultList = new ArrayList(size);

            for (int i = 0; i < size; ++i) {
                T t = array.valueAt(i);
                resultList.add(t);
            }

            return resultList;
        } else {
            return null;
        }
    }

    public static <T> T[] toArray(SparseArray<T> array, Class<T> clazz) {
        if (array != null && array.size() != 0) {
            int size = array.size();
            T[] results = (T[]) ArrayUtils.newArrayByClass(clazz, size);

            for (int i = 0; i < size; ++i) {
                T t = array.valueAt(i);
                results[i] = t;
            }

            return results;
        } else {
            return null;
        }
    }
}
