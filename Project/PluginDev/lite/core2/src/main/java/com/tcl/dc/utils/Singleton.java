//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.tcl.dc.utils;

public abstract class Singleton<T> {
    private T mInstance;

    public Singleton() {}

    protected abstract T create();

    public final T get() {
        synchronized (this) {
            if (this.mInstance == null) {
                this.mInstance = this.create();
            }

            return this.mInstance;
        }
    }
}
