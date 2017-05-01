package com.larry.lite.utils;


import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;


public class IOUtils {
    private static final int MAX_BUFFER_BYTES = 1024;

    public static byte[] numberToBytes(long s, int len, boolean big_endian) {
        byte[] buffer = new byte[len];
        int start = big_endian ? len - 1 : 0;
        int end = big_endian ? -1 : len;
        int inc = big_endian ? -1 : 1;

        for (int i = start; i != end; i += inc) {
            buffer[i] = (byte) (int) (s & 0xFF);
            s >>>= 8;
        }

        return buffer;
    }

    public static int readShort(InputStream in) throws IOException {
        return (int) readNumber(in, 2, true);
    }

    public static int readShort(InputStream in, boolean big_endian) throws IOException {
        return (int) readNumber(in, 2, big_endian);
    }

    public static long readInt(InputStream in) throws IOException {
        return readNumber(in, 4, true);
    }

    public static long readInt(InputStream in, boolean big_endian) throws IOException {
        return readNumber(in, 4, big_endian);
    }

    public static float readFloat(InputStream in) throws IOException {
        return readFloat(in, true);
    }

    public static float readFloat(InputStream in, boolean big_endian) throws IOException {
        int i = (int) readInt(in, big_endian);
        return Float.intBitsToFloat(i);
    }

    public static double readDouble(InputStream in) throws IOException {
        return readDouble(in, true);
    }

    public static double readDouble(InputStream in, boolean big_endian) throws IOException {
        long l = readNumber(in, 8, big_endian);
        return Double.longBitsToDouble(l);
    }

    public static long readNumber(InputStream in, int len, boolean big_endian) throws IOException {
        if ((len <= 0) || (len > 8)) {
            throw new IllegalArgumentException("length must between 1 and 8.");
        }
        byte[] buffer = new byte[len];
        if (in.markSupported()) {
            in.mark(len);
        }
        int count = in.read(buffer, 0, len);

        if (count <= 0) {
            buffer = null;
            return -1L;
        }

        int start = big_endian ? 0 : count - 1;
        int end = big_endian ? count : -1;
        int inc = big_endian ? 1 : -1;
        long ret = 0L;

        for (int i = start; i != end; i += inc) {
            ret <<= 8;
            ret |= buffer[i] & 0xFF;
        }

        return ret;
    }

    public static byte[] readBytes(InputStream in, int len) throws IOException {
        if (len <= 0) {
            return null;
        }

        int pos = 0;
        int recvBytes = 0;
        byte[] ret = null;
        byte[] buffer = new byte[len];
        try {
            while ((pos < len) && ((recvBytes = in.read(buffer, pos, len - pos)) > 0)) {
                pos += recvBytes;
            }

            ret = buffer;
        } finally {
            buffer = null;
        }

        return ret;
    }

    public static String readString(InputStream in, int len) throws IOException {
        int leftBytes = len;
        int recvBytes = 0;
        int bufLen = Math.min(leftBytes, 1024);

        byte[] buffer = new byte[bufLen];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);

        String result = null;
        try {
            while ((leftBytes > 0) && ((recvBytes = in.read(buffer, 0, bufLen)) != -1)) {
                outputStream.write(buffer, 0, recvBytes);
                leftBytes -= recvBytes;
            }

            result = outputStream.toString();
        } finally {
            buffer = null;
            outputStream.close();
        }

