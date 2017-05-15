//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.tcl.dc.utils;

import java.lang.reflect.Array;

public class ArrayUtils {
    public ArrayUtils() {}

    public static <T> T[] newArrayByArrayClass(Class<T[]> clazz, int length) {
        return (T[]) ((Object[]) Array.newInstance(clazz.getComponentType(), length));
    }

    public static <T> T[] newArrayByClass(Class<T> clazz, int length) {
        return (T[]) ((Object[]) Array.newInstance(clazz, length));
    }

    public static <T> boolean empty(T[] array) {
        return array == null || array.length == 0;
    }

    public static <T> int length(T[] array) {
        return array == null ? 0 : array.length;
    }

    public static void main(String[] args) {
        String[] byArray = (String[]) newArrayByArrayClass(String[].class, 10);
        String[] byOne = (String[]) newArrayByClass(String.class, 10);
    }
}
