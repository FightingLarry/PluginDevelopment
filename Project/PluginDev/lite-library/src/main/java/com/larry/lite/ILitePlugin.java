package com.larry.lite;

import java.io.IOException;

/**
 * Created by larry on 2017/3/2.
 */

public interface ILitePlugin {
    /**
     * 插件生命周期方法，创建回调
     */
    void onCreated();

    /**
     * 插件主业务逻辑方法，执行特定的数据收集工作
     *
     * @throws IOException
     */
    int execute() throws IOException;

    /**
     * 插件生命周期方法，销毁回调
     */
    void onDestroy();

}
