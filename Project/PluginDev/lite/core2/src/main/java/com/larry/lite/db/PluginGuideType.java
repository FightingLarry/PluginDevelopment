package com.larry.lite.db;

/**
 * Created by Larry on 2017/5/21.
 */

public enum PluginGuideType {

    Remote(0), SDCard(1), Assert(2);

    int value;

    PluginGuideType(int value) {
        this.value = value;
    }
}
