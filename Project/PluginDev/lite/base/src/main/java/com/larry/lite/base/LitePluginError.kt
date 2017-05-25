package com.larry.lite.base

interface LitePluginError {
    companion object {
        val SUCCESS = 0
        val FAIL_UNKNOWN = -1
        val FAIL_CONNECT_TIMEOUT = -2
        val FAIL_NOT_FOUND = -3
        val FAIL_IO_ERROR = -4
        val CANCEL = -5
        val NO_PERMISSIONS = 1
        val CRASH_NOT_CATCH = 2
        val CRASH_OOM = 3
        val PLUGIN_NOT_READY = 4
        val PLUGIN_NOT_EXIST = 5
        val PLUGIN_UNZIP_ERROR = 6
        val MANIFEST_PARSE_JSON_ERROR = 7
        val MANIFEST_NOT_EXIST = 8
        val MANIFEST_READ_FAIL = 9
        val MANIFEST_PLUGIN_IS_NULL = 10
        val MANIFEST_LAUNCH_IS_NULL = 11
        val MANIFEST_LAUNCH_MODE_IS_NULL = 12
        val MANIFEST_LAUNCH_PARAM_IS_NULL = 13
    }
}
