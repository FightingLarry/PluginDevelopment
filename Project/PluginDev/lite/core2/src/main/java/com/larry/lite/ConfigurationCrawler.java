
package com.larry.lite;

import java.util.List;

public interface ConfigurationCrawler {
    int ERR_CONFIG_NO_CHANGED = 4097;
    int SUCCESS = 0;
    int FAIL_RETRY = -1;
    int FAIL_NEVER_TRY = -10;
    int FAIL_NONE_NETWORK = -11;
    int FAIL_IO = -12;
    int ALREADY = 1;
    int NOT_EXPIRED = 2;
    int CONTINUE = 3;

    int crawlConfiguration(ConfigurationCrawler.Callback var1);

    void cancel();

    public interface Callback {
        void onConfigurationResult(int success, List<PluginStub> var2, long var3);
    }
}
