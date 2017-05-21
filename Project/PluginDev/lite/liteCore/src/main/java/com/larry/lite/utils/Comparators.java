
package com.larry.lite.utils;

public class Comparators {
    public Comparators() {}

    public static int compare(long lhs, long rhs) {
        return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
    }

    public static int compare(int lhs, long rhs) {
        return (long) lhs < rhs ? -1 : ((long) lhs == rhs ? 0 : 1);
    }

    public static int compare(float lhs, float rhs) {
        return Float.compare(lhs, rhs);
    }

    public static int compare(double lhs, double rhs) {
        return Double.compare(lhs, rhs);
    }
}
