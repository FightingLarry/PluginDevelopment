package com.dh.baseactivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * FragmentActivity,用于包含Fragment
 *
 * @author yancai.liu
 */
public abstract class BaseFragmentActivity extends AppCompatActivity {

    private static final String TAG = "BaseFragmentActivity";

    protected String mFragmentClassName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        initializeStartingFragment();
    }


    protected int getLayoutResource() {
        return R.layout.baseacitvity_activity_main;
    }

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
            finish();
        }
        mFragmentClassName = fragment.getClass().getName();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "mFragmentClassName=" + mFragmentClassName);
        }
        FragmentUtils.replaceFragment(com.dh.baseactivity.R.id.layout_container_main, getSupportFragmentManager(),
                fragment, bundle);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fragment fragment =
                getSupportFragmentManager().findFragmentById(com.dh.baseactivity.R.id.layout_container_main);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Fragment fragment =
                getSupportFragmentManager().findFragmentById(com.dh.baseactivity.R.id.layout_container_main);
        if (fragment != null) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
