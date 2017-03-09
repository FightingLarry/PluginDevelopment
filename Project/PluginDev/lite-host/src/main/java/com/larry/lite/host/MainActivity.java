package com.larry.lite.host;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.larry.lite.LitePluginService;

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
                Intent intent = new Intent(LitePluginService.ACTION_CHECK_PLUGIN);
                intent.setClass(this, LitePluginService.class);
                startService(intent);
                break;
            case R.id.btn2:

                break;
        }
    }
}
