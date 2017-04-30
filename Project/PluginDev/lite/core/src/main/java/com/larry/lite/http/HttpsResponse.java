package com.larry.lite.http;


public class HttpsResponse {

    public final static int SUCCESS = 1;
    public final static int TIMEOUT = -1;
    public final static int IOFAIL = -2;
    public final static int FAIL = -3;

    private int code;
    private String msg;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "code=" + code + ",msg=" + msg;
    }
}
