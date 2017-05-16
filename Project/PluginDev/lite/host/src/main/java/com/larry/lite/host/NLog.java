package com.larry.lite.host;

/**
 * Created by Larry on 2017/5/16.
 */


import android.text.TextUtils;
import java.io.File;

public final class NLog {
    private static final String LOG_FILENAME = "tcl_logcat.log";
    private static boolean debug = BuildConfig.DEBUG;
    private static Logger logger = null;

    public NLog() {}

    public static Logger getLogger() {
        return logger;
    }

    public static synchronized void setDebug(boolean d, int level) {
        boolean old = debug;
        if (old != d) {
            if (old) {
                trace(1, (String) null);
            }

            debug = d;
            if (d) {
                if (logger == null) {
                    logger = Logger.getLogger((String) null);
                }

                logger.setLevel(level);
            }

        }
    }

    public static boolean isDebug() {
        return debug;
    }

    public static synchronized boolean trace(int level, String path) {
        if (!debug) {
            throw new IllegalStateException("you should enable log before modifing trace mode");
        } else {
            if (logger == null) {
                logger = Logger.getLogger((String) null);
            }

            if (level == 3 || level == 2) {
                if (TextUtils.isEmpty(path)) {
                    throw new IllegalArgumentException("path should not be null for offline and all mode");
                }

                File dir = new File(path);
                if (!dir.exists() || !dir.isDirectory()) {
                    boolean b = dir.mkdirs();
                    if (!b) {
                        return false;
                    }
                }

                StringBuffer sb = new StringBuffer(path);
                sb.append(File.separator);
                sb.append("tcl_logcat.log");
                path = sb.toString();
            }

            return logger.trace(level, path);
        }
    }

    private static String buildWholeMessage(String format, Object... args) {
        if (args != null && args.length != 0) {
            String msg = String.format(format, args);
            return msg;
        } else {
            return format;
        }
    }

    public static void d(String tag, String format, Object... args) {
        if (debug) {
            try {
                logger.d(tag, buildWholeMessage(format, args));
            } catch (Exception var4) {
                ;
            }
        }

    }

    public static void i(String tag, String format, Object... args) {
        if (debug) {
            try {
                logger.i(tag, buildWholeMessage(format, args));
            } catch (Exception var4) {
                ;
            }
        }

    }

    public static void e(String tag, String format, Object... args) {
        if (debug) {
            try {
                logger.e(tag, buildWholeMessage(format, args));
            } catch (Exception var4) {
                ;
            }
        }

    }

    public static void e(String tag, Throwable e) {
        if (debug) {
            logger.e(tag, e);
        }

    }

    public static void v(String tag, String format, Object... args) {
        if (debug) {
            try {
                logger.v(tag, buildWholeMessage(format, args));
            } catch (Exception var4) {
                ;
            }
        }

    }

    public static void w(String tag, String format, Object... args) {
        if (debug) {
            try {
                logger.w(tag, buildWholeMessage(format, args));
            } catch (Exception var4) {
                ;
            }
        }

    }

    public static void printStackTrace(Exception e) {
        if (debug) {
            try {
                logger.e("TCLException", e);
            } catch (Exception var2) {
                ;
            }
        }

    }
}
