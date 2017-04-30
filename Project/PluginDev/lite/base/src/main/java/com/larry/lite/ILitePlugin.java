package com.larry.lite;

import java.io.IOException;

/**
 * Created by larry on 2017/3/2. 轻量级插件的接口
 */
public interface ILitePlugin {
    /**
     * 初始化
     */
    void onCreated();

    /**
     * 执行的主要方法，执行后台能做的事情。
     *
     * @throws IOException
     */
    int execute() throws IOException;

    /**
     * 结束
     */
    void onDestroy();

}
