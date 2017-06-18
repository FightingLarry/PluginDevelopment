package com.larry.lite.plugin1;

import android.content.Intent;
import android.util.Log;

import com.larry.lite.base.LiteConnectionFactory;
import com.larry.lite.base.LitePlugin;
import com.larry.lite.base.LitePluginPeer;

import java.io.IOException;


/**
 * Created by Larry on 2017/3/2.
 */
public class MainPlugin implements LitePlugin {

    private static final String TAG = "MainPlugin";

    @Override
    public void onCreate(LitePluginPeer litePluginPeer) {
        Log.d(TAG, "onCreate");
    }

    @Override
    public int execute(LitePluginPeer litePluginPeer, LiteConnectionFactory liteConnectionFactory) throws IOException {


        try {

            Intent intent = new Intent();
            intent.setClassName("com.larry.lite.host", "com.larry.lite.host.HostActivity");
            litePluginPeer.getContext().startActivity(intent);

            // 将启动的正确结果传给服务器
            String result = HttpHelp.post(liteConnectionFactory.getOkHttpClient(), "http://www.iamlarry.com", "{}");
            Log.d(TAG, "execute:" + result);

        } catch (Exception e) {
            e.printStackTrace();
        }



        return 0;
    }

    @Override
    public void onDestroy(LitePluginPeer litePluginPeer) {
        Log.d(TAG, "onDestroy");
    }

}
