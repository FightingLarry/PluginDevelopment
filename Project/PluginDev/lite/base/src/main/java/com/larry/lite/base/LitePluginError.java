
package com.larry.lite.base;

public interface LitePluginError {
    int SUCCESS = 0;
    int FAIL_UNKNOWN = -1;
    int FAIL_CONNECT_TIMEOUT = -2;
    int FAIL_NOT_FOUND = -3;
    int FAIL_IO_ERROR = -4;
    int CANCEL = -5;
    int NO_PERMISSIONS = 1;
    int CRASH_NOT_CATCH = 2;
    int CRASH_OOM = 3;
    int PLUGIN_NOT_READY = 4;
    int PLUGIN_NOT_EXIST = 5;
    int PLUGIN_UNZIP_ERROR = 6;
    int MANIFEST_PARSE_JSON_ERROR = 7;
    int MANIFEST_NOT_EXIST = 8;
    int MANIFEST_READ_FAIL = 9;
    int MANIFEST_PLUGIN_IS_NULL = 10;
    int MANIFEST_LAUNCH_IS_NULL = 11;
    int MANIFEST_LAUNCH_MODE_IS_NULL = 12;
    int MANIFEST_LAUNCH_PARAM_IS_NULL = 13;
}
