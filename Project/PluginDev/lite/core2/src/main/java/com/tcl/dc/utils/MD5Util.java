//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.tcl.dc.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
    public MD5Util() {}

    public static String digest(String rawString) {
        try {
            return compute(new String(rawString.getBytes("UTF8"), "latin1"));
        } catch (Exception var2) {
            return "";
        }
    }

    public static String digest(Object... objs) {
        if (objs != null && objs.length != 0) {
            StringBuilder sb = new StringBuilder("");
            Object[] var2 = objs;
            int var3 = objs.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                Object param = var2[var4];
                if (param != null) {
                    if (!(param instanceof String) && !(param instanceof Integer) && !(param instanceof Long)
                            && !(param instanceof Short) && !(param instanceof Byte) && !(param instanceof Float)
                            && !(param instanceof Double) && !(param instanceof Character)
                            && !(param instanceof Boolean)) {
                        sb.append(param.toString());
                    } else {
                        sb.append(param);
                    }
                }
            }

            return digest(sb.toString());
        } else {
            return null;
        }
    }

    private static String compute(String str) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        char[] charArray = str.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; ++i) {
            byteArray[i] = (byte) charArray[i];
        }

        byte[] md5Bytes = md5.digest(byteArray);
        return toHexString(md5Bytes);
    }

    public static byte[] encode16(String origin, String enc)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        if (origin != null && origin.length() != 0) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            if (md == null) {
                throw new IllegalAccessError("no md5 algorithm");
            } else {
                byte[] bytes = md.digest(origin.getBytes(enc));
                byte[] dstBytes = new byte[8];
                System.arraycopy(bytes, 4, dstBytes, 0, 8);
                bytes = null;
                return dstBytes;
            }
        } else {
            return null;
        }
    }

    public static String toHexString(byte[] bytes) {
        if (bytes != null && bytes.length != 0) {
            StringBuilder hexValue = new StringBuilder();
            byte[] var2 = bytes;
            int var3 = bytes.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                byte b = var2[var4];
                int val = b & 255;
                if (val < 16) {
                    hexValue.append("0");
                }

                hexValue.append(Integer.toHexString(val));
            }

            return hexValue.toString();
        } else {
            return null;
        }
    }

    public static String getFileMD5(String path) {
        File file = new File(path);
        if (!file.isFile()) {
            return null;
        } else {
            MessageDigest digest = null;
            FileInputStream in = null;
            byte[] buffer = new byte[1024];

            try {
                digest = MessageDigest.getInstance("MD5");
                in = new FileInputStream(file);

                int len;
                while ((len = in.read(buffer, 0, 1024)) != -1) {
                    digest.update(buffer, 0, len);
                }

                in.close();
            } catch (Exception var15) {
                var15.printStackTrace();
            } finally {
                if (null != in) {
                    try {
                        in.close();
                    } catch (IOException var14) {
                        var14.printStackTrace();
                    }
                }

            }

            return toHexString(digest.digest());
        }
    }
}
