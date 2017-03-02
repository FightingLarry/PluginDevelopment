package com.larry.light.loader;

public enum LightResponseStatus {

    Loading(0), Ok(1), Error(2);

    private int value;

    private LightResponseStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