        return result;
    }

    public static String readString(InputStream in, int len, String characterSet) throws IOException {
        int leftBytes = len;
        int recvBytes = 0;
        int bufLen = Math.min(leftBytes, 1024);

        byte[] buffer = new byte[bufLen];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);

        while ((leftBytes > 0) && ((recvBytes = in.read(buffer, 0, bufLen)) != -1)) {
            outputStream.write(buffer, 0, recvBytes);
            leftBytes -= recvBytes;
        }

        buffer = null;
        String result = outputStream.toString(characterSet);
        outputStream.close();
        return result;
    }

    public static void writeShort(OutputStream out, int s) throws IOException {
        byte[] buffer = {(byte) (s >> 8 & 0xFF), (byte) (s & 0xFF)};

        out.write(buffer);
        out.flush();
        buffer = null;
    }

    public static void writeShort(OutputStream out, int s, boolean big_endian) throws IOException {
        writeNumber(out, s, 2, big_endian);
    }

    public static void writeInt(OutputStream out, long s) throws IOException {
        byte[] buffer =
                {(byte) (int) (s >> 24 & 0xFF), (byte) (int) (s >> 16 & 0xFF), (byte) (int) (s >> 8 & 0xFF),
                        (byte) (int) (s & 0xFF)};

        out.write(buffer);
        out.flush();
        buffer = null;
    }

    public static void writeInt(OutputStream out, long s, boolean big_endian) throws IOException {
        writeNumber(out, s, 4, big_endian);
    }

    public static void writeFloat(OutputStream out, float f) throws IOException {
        writeFloat(out, f, true);
    }

    public static void writeFloat(OutputStream out, float f, boolean big_endian) throws IOException {
        int bits = Float.floatToIntBits(f);
        writeInt(out, bits, big_endian);
    }

    public static void writeDouble(OutputStream out, double d) throws IOException {
        writeDouble(out, d, true);
    }

    public static void writeDouble(OutputStream out, double d, boolean big_endian) throws IOException {
        long bits = Double.doubleToLongBits(d);
        writeNumber(out, bits, 8, big_endian);
    }

    public static void writeNumber(OutputStream out, long s, int len, boolean big_endian) throws IOException {
        if ((len <= 0) || (len > 8)) {
            throw new IllegalArgumentException("length must between 1 and 8.");
        }
        byte[] buffer = numberToBytes(s, len, big_endian);
        out.write(buffer);
        out.flush();
    }

    public static void writeCString(OutputStream out, String s) throws IOException {
        writeCString(out, s, "utf-8");
    }

    public static void writeWString(OutputStream out, String s) throws IOException {
        byte[] bytes = s.getBytes();
        writeShort(out, bytes.length);
        out.write(bytes);
        out.flush();
    }

    public static void writeCString(OutputStream os, String s, String characterSet) throws IOException {
        if ((s == null) || (s.length() == 0)) {
            os.write(0);
            return;
        }
        if (characterSet == null) {
            characterSet = "utf-8";
        }
        byte[] bytes = s.getBytes(characterSet);
        writeCLenData(os, bytes);
    }

    public static void writeWString(OutputStream out, String s, String characterSet, boolean big_endian)
            throws IOException {
        if ((s == null) || (s.length() == 0)) {
            writeShort(out, 0);
            return;
        }
        if (characterSet == null) {
            characterSet = "utf-8";
        }
        byte[] bytes = s.getBytes(characterSet);
        writeWLenData(out, bytes, big_endian);
    }

    public static void writeCString(OutputStream out, String s, int fixedLen) throws IOException {
        byte[] bytes = s.getBytes();
        out.write(bytes.length);
        fixedLen--;

        if (fixedLen <= 0) {
            return;
        }
        if (fixedLen <= bytes.length) {
            out.write(bytes, 0, fixedLen);
        } else {
            out.write(bytes);
            byte[] fillBytes = new byte[fixedLen - bytes.length];
            Arrays.fill(fillBytes, (byte) 0);
            out.write(fillBytes);
        }

        out.flush();
    }

    public static void writeWString(OutputStream out, String s, int fixedLen) throws IOException {
        byte[] bytes = s.getBytes();
        writeShort(out, bytes.length);
        fixedLen -= 2;

        if (fixedLen <= 0) {
            return;
        }
        if (fixedLen <= bytes.length) {
            out.write(bytes, 0, fixedLen);
        } else {
            out.write(bytes);
            byte[] fillBytes = new byte[fixedLen - bytes.length];
            Arrays.fill(fillBytes, (byte) 0);
            out.write(fillBytes);
        }

        out.flush();
    }

    public static void writeCString(OutputStream out, String s, String characterSet, int fixedLen) throws IOException {
        byte[] bytes = s.getBytes(characterSet);
        out.write(bytes.length);
        fixedLen--;

        if (fixedLen <= 0) {
            return;
        }
        if (fixedLen <= bytes.length) {
            out.write(bytes, 0, fixedLen);
        } else {
            out.write(bytes);
            byte[] fillBytes = new byte[fixedLen - bytes.length];
            Arrays.fill(fillBytes, (byte) 0);
            out.write(fillBytes);
        }

        out.flush();
    }

    public static void writeWString(OutputStream out, String s, String characterSet, int fixedLen) throws IOException {
        byte[] bytes = s.getBytes(characterSet);
        writeShort(out, bytes.length);
        fixedLen -= 2;

        if (fixedLen <= 0) {
            return;
        }
        if (fixedLen <= bytes.length) {
            out.write(bytes, 0, fixedLen);
        } else {
            out.write(bytes);
            byte[] fillBytes = new byte[fixedLen - bytes.length];
            Arrays.fill(fillBytes, (byte) 0);
            out.write(fillBytes);
        }

        out.flush();
    }

    public static ReadableByteChannel getChannel(InputStream inputStream) {
        return inputStream != null ? Channels.newChannel(inputStream) : null;
    }

    public static WritableByteChannel getChannel(OutputStream outputStream) {
        return outputStream != null ? Channels.newChannel(outputStream) : null;
    }

    public static long exhaust(InputStream input) throws IOException {
        long result = 0L;

        if (input != null) {
            byte[] buf = new byte[512];
            try {
                int read = input.read(buf);
                result = read == -1 ? -1L : 0L;

                while (read != -1) {
                    result += read;
                    read = input.read(buf);
                }

            } finally {
                buf = null;
            }
        }

        return result;
    }

    public static void skip(InputStream in, int len) throws IOException {
        if ((in == null) || (len <= 0)) {
            return;
        }

        int recvBytes = 0;
        byte[] buffer = new byte[512];
        do {
            int need = Math.min(buffer.length, len);
            recvBytes = in.read(buffer, 0, need);
            if (recvBytes < 0) break;
            len -= recvBytes;
        } while (len > 0);
        buffer = null;
    }

    public static String readLeft(InputStream in) throws IOException {
        String result = null;
        if (in == null) {
            return null;
        }

        int recvBytes = 0;
        byte[] buffer = new byte[512];

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
        try {
            while ((recvBytes = in.read(buffer, 0, 512)) >= 0) {
                outputStream.write(buffer, 0, recvBytes);
            }

            buffer = null;
            result = outputStream.toString();
        } finally {
            outputStream.close();
            buffer = null;
        }

        return result;
    }

    public static String readLeft(InputStream in, String characterSet) throws IOException {
        String result = null;
        if (in == null) {
            return null;
        }

        int recvBytes = 0;
        byte[] buffer = new byte[512];

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
        try {
            while ((recvBytes = in.read(buffer, 0, 512)) >= 0) {
                outputStream.write(buffer, 0, recvBytes);
            }

            result = outputStream.toString(characterSet);
        } finally {
            outputStream.close();
        }

        return result;
    }

    public static byte[] readLeftBytes(InputStream in) throws IOException {
        if (in == null) {
            return null;
        }

        byte[] result = null;
        int recvBytes = 0;
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
        try {
            while ((recvBytes = in.read(buffer, 0, 1024)) >= 0) {
                outputStream.write(buffer, 0, recvBytes);
            }

            buffer = null;
            result = outputStream.toByteArray();
        } finally {
            outputStream.close();
            buffer = null;
        }

        return result;
    }

    public static byte[] createZeroBytes(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be gt 0");
        }
        byte[] bytes = new byte[length];
        Arrays.fill(bytes, (byte) 0);
        return bytes;
    }

    public static int indexOf(byte[] datas, int start, byte[] t) {
        if ((datas == null) || (t == null)) {
            throw new NullPointerException("source or target array is null!");
        }

        int index = -1;
        int len = datas.length;
        int tlen = t.length;

        if ((start >= len) || (len - start < tlen)) {
            return -1;
        }

        while (start <= len - tlen) {
            int i = 0;
            for (; i < tlen; i++) {
                if (datas[(start + i)] != t[i]) {
                    break;
                }
            }

            if (i == tlen) {
                index = start;
                break;
            }

            start++;
        }

        return index;
    }

    public static int parseInteger(byte[] buf, boolean bigEndian) {
        return (int) parseNumber(buf, 4, bigEndian);
    }

    public static int parseShort(byte[] buf, boolean bigEndian) {
        return (int) parseNumber(buf, 2, bigEndian);
    }

    public static long parseNumber(byte[] buf, int len, boolean bigEndian) {
        if ((buf == null) || (buf.length == 0)) {
            throw new IllegalArgumentException("byte array is null or empty!");
        }

        int mlen = Math.min(len, buf.length);
        long r = 0L;
        if (bigEndian)
            for (int i = 0; i < mlen; i++) {
                r <<= 8;
                r |= buf[i] & 0xFF;
            }
        else
            for (int i = mlen - 1; i >= 0; i--) {
                r <<= 8;
                r |= buf[i] & 0xFF;
            }
        return r;
    }

    public static boolean startWiths(byte[] all, byte[] sub) {
        if ((all == null) || (sub == null) || (all.length < sub.length)) {
            return false;
        }
        for (int i = 0; i < sub.length; i++) {
            if (all[i] != sub[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean endWiths(byte[] all, byte[] sub) {
        if ((all == null) || (sub == null) || (all.length < sub.length)) return false;
        int allLen = all.length;
        int subLen = sub.length;

        for (int i = 1; i < subLen + 1; i++) {
            if (all[(allLen - i)] != sub[(subLen - i)]) {
                return false;
            }
        }
        return true;
    }

    public static boolean endWiths(byte[] all, int length, byte[] sub) {
        if ((all == null) || (sub == null) || (length < sub.length)) {
            return false;
        }
        int allLen = Math.min(all.length, length);
        int subLen = sub.length;

        for (int i = 1; i < subLen + 1; i++) {
            if (all[(allLen - i)] != sub[(subLen - i)]) {
                return false;
            }
        }
        return true;
    }

    public static byte[] readCLenData(InputStream is) throws IOException {
        int len = is.read();
        if (len <= 0) {
            return null;
        }

        return readBytes(is, len);
    }

    public static void writeCLenData(OutputStream os, byte[] bytes) throws IOException {
        if (bytes == null) {
            os.write(0);
        } else {
            os.write(bytes.length);
            os.write(bytes);
        }
    }

    public static byte[] readWLenData(InputStream is, boolean big_endian) throws IOException {
        int len = readShort(is, big_endian);
        if (len <= 0) {
            return null;
        }

        return readBytes(is, len);
    }

    public static void writeWLenData(OutputStream os, byte[] bytes, boolean big_endian) throws IOException {
        if (bytes == null) {
            writeShort(os, 0, big_endian);
        } else {
            writeShort(os, bytes.length, big_endian);
            os.write(bytes);
        }
    }

    public static String readCString(InputStream is, String characterSet) throws IOException {
        int len = is.read();
        if (len <= 0) {
            return null;
        }
        return readString(is, len, characterSet);
    }

    public static String readWString(InputStream is, boolean big_endian, String characterSet) throws IOException {
        int len = readShort(is, big_endian);
        if (len <= 0) {
            return null;
        }
        return readString(is, len, characterSet);
    }

    public static void close(Closeable conn) {
        try {
            if (conn != null) conn.close();
        } catch (IOException localIOException) {}
    }
}
