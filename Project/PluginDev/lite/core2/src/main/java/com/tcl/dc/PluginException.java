
package com.tcl.dc;

import java.io.IOException;

public class PluginException extends IOException {
    private int errorCode;

    public PluginException(int cause, String detailMessage) {
        super("err = " + cause + ", " + detailMessage);
        this.errorCode = cause;
    }

    public PluginException(int cause, String detailMessage, Throwable throwable) {
        super("err = " + cause + ", " + detailMessage, throwable);
        this.errorCode = cause;
    }

    public int errorCode() {
        return this.errorCode;
    }
}
