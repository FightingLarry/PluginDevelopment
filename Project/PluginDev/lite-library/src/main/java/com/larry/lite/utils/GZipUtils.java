package com.larry.lite.utils;

/**
 * Created by Larry on 2017/3/9.
 */

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public abstract class GZipUtils {

    public static final int BUFFER = 1024;
    public static final String EXT = ".gz";


    public static byte[] compress(byte[] data) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compress(bais, baos);
        byte[] output = baos.toByteArray();

        try {
            baos.flush();
        } catch (IOException var7) {
            var7.printStackTrace();
        }

        try {
            baos.close();
        } catch (IOException var6) {
            var6.printStackTrace();
        }

        try {
            bais.close();
        } catch (IOException var5) {
            var5.printStackTrace();
        }

        return output;
    }

    public static void compress(File file) {
        compress(file, true);
    }

    public static void compress(File file, boolean delete) {
        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            fis = new FileInputStream(file);
            fos = new FileOutputStream(file.getPath() + ".gz");
            compress(fis, fos);
        } catch (FileNotFoundException var21) {
            var21.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException var20) {
                var20.printStackTrace();
            }

            try {
                if (fos != null) {
                    fos.flush();
                }
            } catch (IOException var19) {
                var19.printStackTrace();
            }

            try {
                fos.close();
            } catch (IOException var18) {
                var18.printStackTrace();
            }

        }

        if (delete) {
            file.delete();
        }

    }

    public static void compress(InputStream is, OutputStream os) {
        GZIPOutputStream gos = null;

        try {
            gos = new GZIPOutputStream(os);
            byte[] data = new byte[1024];

            int e;
            while ((e = is.read(data, 0, 1024)) != -1) {
                gos.write(data, 0, e);
            }

            gos.finish();
            gos.flush();
        } catch (Exception var13) {
            var13.printStackTrace();
        } finally {
            try {
                if (gos != null) {
                    gos.close();
                }
            } catch (IOException var12) {
                var12.printStackTrace();
            }

        }

    }

    public static void compress(String path) {
        compress(path, true);
    }

    public static void compress(String path, boolean delete) {
        File file = new File(path);
        compress(file, delete);
    }

    public static byte[] decompress(byte[] data) {
        ByteArrayInputStream bais = null;
        ByteArrayOutputStream baos = null;

        try {
            bais = new ByteArrayInputStream(data);
            baos = new ByteArrayOutputStream();
            decompress((InputStream) bais, (OutputStream) baos);
            data = baos.toByteArray();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                }
            } catch (IOException var15) {
                var15.printStackTrace();
            }

            try {
                baos.close();
            } catch (IOException var14) {
                var14.printStackTrace();
            }

            try {
                if (bais != null) {
                    bais.close();
                }
            } catch (IOException var13) {
                var13.printStackTrace();
            }

        }

        return data;
    }

    public static void decompress(File file) {
        decompress(file, true);
    }

    public static void decompress(File file, boolean delete) {
        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            fis = new FileInputStream(file);
            fos = new FileOutputStream(file.getPath().replace(".gz", ""));
            decompress((InputStream) fis, (OutputStream) fos);
        } catch (FileNotFoundException var21) {
            var21.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException var20) {
                var20.printStackTrace();
            }

            try {
                if (fos != null) {
                    fos.flush();
                }
            } catch (IOException var19) {
                var19.printStackTrace();
            }

            try {
                fos.close();
            } catch (IOException var18) {
                var18.printStackTrace();
            }

        }

        if (delete) {
            file.delete();
        }

    }

    public static void decompress(File in, File out) {
        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            fis = new FileInputStream(in);
            fos = new FileOutputStream(out);
            decompress((InputStream) fis, (OutputStream) fos);
        } catch (FileNotFoundException var21) {
            var21.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException var20) {
                var20.printStackTrace();
            }

            try {
                if (fos != null) {
                    fos.flush();
                }
            } catch (IOException var19) {
                var19.printStackTrace();
            }

            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException var18) {
                var18.printStackTrace();
            }

        }

    }

    public static void decompress(InputStream is, OutputStream os) {
        GZIPInputStream gis = null;

        try {
            gis = new GZIPInputStream(is);
            byte[] data = new byte[1024];

            int e;
            while ((e = gis.read(data, 0, 1024)) != -1) {
                os.write(data, 0, e);
            }
        } catch (IOException var13) {
            var13.printStackTrace();
        } finally {
            try {
                if (gis != null) {
                    gis.close();
                }
            } catch (IOException var12) {
                var12.printStackTrace();
            }

        }

    }

    public static void decompress(String path) {
        decompress(path, true);
    }

    public static void decompress(String path, boolean delete) {
        File file = new File(path);
        decompress(file, delete);
    }

    public static void unZipFiles(String zipPath, String descDir) throws IOException {
        unZipFiles(new File(zipPath), descDir);
    }

    public static void unZipFiles(File zipFile, String descDir) throws IOException {
        File pathFile = new File(descDir);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }

        ZipFile zip = new ZipFile(zipFile);
        Enumeration entries = zip.entries();

        while (true) {
            ZipEntry entry;
            String outPath;
            do {
                if (!entries.hasMoreElements()) {
                    return;
                }

                entry = (ZipEntry) entries.nextElement();
                String zipEntryName = entry.getName();
                outPath = (descDir + File.separator + zipEntryName).replaceAll("\\*", "/");
                File file = new File(outPath.substring(0, outPath.lastIndexOf(47)));
                if (!file.exists()) {
                    file.mkdirs();
                }
            } while ((new File(outPath)).isDirectory());

            InputStream in = null;
            FileOutputStream out = null;

            try {
                in = zip.getInputStream(entry);
                out = new FileOutputStream(outPath);
                byte[] buf1 = new byte[1024];

                int len;
                while ((len = in.read(buf1)) > 0) {
                    out.write(buf1, 0, len);
                }
            } finally {
                if (in != null) {
                    in.close();
                }

                if (out != null) {
                    out.close();
                }

            }
        }
    }

    public static byte[] unZipFilesToByte(File zipFile, String fileName) throws IOException {
        ZipFile zip = new ZipFile(zipFile);
        Enumeration entries = zip.entries();

        ZipEntry entry;
        String zipEntryName;
        do {
            if (!entries.hasMoreElements()) {
                return null;
            }

            entry = (ZipEntry) entries.nextElement();
            zipEntryName = entry.getName();
        } while (!zipEntryName.equals(fileName));

        InputStream in = null;

        byte[] buffer;
        try {
            in = zip.getInputStream(entry);
            buffer = new byte[2097152];
            byte[] buf = new byte[1024];

            int len;
            for (int offset = 0; (len = in.read(buf)) >= 0; offset += len) {
                if (offset > buffer.length) {
                    throw new IOException("file max length 1024 * 1024 * 2");
                }

                System.arraycopy(buf, 0, buffer, offset, buf.length);
            }
        } finally {
            if (in != null) {
                in.close();
            }

        }

        return buffer;
    }

    public static String readZipFile(File zipFile, String fileName) throws IOException {
        BufferedReader reader = null;
        ZipInputStream zin = null;
        BufferedInputStream in = null;
        StringBuilder buffer = new StringBuilder();

        try {
            ZipFile file = new ZipFile(zipFile);
            in = new BufferedInputStream(new FileInputStream(zipFile));
            zin = new ZipInputStream(in);

            ZipEntry ze;
            while ((ze = zin.getNextEntry()) != null) {
                if (!ze.isDirectory() && ze.getName().equals(fileName)) {
                    long size = ze.getSize();
                    if (size > 0L) {
                        reader = new BufferedReader(new InputStreamReader(file.getInputStream(ze)));

                        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                            buffer.append(line);
                            buffer.append("\n");
                        }
                    }
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }

            if (zin != null) {
                zin.closeEntry();
            }

            if (in != null) {
                in.close();
            }

        }

        return buffer.toString();
    }
}
