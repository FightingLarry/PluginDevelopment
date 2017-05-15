//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.tcl.dc.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class CollectionUtils {
    public CollectionUtils() {}

    public static boolean isEmpty(Collection<? extends Object> c) {
        return c == null || c.size() == 0;
    }

    public static boolean isEmpty(Object[] objs) {
        return objs == null || objs.length == 0;
    }

    public static <T> T get(Collection<T> c, int index) {
        if (isEmpty(c)) {
            return null;
        } else if (index >= 0 && index < c.size()) {
            if (c instanceof List) {
                return ((List<T>) c).get(index);
            } else {
                List<? extends T> a = new ArrayList(c);
                return a.get(index);
            }
        } else {
            return null;
        }
    }

    public static <T> T first(Collection<T> c) {
        if (isEmpty(c)) {
            return null;
        } else if (c instanceof List) {
            return ((List<T>) c).get(0);
        } else {
            Iterator<T> iter = c.iterator();
            return iter.hasNext() ? iter.next() : null;
        }
    }

    public static <T> T last(Collection<T> c) {
        if (isEmpty(c)) {
            return null;
        } else if (c instanceof List) {
            return ((List<T>) c).get(c.size() - 1);
        } else {
            List<T> a = new ArrayList(c);
            return a.get(a.size() - 1);
        }
    }

    public static <E> Collection<E> diff(Collection<E> l, Collection<E> r) {
        if (!isEmpty(l) && !isEmpty(r)) {
            List<E> s = new ArrayList(l);
            s.removeAll(r);
            return s;
        } else {
            return l;
        }
    }

    public static <E> Collection<E> diffLeft(Collection<E> l, Collection<E> r) {
        if (!isEmpty(l) && !isEmpty(r)) {
            List<E> s = new ArrayList(l);
            s.removeAll(r);
            r.removeAll(l);
            return s;
        } else {
            return l;
        }
    }

    public static <E> Collection<E> same(Collection<E> l, Collection<E> r) {
        if (!isEmpty(l) && !isEmpty(r)) {
            List<E> s = new ArrayList(l);
            s.removeAll(r);
            List<E> k = new ArrayList(l);
            k.removeAll(s);
            return k;
        } else {
            return null;
        }
    }

    public static <E> int sizeOf(Collection<E> c) {
        return isEmpty(c) ? 0 : c.size();
    }
}
