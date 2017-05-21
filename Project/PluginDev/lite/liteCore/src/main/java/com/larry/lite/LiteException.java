
package com.larry.lite;

import java.io.IOException;

public class LiteException extends IOException {
    private int errorCode;

    public LiteException(int cause, String detailMessage) {
        super("err = " + cause + ", " + detailMessage);
        this.errorCode = cause;
    }

    public LiteException(int cause, String detailMessage, Throwable throwable) {
        super("err = " + cause + ", " + detailMessage, throwable);
        this.errorCode = cause;
    }

    public int errorCode() {
        return this.errorCode;
    }
}
