package com.dh.baseactivity.loader;

public class ResponseCode {

    public static int META_CODE_DEFAULT = 0;

    /**
     * Success(1), 正常返回，请解析data里的东西
     */
    public static int META_CODE_OK = 1; //

    /**
     * Unauthorized(401), // 请客户端妥妥踢票
     */
    public static int META_CODE_UNAUTHORIZED = 401; //

    /**
     * Forbidden(403), // 没有权限访问（一般发生这种事情就是尝试删别人贴啊之类的）
     */
    public static int META_CODE_FORBIDDEN = 403;//

    /**
     * ServerFailed(500), // 请客户端安抚用户，服务器GG了
     */
    public static int META_CODE_SERVER_FAILED = 500;// 


}
