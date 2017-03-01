package com.dh.baseactivity;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

public class FragmentUtils {

    private static final String TAG = "FragmentUtils";

    public static final String ARGUMENTS_KEY_NO_BACK_STACK = "noBackStack";

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static void navigateToInNewActivity(Context context, Class<?> fragment, Bundle bundle, View v) {
        Intent intent = new Intent(context, ArbitraryFragmentActivity.class);
        if (bundle != null && bundle.getBoolean(ARGUMENTS_KEY_NO_BACK_STACK)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        }
        intent.putExtra(ArbitraryFragmentActivity.EXTRAS_FRAGMENT_CLASS_NAME, fragment.getName());

        if (v != null && hasJellyBean()) {
            int location[] = new int[2];
            v.getLocationOnScreen(location);
            ActivityOptions activityOptions =
                    ActivityOptions.makeScaleUpAnimation(v, location[0], location[1], v.getWidth(), v.getHeight());
            if (bundle == null) {
                bundle = new Bundle();
            }
            intent.putExtra(ArbitraryFragmentActivity.EXTRAS_BUNDLE, bundle);
            context.startActivity(intent, activityOptions.toBundle());
        } else {
            intent.putExtra(ArbitraryFragmentActivity.EXTRAS_BUNDLE, bundle);
            context.startActivity(intent);
        }
    }

    public static void navigateToInNewActivity(Context context, Class<?> fragment, Bundle bundle, View v,
            boolean isNewTask) {

        Intent intent = new Intent(context, ArbitraryFragmentActivity.class);

        if (bundle != null && bundle.getBoolean(ARGUMENTS_KEY_NO_BACK_STACK)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        }
        intent.putExtra(ArbitraryFragmentActivity.EXTRAS_FRAGMENT_CLASS_NAME, fragment.getName());
        if (isNewTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (v != null && hasJellyBean()) {
            int location[] = new int[2];
            v.getLocationOnScreen(location);
            ActivityOptions activityOptions =
                    ActivityOptions.makeScaleUpAnimation(v, location[0], location[1], v.getWidth(), v.getHeight());
            if (bundle == null) {
                bundle = new Bundle();
            }
            intent.putExtra(ArbitraryFragmentActivity.EXTRAS_BUNDLE, bundle);
            context.startActivity(intent, activityOptions.toBundle());
        } else {
            intent.putExtra(ArbitraryFragmentActivity.EXTRAS_BUNDLE, bundle);
            context.startActivity(intent);
        }
    }


    public static void replaceFragment(int fragmentId, FragmentManager fragmentManager, Fragment fragment,
            Bundle bundle) {

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        fragmentTransaction.replace(fragmentId, fragment, fragment.getClass().getName());
        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions();
    }


}
