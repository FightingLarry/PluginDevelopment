
package com.larry.lite.utils;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class StringUtils {
    public StringUtils() {}

    public static boolean isNull(String str) {
        return str == null;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static int strLen(String s) {
        int length = 0;

        for (int i = 0; i < s.length(); ++i) {
            int ascii = Character.codePointAt(s, i);
            if (ascii >= 0 && ascii <= 255) {
                ++length;
            } else {
                length += 2;
            }
        }

        return length;
    }

    public static String substring(String s, int subLen) {
        return substring(s, subLen, "..");
    }

    public static String substring(String s, int subLen, String postfix) {
        int length = 0;

        for (int i = 0; i < s.length(); ++i) {
            int ascii = Character.codePointAt(s, i);
            if (ascii >= 0 && ascii <= 255) {
                ++length;
            } else {
                length += 2;
            }

            if (length > subLen || length == subLen && i + 1 != s.length()) {
                return s.substring(0, i).concat(postfix);
            }
        }

        return s;
    }

    public static byte[] getBytesUtf8(String string) {
        return getBytesUnchecked(string, "utf-8");
    }

    public static byte[] getBytesUnchecked(String string, String charsetName) {
        if (string == null) {
            return null;
        } else {
            try {
                return string.getBytes(charsetName);
            } catch (UnsupportedEncodingException var3) {
                throw newIllegalStateException(charsetName, var3);
            }
        }
    }

    private static IllegalStateException newIllegalStateException(String charsetName, UnsupportedEncodingException e) {
        return new IllegalStateException(charsetName + ": " + e);
    }

    public static Date stringToDate(String str) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;

        try {
            date = format.parse(str);
        } catch (ParseException var4) {
            ;
        }

        return date;
    }

    public static String timeToDate(String str) {
        try {
            Date d = new Date(Long.parseLong(str));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(d);
        } catch (Exception var3) {
            return "";
        }
    }

    public static String timeToDate(Long time) {
        Date d = new Date(time.longValue());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(d);
    }

    public static String getTime2GMT(long time) {
        StringBuffer sb = new StringBuffer();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
        if (time > 0L) {
            calendar.setTimeInMillis(time);
        }

        sb.append(calendar.get(1));
        sb.append("-");
        if (calendar.get(2) + 1 < 10) {
            sb.append(0);
        }

        sb.append(calendar.get(2) + 1);
        sb.append("-");
        if (calendar.get(5) < 10) {
            sb.append(0);
        }

        sb.append(calendar.get(5));
        sb.append(" ");
        if (calendar.get(11) < 10) {
            sb.append(0);
        }

        sb.append(calendar.get(11));
        sb.append(":");
        if (calendar.get(12) < 10) {
            sb.append(0);
        }

        sb.append(calendar.get(12));
        sb.append(":");
        if (calendar.get(13) < 10) {
            sb.append(0);
        }

        sb.append(calendar.get(13));
        return sb.toString();
    }

    public static String getTime2GMT() {
        return getTime2GMT(0L);
    }

    public static boolean mailAddressVerify(String mailAddress) {
        String emailExp =
                "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(emailExp);
        return p.matcher(mailAddress).matches();
    }

    public static String parseNumber(int number) {
        String pattern = "#,###";
        DecimalFormat formatter = new DecimalFormat();
        formatter.applyPattern(pattern);
        return formatter.format((long) number);
    }

    public static String parseNumber(int number, Locale l) {
        String pattern = "#,###";
        DecimalFormatSymbols value = new DecimalFormatSymbols(l);
        DecimalFormat formatter = new DecimalFormat(pattern, value);
        return formatter.format((long) number);
    }

    public static String repeat(String str, int repeatTimes) {
        int inputLen = str.length();
        int outputLen = inputLen * repeatTimes;
        switch (inputLen) {
            case 1:
                return repeat(str.charAt(0), repeatTimes);
            case 2:
                char ch0 = str.charAt(0);
                char ch1 = str.charAt(1);
                char[] output = new char[outputLen];

                for (int i = 0; i < repeatTimes * 2; i += 2) {
                    output[i] = ch0;
                    output[i + 1] = ch1;
                }

                return Arrays.toString(output);
            default:
                StringBuilder buf = new StringBuilder();

                for (int i = 0; i <= repeatTimes - 1; ++i) {
                    buf.append(str);
                }

                return buf.toString();
        }
    }

    public static String repeat(char ch, int repeatTimes) {
        char[] buf = new char[repeatTimes];

        for (int i = repeatTimes - 1; i >= 0; --i) {
            buf[i] = ch;
        }

        return new String(buf);
    }
}
