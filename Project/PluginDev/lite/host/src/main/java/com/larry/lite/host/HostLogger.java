package com.larry.lite.host;

/**
 * Created by Larry on 2017/5/16.
 */


import android.os.Process;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HostLogger {
    public static final int TRACE_REALTIME = 1;
    public static final int TRACE_OFFLINE = 2;
    public static final int TRACE_ALL = 3;
    public static final int VERBOSE = 2;
    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARN = 5;
    public static final int ERROR = 6;
    public static final int ASSERT = 7;
    protected int trace = 1;
    private int level = 4;
    private final Object mLogLock = new Object();
    private String logFileName = null;
    OutputStreamWriter logWriter = null;
    int failCount = 0;

    private HostLogger(String fileName, int level) {
        this.logFileName = fileName;
        this.level = level;
    }

    public static HostLogger getLogger(String fileName) {
        return new HostLogger(fileName, 4);
    }

    protected boolean trace(int traceLevel, String logPath) {
        if (traceLevel > 0 && traceLevel <= 3) {
            if ((traceLevel & 2) == 0 || logPath != null && logPath.length() != 0) {
                this.closeLogStream();
                if ((traceLevel & 2) != 0) {
                    this.logFileName = logPath;
                }

                this.trace = traceLevel;
                return this.openLogStream();
            } else {
                throw new IllegalArgumentException("offline trace level should with valid logPath");
            }
        } else {
            throw new IllegalArgumentException("param traceLevel invalid");
        }
    }

    private void closeLogStream() {
        if ((this.trace & 2) != 0 && this.logFileName != null && this.logFileName.length() != 0) {
            Object var1 = this.mLogLock;
            synchronized (this.mLogLock) {
                if (this.logWriter != null) {
                    try {
                        this.logWriter.close();
                    } catch (IOException var3) {
                        ;
                    }

                    this.logWriter = null;
                }

            }
        }
    }

    private boolean openLogStream() {
        if ((this.trace & 2) != 0 && this.logFileName != null && this.logFileName.length() != 0) {
            Object var1 = this.mLogLock;
            synchronized (this.mLogLock) {
                OutputStreamWriter writer = null;
                File file = new File(this.logFileName);

                try {
                    if (!file.exists()) {
                        file.createNewFile();
                    }

                    writer = new FileWriter(file, true);
                    this.logWriter = writer;
                    this.failCount = 0;
                } catch (IOException var5) {
                    return false;
                }

                return true;
            }
        } else {
            return true;
        }
    }

    public int getTraceLevel() {
        return this.trace;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int v(String tag, String msg) {
        if (2 < this.level) {
            return 0;
        } else {
            int ret = 0;
            if ((this.trace & 1) > 0) {
                ret = Log.v(tag, msg);
            }

            if ((this.trace & 2) > 0) {
                ret = this.println(2, tag, msg);
            }

            return ret;
        }
    }

    public int v(String tag, Throwable tr) {
        return 2 < this.level ? 0 : this.v(tag, this.getStackTraceString(tr));
    }

    public int d(String tag, String msg) {
        if (3 < this.level) {
            return 0;
        } else {
            int ret = 0;
            if ((this.trace & 1) > 0) {
                ret = Log.d(tag, msg);
            }

            if ((this.trace & 2) > 0) {
                ret = this.println(3, tag, msg);
            }

            return ret;
        }
    }

    public int d(String tag, Throwable tr) {
        return 3 < this.level ? 0 : this.d(tag, this.getStackTraceString(tr));
    }

    public int w(String tag, String msg) {
        if (5 < this.level) {
            return 0;
        } else {
            int ret = 0;
            if ((this.trace & 1) > 0) {
                ret = Log.w(tag, msg);
            }

            if ((this.trace & 2) > 0) {
                ret = this.println(5, tag, msg);
            }

            return ret;
        }
    }

    public int w(String tag, Throwable tr) {
        return 5 < this.level ? 0 : this.w(tag, this.getStackTraceString(tr));
    }

    public int i(String tag, String msg) {
        if (4 < this.level) {
            return 0;
        } else {
            int ret = 0;
            if ((this.trace & 1) > 0) {
                ret = Log.i(tag, msg);
            }

            if ((this.trace & 2) > 0) {
                ret = this.println(4, tag, msg);
            }

            return ret;
        }
    }

    public int i(String tag, Throwable tr) {
        return 4 < this.level ? 0 : this.i(tag, this.getStackTraceString(tr));
    }

    public int e(String tag, String msg) {
        if (6 < this.level) {
            return 0;
        } else {
            int ret = 0;
            if ((this.trace & 1) > 0) {
                ret = Log.e(tag, msg);
            }

            if ((this.trace & 2) > 0) {
                ret = this.println(6, tag, msg);
            }

            return ret;
        }
    }

    public int e(String tag, Throwable tr) {
        return 6 < this.level ? 0 : this.e(tag, this.getStackTraceString(tr));
    }

    public String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        } else {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            tr.printStackTrace(pw);
            return sw.toString();
        }
    }

    private int println(int priority, String tag, String msg) {
        if (priority >= this.level && this.failCount < 5 && this.logWriter != null) {
            String[] ps = new String[] {"", "", "V", "D", "I", "W", "E", "A"};
            SimpleDateFormat df = new SimpleDateFormat("[MM-dd HH:mm:ss.SSS]");
            String time = df.format(new Date());
            StringBuilder sb = new StringBuilder();
            sb.append(time);
            sb.append("\t");
            sb.append(ps[priority]);
            sb.append("/");
            sb.append(tag);
            int pid = Process.myPid();
            sb.append("(");
            sb.append(pid);
            sb.append("):");
            sb.append(msg);
            sb.append("\n");
            Object var9 = this.mLogLock;
            synchronized (this.mLogLock) {
                OutputStreamWriter writer = this.logWriter;

                try {
                    if (writer != null) {
                        writer.write(sb.toString());
                        writer.flush();
                    }

                    return 0;
                } catch (FileNotFoundException var17) {
                    ++this.failCount;
                    return -1;
                } catch (IOException var18) {
                    ++this.failCount;
                } finally {
                    if (this.failCount >= 5) {
                        this.closeLogStream();
                    }

                }

                return -1;
            }
        } else {
            return 0;
        }
    }
}
