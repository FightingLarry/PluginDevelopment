
package com.larry.lite.utils;

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
