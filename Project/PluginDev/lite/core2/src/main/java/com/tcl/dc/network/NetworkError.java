//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.tcl.dc.network;

public interface NetworkError {
    int SUCCESS = 0;
    int FAIL_UNKNOWN = -1;
    int FAIL_CONNECT_TIMEOUT = -2;
    int FAIL_NOT_FOUND = -3;
    int FAIL_IO_ERROR = -4;
    int CANCEL = -5;
    int NO_AVALIABLE_NETWORK = -6;
    int SOCKET_TIMEOUT = -7;
}
