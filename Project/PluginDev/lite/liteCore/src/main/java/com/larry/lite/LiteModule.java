
package com.larry.lite;

public interface LiteModule {
    /**
     * 注册组件
     * @param service
     */
    void registerComponents(LiteService service);

    /**
     * 远程轻应用的配置
     * @param service
     * @param liteContext
     */
    void applyOptions(LiteService service, LiteContext liteContext);


}
