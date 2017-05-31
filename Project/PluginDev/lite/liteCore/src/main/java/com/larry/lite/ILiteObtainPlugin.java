package com.larry.lite;

public interface ILiteObtainPlugin {
    int ERR_CONFIG_NO_CHANGED = 4097;
    int SUCCESS = 0;
    int FAIL_RETRY = -1;
    int FAIL_NEVER_TRY = -10;
    int FAIL_NONE_NETWORK = -11;
    int FAIL_IO = -12;
    int FAIL_NULL = -13;
    int ALREADY = 1;
    int NOT_EXPIRED = 2;
    int CONTINUE = 3;

    int obtain(ILiteObtainPlugin.Callback callback);

    void cancel();

    public interface Callback {
        void onObtainResult(int success, LitePluginsConfigInfo litePluginsConfigInfo);
    }
}
