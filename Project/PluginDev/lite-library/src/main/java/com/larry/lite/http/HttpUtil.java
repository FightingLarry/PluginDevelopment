package com.larry.lite.http;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by fanyang.sz on 2016/12/3.
 */

public class HttpUtil {

    public static String encodeToURLParams(Map<String, String> params) {
        return encodeParams(params, "&", true);
    }

    public static String encodeParams(Map<String, String> params, String splitStr, boolean encode) {
        StringBuffer paramsBuffer = new StringBuffer();
        for (String key : params.keySet()) {
            if (params.get(key) == null) continue;

            if (paramsBuffer.length() != 0) {
                paramsBuffer.append(splitStr);
            }

            paramsBuffer.append(key + "=");
            paramsBuffer.append(encode ? encode(params.get(key)) : params.get(key));
        }
        return paramsBuffer.toString();
    }

    private static String encode(String value) {
        if (value == null) return "";

        String encoded = null;
        try {
            encoded = URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException ignore) {
        }
        StringBuffer buf = new StringBuffer(encoded.length());
        char focus;
        for (int i = 0; i < encoded.length(); i++) {
            focus = encoded.charAt(i);
            if (focus == '*') {
                buf.append("%2A");
            } else if (focus == '+') {
                buf.append("%20");
            } else if (focus == '%' && (i + 1) < encoded.length() && encoded.charAt(i + 1) == '7'
                    && encoded.charAt(i + 2) == 'E') {
                buf.append('~');
                i += 2;
            } else {
                buf.append(focus);
            }
        }
        return buf.toString();
    }

    public static boolean getTestParamsMap(Map<String, String> paramsMap) {
        String config = readConfigParams("config.txt");
        if (config == null || (config.split(";")).length < 2) {
            return false;
        }
        String[] cStr = config.split(";");
        paramsMap.put("imsi", cStr[0]);
        paramsMap.put("model", cStr[1]);
        return true;
    }


    public static final String ROOT_PATH_SDCARD =
            Environment.getExternalStorageDirectory().getPath() + "/" + "lite";

    public static String readConfigParams(String tempFile) {
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                FileInputStream fis = new FileInputStream(ROOT_PATH_SDCARD + "/" + tempFile);
                BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
                StringBuilder sb = new StringBuilder("");
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                return sb.toString();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
