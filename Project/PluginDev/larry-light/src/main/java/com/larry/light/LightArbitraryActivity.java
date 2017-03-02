package com.larry.light;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;

public class LightArbitraryActivity extends LightFragmentActivity {

    private static final String TAG = "LightArbitraryActivity";

    public static final String EXTRAS_BUNDLE = TAG + ".EXTRAS_BUNDLE";

    public static final String EXTRAS_FRAGMENT_CLASS_NAME = TAG + ".EXTRAS_FRAGMENT_CLASS_NAME";

    public static final String EXTRAS_TYPE = TAG + ".EXTRAS_TYPE";

    // 动画的相关字段
    public static final String ARGUMENTS_KEY_ACTIVITY_ANIMATION = "ACTIVITY_ANIMATION";
    public static final String ARGUMENTS_ENTER_ANIMATION = "LightArbitraryActivity.ARGUMENTS_ENTER_ANIMATION";
    public static final String ARGUMENTS_EXIT_ANIMATION = "LightArbitraryActivity.ARGUMENTS_EXIT_ANIMATION";

    private boolean mActivityAnimation;
    private int mEnterAnimationResId;
    private int mExitAnimationResId;

    private String className;
    private Bundle classBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle activityBundle = getIntent().getBundleExtra(EXTRAS_BUNDLE);
        if (activityBundle != null) {
            mActivityAnimation = activityBundle.getBoolean(ARGUMENTS_KEY_ACTIVITY_ANIMATION);
            mEnterAnimationResId = activityBundle.getInt(ARGUMENTS_ENTER_ANIMATION);
            mExitAnimationResId = activityBundle.getInt(ARGUMENTS_EXIT_ANIMATION);

        }
        if (mActivityAnimation && mEnterAnimationResId != 0) {
            overridePendingTransition(mEnterAnimationResId, R.anim.light_no_anim);
        }
        if (savedInstanceState != null) {
            className = savedInstanceState.getString(EXTRAS_FRAGMENT_CLASS_NAME);
            classBundle = savedInstanceState.getBundle(EXTRAS_BUNDLE);
        }
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(EXTRAS_FRAGMENT_CLASS_NAME, getIntent().getExtras().getString(EXTRAS_FRAGMENT_CLASS_NAME));
        outState.putBundle(EXTRAS_BUNDLE, getIntent().getExtras().getBundle(EXTRAS_BUNDLE));
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void initializeStartingFragment() {
        String className = null;
        Bundle bundle = null;
        if (getSupportFragmentManager().findFragmentById(R.id.layout_container_main) == null && getIntent() != null
                && getIntent().getExtras() != null) {
            className = getIntent().getExtras().getString(EXTRAS_FRAGMENT_CLASS_NAME);
            bundle = getIntent().getExtras().getBundle(EXTRAS_BUNDLE);
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "initializeStartingFragment(), className=" + className);
        }

        if (TextUtils.isEmpty(className)) {
            className = this.className;
            bundle = this.classBundle;
        }
        try {
            Fragment fragment = (Fragment) Class.forName(className).newInstance();
            loadFragment(fragment, bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        super.finish();
        if (mActivityAnimation && mExitAnimationResId != 0) {
            overridePendingTransition(R.anim.light_no_anim, mExitAnimationResId);
        }
    }

}
