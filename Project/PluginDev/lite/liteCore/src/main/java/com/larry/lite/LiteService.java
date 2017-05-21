package com.larry.lite;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.larry.lite.obtain.LiteConnections;
import com.larry.lite.network.NetworkHelper;
import com.larry.lite.obtain.LiteObtainAssetPlugin;
import com.larry.lite.utils.AndroidUtil;
import com.larry.lite.LiteLog.Logger;
import com.larry.lite.obtain.LiteObtainSdCardPlugin;
import com.larry.lite.obtain.LiteObtainRemotePlugin;
import com.larry.lite.network.NetworkSensor;
import com.larry.lite.utils.PrefsUtils;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LiteService extends GrayService {
    private static final int ALARM_INTERVAL = 300000;
    private static final int WAKE_REQUEST_CODE = 6666;
    private static final String LAST_VERSION_CODE = "dc_last_version_code";

    HandlerThread mIoThread;
    Handler mIoHandler;
    // Handler mUiHandler;
    private ILiteEngine mEngine;
    AlarmManager mAlarmManager;
    PendingIntent mAlarmOperation;
    private boolean isFirst;
    private Map<String, Object> mComponents;
    private final BroadcastReceiver mKeyEventReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                try {
                    LiteEvent event = LiteEvent.valueOf(action);
                    Message msgx;
                    if (event.equals(LiteEvent.KeyEventImmediate)) {
                        int id = intent.getIntExtra("plugin_id", -1);
                        if (id == 0) {
                            LiteLog.w("plugin id 0 was invalid!", new Object[0]);
                            return;
                        }
                        msgx = LiteService.this.mIoHandler.obtainMessage(2, id, 0);
                        msgx.sendToTarget();
                    } else if (event.equals(LiteEvent.KeyEventDebug)) {
                        boolean enabled = intent.getBooleanExtra("debug", false);
                        if (enabled) {
                            boolean print = intent.getBooleanExtra("print_config", false);
                            boolean update = intent.getBooleanExtra("update_config", false);
                            int flagx = 0;
                            int flag = flagx | (print ? 1 : 0);
                            flag |= update ? 2 : 0;
                            Message msgxx = LiteService.this.mIoHandler.obtainMessage(1, 0, flag);
                            LiteService.this.mIoHandler.sendMessageDelayed(msgxx, 10000L);
                        } else {
                            LiteService.this.mIoHandler.removeMessages(1);
                            msgx = LiteService.this.mIoHandler.obtainMessage(1, 1, 0);
                            msgx.sendToTarget();
                        }
                    } else if (LiteEvent.KeyEventDumpHPROF.equals(event)) {
                        Message msg = LiteService.this.mIoHandler.obtainMessage(3);
                        msg.sendToTarget();
                    }
                } catch (Exception var10) {
                    LiteLog.printStackTrace(var10);
                }

            }
        }
    };
    private static final int MSG_PERIODICITY = 1;
    private static final int MSG_IMMEDIATE = 2;
    private static final int MSG_DUMP = 3;
    private static final int PERIODICITY_TICK_TIME = 10000;
    private static final int CONFIG_FLAG_PRINT = 1;
    private static final int CONFIG_FLAG_UPDATE = 2;

    public LiteService() {}

    public static void pumpEvent(Context context, LiteEvent event) {
        LiteLog.d("LiteService->pumpEvent %s", new Object[] {event});
        Intent intent = new Intent(context, LiteService.class);
        intent.setAction(event.toString());
        context.startService(intent);
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        // this.mUiHandler = new Handler(this.getMainLooper());
        this.mIoThread = new HandlerThread("plugin-io");
        this.mIoThread.start();
        Looper looper = this.mIoThread.getLooper();
        this.mIoHandler = new LiteService.ServiceHandler(looper);
        this.mComponents = new HashMap();
        LiteParser parser = new LiteParser(this.getApplicationContext());
        List<LiteModule> configModules = parser.parse();
        if (configModules != null && configModules.size() > 0) {
            Iterator var4 = configModules.iterator();

            while (var4.hasNext()) {
                LiteModule module = (LiteModule) var4.next();
                module.registerComponents(this);
            }
        }

        Object logger = this.mComponents.get("logger");
        if (logger != null && logger instanceof Logger) {
            LiteLog.setLogger((Logger) logger);
        }

        Object sensor = this.mComponents.get("network");
        if (sensor != null && sensor instanceof NetworkSensor) {
            NetworkHelper.sharedHelper().registerNetworkSensor(this.getApplicationContext(), (NetworkSensor) sensor);
        } else {
            NetworkHelper.sharedHelper().registerNetworkSensor(this.getApplicationContext());
        }

        LiteLog.v("LiteService->onCreate", new Object[0]);
        LiteContext context = new LiteContext(this, this.mComponents);
        context.setIoLooper(looper);
        context.setConnectionFactory(new LiteConnections());
        if (configModules != null && configModules.size() > 0) {
            Iterator var7 = configModules.iterator();

            while (var7.hasNext()) {
                LiteModule module = (LiteModule) var7.next();
                module.applyOptions(this, context);
            }
        }

        this.onPluginContextCreated(context);

        // if (context.isLocalDebug()) {

        context.addConfigurationCrawler(new LiteObtainAssetPlugin(context));

        context.addConfigurationCrawler(new LiteObtainSdCardPlugin(context));
        // } else {
        context.addConfigurationCrawler(new LiteObtainRemotePlugin(context, context.getConnectionFactory()));
        // }

        Object crawler = this.mComponents.get("crawler");
        if (crawler != null && crawler instanceof ILiteObtainPlugin) {
            context.addConfigurationCrawler((ILiteObtainPlugin) crawler);
        }

        this.mEngine = new ILiteEngine(context);
        this.mEngine.setRunLoop(context.getIoLooper());

        this.mAlarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, LiteService.class);
        alarmIntent.setAction(LiteEvent.Periodicity.toString());
        PendingIntent operation = PendingIntent.getService(this, 6666, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        this.mAlarmOperation = operation;
        this.mAlarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), 900000L, operation);

        this.isFirst = true;
        IntentFilter filter = new IntentFilter();
        filter.addAction(LiteEvent.KeyEventImmediate.toString());
        filter.addAction(LiteEvent.KeyEventDebug.toString());
        filter.addAction(LiteEvent.KeyEventDumpHPROF.toString());
        this.registerReceiver(this.mKeyEventReceiver, filter);
    }

    public void registerComponent(String name, Object obj) {
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("name should valid for registerComponent!");
        } else if (obj == null) {
            this.mComponents.remove(name);
        } else {
            this.mComponents.put(name, obj);
        }
    }

    protected void onPluginContextCreated(LiteContext context) {}

    public void onDestroy() {
        LiteLog.v("LiteService->onDestroy", new Object[0]);
        super.onDestroy();
        this.unregisterReceiver(this.mKeyEventReceiver);
        this.mEngine.destroy();
        this.mIoThread.quit();
        this.mAlarmManager.cancel(this.mAlarmOperation);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        LiteLog.i("LiteService->onStartCommand", new Object[0]);
        boolean first = this.isFirst;
        if (this.isFirst) {
            this.isFirst = false;
        }

        LiteEvent event = LiteEvent.KeyEventStart;
        if (intent != null) {
            String action = intent.getAction();

            try {
                LiteLog.i("action: %s", new Object[] {action});
                event = LiteEvent.valueOf(action);
            } catch (Exception e) {
                ;
            }
        }

        if (LiteEvent.KeyEventStart.equals(event)) {
            int currentCode = AndroidUtil.getVersionCode(this);
            int code = PrefsUtils.getInt(this, LAST_VERSION_CODE, 0);
            if (code > 0 && currentCode > code) {
                LiteLog.i("upgrade first start!", new Object[0]);
                event = LiteEvent.KeyEventUpgrade;
                PrefsUtils.saveInt(this, LAST_VERSION_CODE, currentCode);
            } else if (code == 0) {
                PrefsUtils.saveInt(this, LAST_VERSION_CODE, currentCode);
            }
        }

        if (first) {
            this.mEngine.init(event);
        } else {
            this.mEngine.pumpEvent(event, (Object) null);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void printConfiguration() {
        if (this.mEngine != null) {
            this.mEngine.printConfiguration();
        }

    }

    public static void dumpHprof(Context context) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String date = sdf.format(new Date(System.currentTimeMillis()));

        try {
            File dir = context.getExternalFilesDir("hprof");
            String name = date + "#.hprof";
            File file = new File(dir, name);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            LiteLog.d("dump hprof to %s", new Object[] {file});
            Debug.dumpHprofData(file.getAbsolutePath());
        } catch (Throwable var6) {
            LiteLog.d("fail to dump hprof %s", new Object[] {var6.getMessage()});
        }

    }

    private class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PERIODICITY:
                    LiteLog.i("LiteService alive Periodicity", new Object[0]);
                    if (msg.arg1 > 0) {
                        this.removeMessages(1);
                        return;
                    }

                    if (msg.arg2 != 0) {
                        if ((msg.arg2 & 1) != 0) {
                            LiteService.this.printConfiguration();
                        }

                        if ((msg.arg2 & 2) != 0) {
                            LiteService.this.mEngine.updateConfiguration(true);
                        }
                    }

                    LiteService.this.mEngine.pumpEvent(LiteEvent.Periodicity, (Object) null);
                    this.sendEmptyMessageDelayed(1, 10000L);
                    break;
                case MSG_IMMEDIATE:
                    LiteLog.i("LiteService immediately start plugin %d", new Object[] {Integer.valueOf(msg.arg1)});
                    LiteService.this.mEngine.pumpEvent(LiteEvent.KeyEventImmediate, Integer.valueOf(msg.arg1));
                    break;
                case MSG_DUMP:
                    LiteLog.i("LiteService dump hprof start...", new Object[0]);
                    LiteService.dumpHprof(LiteService.this.getApplicationContext());
                    LiteLog.i("LiteService dump hprof end.", new Object[0]);
            }

        }
    }
}
