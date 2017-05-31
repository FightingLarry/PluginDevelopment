package com.larry.lite;

/**
 * Created by Larry on 2017/5/21.
 */

public enum LiteConfigType {

    Remote(0), SDCard(1), Assert(2);

    int value;

    LiteConfigType(int value) {
        this.value = value;
    }
}
