
package com.larry.lite;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
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

import com.larry.lite.obtain.Connections;
import com.larry.lite.network.NetworkHelper;
import com.larry.lite.utils.AndroidUtil;
import com.larry.lite.PLog.Logger;
import com.larry.lite.obtain.ObtainSdCardPlugin;
import com.larry.lite.obtain.ObtainRemotePlugin;
import com.larry.lite.network.NetworkSensor;
import com.larry.lite.utils.PrefsUtils;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PluginService extends GrayService {
    private static final int ALARM_INTERVAL = 300000;
    private static final int WAKE_REQUEST_CODE = 6666;
    private static final String LAST_VERSION_CODE = "dc_last_version_code";

    HandlerThread mIoThread;
    Handler mIoHandler;
    // Handler mUiHandler;
    private PluginEngine mEngine;
    AlarmManager mAlarmManager;
    PendingIntent mAlarmOperation;
    private boolean isFirst;
    private Map<String, Object> mComponents;
    private final BroadcastReceiver mKeyEventReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                try {
                    TriggerEvent event = TriggerEvent.valueOf(action);
                    Message msgx;
                    if (event.equals(TriggerEvent.KeyEventImmediate)) {
                        int id = intent.getIntExtra("plugin_id", -1);
                        if (id == 0) {
                            PLog.w("plugin id 0 was invalid!", new Object[0]);
                            return;
                        }
                        msgx = PluginService.this.mIoHandler.obtainMessage(2, id, 0);
                        msgx.sendToTarget();
                    } else if (event.equals(TriggerEvent.KeyEventDebug)) {
                        boolean enabled = intent.getBooleanExtra("debug", false);
                        if (enabled) {
                            boolean print = intent.getBooleanExtra("print_config", false);
                            boolean update = intent.getBooleanExtra("update_config", false);
                            int flagx = 0;
                            int flag = flagx | (print ? 1 : 0);
                            flag |= update ? 2 : 0;
                            Message msgxx = PluginService.this.mIoHandler.obtainMessage(1, 0, flag);
                            PluginService.this.mIoHandler.sendMessageDelayed(msgxx, 10000L);
                        } else {
                            PluginService.this.mIoHandler.removeMessages(1);
                            msgx = PluginService.this.mIoHandler.obtainMessage(1, 1, 0);
                            msgx.sendToTarget();
                        }
                    } else if (TriggerEvent.KeyEventDumpHPROF.equals(event)) {
                        Message msg = PluginService.this.mIoHandler.obtainMessage(3);
                        msg.sendToTarget();
                    }
                } catch (Exception var10) {
                    PLog.printStackTrace(var10);
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

    public PluginService() {}

    public static void pumpEvent(Context context, TriggerEvent event) {
        PLog.d("PluginService->pumpEvent %s", new Object[] {event});
        Intent intent = new Intent(context, PluginService.class);
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
        this.mIoHandler = new PluginService.ServiceHandler(looper);
        this.mComponents = new HashMap();
        ManifestParser parser = new ManifestParser(this.getApplicationContext());
        List<PluginModule> configModules = parser.parse();
        if (configModules != null && configModules.size() > 0) {
            Iterator var4 = configModules.iterator();

            while (var4.hasNext()) {
                PluginModule module = (PluginModule) var4.next();
                module.registerComponents(this);
            }
        }

        Object logger = this.mComponents.get("logger");
        if (logger != null && logger instanceof Logger) {
            PLog.setLogger((Logger) logger);
        }

        Object sensor = this.mComponents.get("network");
        if (sensor != null && sensor instanceof NetworkSensor) {
            NetworkHelper.sharedHelper().registerNetworkSensor(this.getApplicationContext(), (NetworkSensor) sensor);
        } else {
            NetworkHelper.sharedHelper().registerNetworkSensor(this.getApplicationContext());
        }

        PLog.v("PluginService->onCreate", new Object[0]);
        PluginContext context = new PluginContext(this, this.mComponents);
        context.setIoLooper(looper);
        context.setConnectionFactory(new Connections());
        if (configModules != null && configModules.size() > 0) {
            Iterator var7 = configModules.iterator();

            while (var7.hasNext()) {
                PluginModule module = (PluginModule) var7.next();
                module.applyOptions(this, context);
            }
        }

        this.onPluginContextCreated(context);
        if (context.isLocalDebug()) {
            context.setConfigurationCrawler(new ObtainSdCardPlugin(context));
        } else {
            context.setConfigurationCrawler(new ObtainRemotePlugin(context, context.getConnectionFactory()));
        }

        Object crawler = this.mComponents.get("crawler");
        if (crawler != null && crawler instanceof ConfigurationCrawler) {
            context.setConfigurationCrawler((ConfigurationCrawler) crawler);
        }

        this.mEngine = new PluginEngine(context);
        this.mEngine.setRunLoop(context.getIoLooper());
        this.mAlarmManager = (AlarmManager) this.getSystemService("alarm");
        Intent alarmIntent = new Intent(this, PluginService.class);
        alarmIntent.setAction(TriggerEvent.Periodicity.toString());
        PendingIntent operation = PendingIntent.getService(this, 6666, alarmIntent, 134217728);
        this.mAlarmOperation = operation;
        this.mAlarmManager.setInexactRepeating(0, System.currentTimeMillis(), 900000L, operation);
        this.isFirst = true;
        IntentFilter filter = new IntentFilter();
        filter.addAction(TriggerEvent.KeyEventImmediate.toString());
        filter.addAction(TriggerEvent.KeyEventDebug.toString());
        filter.addAction(TriggerEvent.KeyEventDumpHPROF.toString());
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

    protected void onPluginContextCreated(PluginContext context) {}

    public void onDestroy() {
        PLog.v("PluginService->onDestroy", new Object[0]);
        super.onDestroy();
        this.unregisterReceiver(this.mKeyEventReceiver);
        this.mEngine.destroy();
        this.mIoThread.quit();
        this.mAlarmManager.cancel(this.mAlarmOperation);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        PLog.i("PluginService->onStartCommand", new Object[0]);
        boolean first = this.isFirst;
        if (this.isFirst) {
            this.isFirst = false;
        }

        TriggerEvent event = TriggerEvent.KeyEventStart;
        if (intent != null) {
            String action = intent.getAction();

            try {
                PLog.i("action: %s", new Object[] {action});
                event = TriggerEvent.valueOf(action);
            } catch (Exception e) {
                ;
            }
        }

        if (TriggerEvent.KeyEventStart.equals(event)) {
            int currentCode = AndroidUtil.getVersionCode(this);
            int code = PrefsUtils.getInt(this, LAST_VERSION_CODE, 0);
            if (code > 0 && currentCode > code) {
                PLog.i("upgrade first start!", new Object[0]);
                event = TriggerEvent.KeyEventUpgrade;
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

            PLog.d("dump hprof to %s", new Object[] {file});
            Debug.dumpHprofData(file.getAbsolutePath());
        } catch (Throwable var6) {
            PLog.d("fail to dump hprof %s", new Object[] {var6.getMessage()});
        }

    }

    private class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    PLog.i("PluginService alive Periodicity", new Object[0]);
                    if (msg.arg1 > 0) {
                        this.removeMessages(1);
                        return;
                    }

                    if (msg.arg2 != 0) {
                        if ((msg.arg2 & 1) != 0) {
                            PluginService.this.printConfiguration();
                        }

                        if ((msg.arg2 & 2) != 0) {
                            PluginService.this.mEngine.updateConfiguration(true);
                        }
                    }

                    PluginService.this.mEngine.pumpEvent(TriggerEvent.Periodicity, (Object) null);
                    this.sendEmptyMessageDelayed(1, 10000L);
                    break;
                case 2:
                    PLog.i("PluginService immediately start plugin %d", new Object[] {Integer.valueOf(msg.arg1)});
                    PluginService.this.mEngine.pumpEvent(TriggerEvent.KeyEventImmediate, Integer.valueOf(msg.arg1));
                    break;
                case 3:
                    PLog.i("PluginService dump hprof start...", new Object[0]);
                    PluginService.dumpHprof(PluginService.this.getApplicationContext());
                    PLog.i("PluginService dump hprof end.", new Object[0]);
            }

        }
    }
}
