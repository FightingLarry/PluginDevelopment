
package com.larry.lite;

import com.larry.lite.base.LitePlugin;

public interface LiteRuntime {
    void checkPluginsForLaunch(LiteEvent var1, Object var2);

    ClassLoader createClassLoader(LiteStub var1) throws Exception;

    LitePlugin loadPlugin(ClassLoader var1, LiteStub var2) throws Exception;

    void destroy();
}
