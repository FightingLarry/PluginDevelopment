package com.larry.light;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

public abstract class LightFragment extends Fragment {

    private ViewGroup rootView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initActionBar();
    }

    private void initActionBar() {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (getTitle() != 0) {
            actionBar.setTitle(getTitle());
        }
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getLayoutResource() != 0) {
            rootView = (ViewGroup) inflater.inflate(getLayoutResource(), null);
            super.onCreateView(inflater, container, savedInstanceState);
            return rootView;
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    protected ViewGroup getRootView() {
        return rootView;
    }

    protected abstract int getLayoutResource();

    public abstract int getTitle();

    public boolean isShowBack() {
        return false;
    }

    protected void showKeyboard() {
        ((InputMethodManager) getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                .toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.SHOW_IMPLICIT);
    }

    protected void hideKeyboard() {
        if (getView() != null) {
            ((InputMethodManager) getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }

    protected boolean hasDownload() {
        return false;
    }

    private String getClickSimpleName() {
        return getClass().getSimpleName();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        hideKeyboard();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean getUserVisibleHint() {
        return super.getUserVisibleHint();
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

}
