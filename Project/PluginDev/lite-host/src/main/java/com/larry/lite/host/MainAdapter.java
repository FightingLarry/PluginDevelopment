package com.larry.lite.host;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.larry.light.IAdapterListener;
import com.larry.light.LightAdapter;

/**
 * Created by Larry on 2017/3/2.
 */

public class MainAdapter extends LightAdapter<MainInfo> {


    private static final String TAG = "MainAdapter";

    private IAdapterListener mAdapterListener;

    protected MainAdapter(Context context, IAdapterListener listener) {
        super(context);
        this.mAdapterListener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_main_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final MainInfo model = mList.get(position);

        MyViewHolder myViewHolder = (MyViewHolder) holder;
        myViewHolder.mTextView.setText(model.getInfo());
        myViewHolder.mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mAdapterListener != null) {
                    mAdapterListener.onItemClick(view, model, position);
                }

            }
        });

    }


    private static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView mTextView;
        View mRootView;

        public MyViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.tvInfo);
            mRootView = itemView;
        }
    }


}
