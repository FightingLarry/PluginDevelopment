
package com.tcl.dc.manager.download;

public interface PluginDownloadError {
    int SUCCESS = 0;
    int SUCCESS_REQ = 1;
    int SUCCESS_MD5 = 2;
    int FAIL_HTTP_NOT_FOUND = 3;
    int FAIL_NOT_AVAILABLE = 4;
    int FAIL_CANCEL = 5;
    int FAIL_IO_ERROR = 6;
    int FAIL_MD5_ERROR = 7;
    int FAIL_MANIFEST_ERROR = 8;
    int FAIL_UNKNOWN = 9;
}
