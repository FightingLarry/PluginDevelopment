package com.larry.light;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.larry.light.BuildConfig;
import com.larry.light.R;

/**
 * LightFragmentActivity,用于包含Fragment
 *
 * @author larry
 */
public abstract class LightFragmentActivity extends AppCompatActivity {

    private static final String TAG = "LightFragmentActivity";

    protected String mFragmentClassName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        initializeStartingFragment();
    }


    protected int getLayoutResource() {
        return R.layout.light_activity_main;
    }

    /**
     * Look at the method:{@link LightFragmentActivity#loadFragment}
     */
    protected abstract void initializeStartingFragment();

    protected void hideKeyboard() {
        InputMethodManager localInputMethodManager =
                (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        View currentFocus = this.getCurrentFocus();

        if (currentFocus != null) {
            IBinder localIBinder = this.getCurrentFocus().getWindowToken();
            localInputMethodManager.hideSoftInputFromWindow(localIBinder, 0);
        }
    }


    protected void loadFragment(Fragment fragment, Bundle bundle) {

        if (fragment == null) {
            return;
        }
        mFragmentClassName = fragment.getClass().getName();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "mFragmentClassName=" + mFragmentClassName);
        }
        LightFragmentUtils.replaceFragment(R.id.layout_container_main, getSupportFragmentManager(),
                fragment, bundle);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fragment fragment =
                getSupportFragmentManager().findFragmentById(R.id.layout_container_main);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Fragment fragment =
                getSupportFragmentManager().findFragmentById(R.id.layout_container_main);
        if (fragment != null) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
