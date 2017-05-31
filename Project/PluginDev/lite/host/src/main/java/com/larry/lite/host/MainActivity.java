package com.larry.lite.host;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.larry.lite.LiteEvent;
import com.larry.lite.LiteManager;
import com.larry.lite.LitePluginSDK;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn1;
    private Button btn2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn1 = (Button) findViewById(R.id.btn1);
        btn1.setOnClickListener(this);

        btn2 = (Button) findViewById(R.id.btn2);
        btn2.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn1:
                LitePluginSDK.pumpEvent(getApplicationContext(), LiteEvent.Periodicity);
                break;
            case R.id.btn2:
                LitePluginSDK.pumpEvent(getApplicationContext(), LiteEvent.KeyEventStart);
                break;
            case R.id.btn3:
                LitePluginSDK.pumpEvent(getApplicationContext(), LiteEvent.KeyEventUpgrade);
                break;
            case R.id.btn4:
                LitePluginSDK.pumpEvent(getApplicationContext(), LiteEvent.KeyEventBackground);
                break;
            case R.id.btn5:
                LitePluginSDK.pumpEvent(getApplicationContext(), LiteEvent.KeyEventImmediate);
                break;
            case R.id.btn6:
                LitePluginSDK.pumpEvent(getApplicationContext(), LiteEvent.KeyEventDebug);
                break;
            case R.id.btn7:
                LitePluginSDK.pumpEvent(getApplicationContext(), LiteEvent.KeyEventDumpHPROF);
                break;
        }
    }
}
