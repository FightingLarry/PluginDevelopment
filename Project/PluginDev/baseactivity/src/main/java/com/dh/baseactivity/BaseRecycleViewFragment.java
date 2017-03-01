package com.dh.baseactivity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

public abstract class BaseRecycleViewFragment extends BaseFragment {

    private static final boolean DEBUG = true;
    private static final String TAG = "BaseRecycleViewFragment";

    private XRecyclerView mRecyclerView;
    private View mFootView;
    protected View mNoResultLayout;
    protected TextView mNoResultTitle;
    protected TextView mNoResultDes;

    private int index = -1;
    private boolean clearOnAdd;

    private RecyclerView.LayoutManager mLayoutManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup view = getRootView();

        mRecyclerView = (XRecyclerView) view.findViewById(com.dh.baseactivity.R.id.recycler_view);
        mRecyclerView.setLayoutManager(getLayoutManager());
        mRecyclerView.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
        mRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallRotate);

        mRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                refresh();
            }

            @Override
            public void onLoadMore() {
                loadMore();
            }
        });

        mNoResultLayout = inflater.inflate(com.dh.baseactivity.R.layout.baseacitvity_fragment_normal_empty, null);
        mNoResultTitle = (TextView) mNoResultLayout.findViewById(com.dh.baseactivity.R.id.no_result_title);
        mNoResultDes = (TextView) mNoResultLayout.findViewById(com.dh.baseactivity.R.id.no_result_des);

        initViews(view);
        initAdapter();

        addHeaderViews(inflater);
        addFooterViews(inflater);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        readCacheOrExcuteRequest();
    }

    protected XRecyclerView getRecyclerView() {
        return mRecyclerView;
    }


    protected View getFootView() {
        return mFootView;
    }

    protected void hideLoadMoreView() {
        if (mFootView != null) {
            getFootView().setVisibility(View.GONE);
        }
    }

    protected void showLoadMoreView() {
        if (mFootView != null) {
            getFootView().setVisibility(View.VISIBLE);
        }
    }

    protected void addHeaderViews(LayoutInflater inflater) {

    }

    protected void addFooterViews(LayoutInflater inflater) {}


    protected void showNoResult() {
        if (getActivity() != null && mNoResultLayout != null) {
            if (!TextUtils.isEmpty(getNoResultTitle())) {
                mNoResultTitle.setText(getNoResultTitle());
            }
            mNoResultDes.setText(getNoResultDes());
            if (getAdapter().getItemCount() > 0) {
                mRecyclerView.setEmptyView(null);
            } else {
                mRecyclerView.setEmptyView(mNoResultLayout);
            }
        }
    }

    protected String getNoResultTitle() {
        if (getActivity() != null) {
            return getActivity().getString(R.string.no_result);
        }
        return "";
    }

    protected String getNoResultDes() {
        return "";
    }

    protected void initViews(View v) {}

    protected void initAdapter() {
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(getAdapter());
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.baseacitvity_fragment_normal_recycleview;
    }


    protected abstract RecyclerView.Adapter<?> getAdapter();

    // TODO
    protected RecyclerView.LayoutManager getLayoutManager() {
        if (mLayoutManager == null) {
            mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        }
        return mLayoutManager;
    }

    protected void readCacheOrExcuteRequest() {
        index = 0;
        clearOnAdd = true;
        constructAndPerformRequest(clearOnAdd, true, index);
    }

    protected void constructAndPerformRequest(boolean clearOnAdd, boolean readCache, int index) {
        if (DEBUG) {
            Log.d(TAG, "constructAndPerformRequest: index=" + index + " clearOnAdd=" + clearOnAdd + " readCache="
                    + readCache);
        }

        if (clearOnAdd) {
            setIndex(0);
        }

        if (DEBUG) {
            Log.d(TAG, "constructAndPerformRequest: index = " + index);
        }
    }

    protected void loadMore() {
        clearOnAdd = false;
        if (DEBUG) {
            Log.d(TAG, "loadMore: index=" + index + " clearOnAdd=" + clearOnAdd);
        }
        constructAndPerformRequest(clearOnAdd, false, index);
    }

    protected void refresh() {
        index = 0;
        clearOnAdd = true;
        getRecyclerView().setLoadingMoreEnabled(true);
        onLoadMoreComplete();

        if (DEBUG) {
            Log.d(TAG, "onRefresh: index=" + index + " clearOnAdd=" + clearOnAdd);
        }
        constructAndPerformRequest(clearOnAdd, false, index);
    }

    protected void onRefreshComplete() {
        mRecyclerView.refreshComplete();
    }

    protected void onLoadMoreComplete() {
        mRecyclerView.loadMoreComplete();
    }


    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isClearOnAdd() {
        return clearOnAdd;
    }

    public void setClearOnAdd(boolean clearOnAdd) {
        this.clearOnAdd = clearOnAdd;
    }

}
