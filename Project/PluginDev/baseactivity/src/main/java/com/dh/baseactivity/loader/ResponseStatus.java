package com.dh.baseactivity.loader;

public enum ResponseStatus {

    Loading(0), Ok(1), Error(2);

    private int value;

    private ResponseStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
