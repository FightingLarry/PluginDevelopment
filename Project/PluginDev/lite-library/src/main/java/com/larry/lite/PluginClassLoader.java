// package com.larry.lite;
//
// import dalvik.system.DexClassLoader;
// import android.content.Context;
// import android.text.TextUtils;
// import android.util.Log;
//
// import com.larry.lite.utils.GZipUtils;
//
// import org.json.JSONException;
// import org.json.JSONObject;
//
// import java.io.File;
//
// public class PluginClassLoader {
//
// private DexClassLoader mDexClassLoader;
// private PluginClassLoader.Manifest mManifest = null;
// private Context mContext;
//
// public PluginClassLoader(Context context ,ClassLoader parent) {
// super(parent);
// this.mContext = context;
// this.init(mContext.getApplicationContext(), stub, parent);
// }
//
// private void init(Context context, ClassLoader parent) {
// this.context = context;
// File dir = context.getExternalCacheDir();
// if (dir == null) {
// dir = context.getCacheDir();
// }
//
// String name = String.valueOf(stub.id);
// if (this.checkExecuteFile(dir, name)) {
// this.unzipFile(stub.path, name, dir);
// }
//
// this.parseManifest(dir, name);
// File optimizedDir = context.getDir(name, 0);
// String dex = (new File(dir, name + "/classes.dex")).getAbsolutePath();
// this.mDexClassLoader = new DexClassLoader(dex, optimizedDir.getAbsolutePath(), (String) null,
// parent);
// }
//
// public Class<?> loadClass(String className) throws ClassNotFoundException {
// return this.mDexClassLoader.loadClass(className);
// }
//
// private boolean checkExecuteFile(File dir, String name) {
// File destDir = new File(dir, name);
// return !(new File(destDir, "classes.dex")).exists() ? true : !(new File(destDir,
// "manifest.json")).exists();
// }
//
// private void unzipFile(String path, String name, File dir) throws PluginException {
// File file = new File(path);
// File outFile = new File(dir, name);
// if (!outFile.exists()) {
// outFile.mkdirs();
// }
//
// try {
// GZipUtils.unZipFiles(file.getAbsolutePath(), outFile.getAbsolutePath());
// } catch (Exception var7) {
// this.printLog(1);
// throw new PluginException(6, "unzip tdp file exception", var7);
// }
// }
//
// private void parseManifest(File dir, String name) throws PluginException {
// File manifestFile = new File(dir, name + "/manifest.json");
// if (!manifestFile.exists()) {
// throw new PluginException(8, "manifest.json not exists");
// } else {
// try {
// String e = FileUtils.readString(manifestFile);
// PLog.d("parseManifestFromPath : " + e, new Object[0]);
// this.mManifest = parse(e);
// } catch (Exception var5) {
// this.printLog(2);
// throw new PluginException(9, "manifest.json read fail", var5);
// }
// }
// }
//
// private static PluginClassLoader.Manifest parseManifestFromPath(File file) throws PluginException
// {
// try {
// String e = GZipUtils.readZipFile(file, "manifest.json");
// Log.d("parseManifestFromPath : " + e, new Object[0]);
// PluginClassLoader.Manifest manifest = parse(e);
// return manifest;
// } catch (Exception var3) {
// throw new PluginException(6, "unzip manifest.json fail", var3);
// }
// }
//
// static PluginClassLoader.Manifest parse(String jsonString) throws PluginException {
// JSONObject jsonObject;
// try {
// jsonObject = new JSONObject(jsonString);
// } catch (JSONException var3) {
// throw new PluginException(7, "String to JSONObject error", var3);
// }
//
// PluginClassLoader.Manifest manifest = new PluginClassLoader.Manifest();
// manifest.plugin = jsonObject.optString("plugin");
// manifest.launch = jsonObject.optString("launch");
// manifest.launchParam = jsonObject.optString("launchParam");
// manifest.limit = jsonObject.optInt("limit");
// manifest.name = jsonObject.optString("name");
// manifest.network = jsonObject.optString("network");
// verificationManifest(manifest);
// return manifest;
// }
//
// public static void verificationManifest(File file) throws PluginException {
// parseManifestFromPath(file);
// }
//
// private static void verificationManifest(PluginClassLoader.Manifest manifest) throws
// PluginException {
// if (StringUtils.isNull(manifest.plugin)) {
// throw new PluginException(10, "manifest plugin is null");
// } else if (StringUtils.isNull(manifest.launch)) {
// throw new PluginException(11, "manifest launch is null");
// } else {
// if (manifest.launch.equals(LaunchMode.Periodicity.toString())) {
// int modeExtra = Integer.parseInt(manifest.launchParam);
// switch (modeExtra) {
// case 1:
// case 12:
// case 24:
// case 168:
// break;
// default:
// throw new PluginException(13,
// "launchParam " + modeExtra + " not match for mode " + manifest.launch);
// }
// } else {
// if (!manifest.launch.equals(LaunchMode.KeyEvent.toString())) {
// throw new PluginException(12, "Unsupported mode " + manifest.launch);
// }
//
// manifest.launchParam = manifest.launchParam.toLowerCase();
// if (!TextUtils.equals("start", manifest.launchParam)
// && !TextUtils.equals("background", manifest.launchParam)
// && !TextUtils.equals("upgrade", manifest.launchParam)) {
// throw new PluginException(13,
// "launchParam " + manifest.launchParam + " not match for mode " + manifest.launch);
// }
// }
//
// }
// }
//
// private void printLog(int status) {
// PluginLogger.reportDownloaded(this.pluginContext, this.stub.id, this.stub.md5, "install", status,
// "");
// }
//
// public DCPlugin loadPlugin() throws ClassNotFoundException, IllegalAccessException,
// InstantiationException {
// String clsName = this.mManifest.plugin;
// Class clz = this.loadClass(clsName);
// DCPlugin plugin = (DCPlugin) clz.newInstance();
// return plugin;
// }
//
// public PluginClassLoader.Manifest getManifest() {
// return this.mManifest;
// }
//
// public static class Manifest {
// public String plugin;
// public String name;
// public String launch;
// public String launchParam;
// public int limit;
// public String network;
//
// public Manifest() {}
// }
// }
