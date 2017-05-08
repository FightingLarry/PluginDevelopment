package com.larry.lite;

/**
 * Created by Larry on 2017/5/8.
 */

public interface PluginError {

    public static final int SUCCESS = 0;
    public static final int FAIL_UNKNOWN = -1;
    public static final int FAIL_CONNECT_TIMEOUT = -2;
    public static final int FAIL_NOT_FOUND = -3;
    public static final int FAIL_IO_ERROR = -4;
    public static final int CANCEL = -5;
    public static final int NO_PERMISSIONS = 1;
    public static final int CRASH_NOT_CATCH = 2;
    public static final int CRASH_OOM = 3;
    public static final int PLUGIN_NOT_READY = 4;
    public static final int PLUGIN_NOT_EXIST = 5;
    public static final int PLUGIN_UNZIP_ERROR = 6;
    public static final int MANIFEST_PARSE_JSON_ERROR = 7;
    public static final int MANIFEST_NOT_EXIST = 8;
    public static final int MANIFEST_READ_FAIL = 9;
    public static final int MANIFEST_PLUGIN_IS_NULL = 10;
    public static final int MANIFEST_LAUNCH_IS_NULL = 11;
    public static final int MANIFEST_LAUNCH_MODE_IS_NULL = 12;
    public static final int MANIFEST_LAUNCH_PARAM_IS_NULL = 13;

}
