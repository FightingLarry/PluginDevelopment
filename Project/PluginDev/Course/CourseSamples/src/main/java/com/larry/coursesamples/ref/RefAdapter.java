package com.larry.coursesamples.ref;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.larry.coursesamples.MainInfo;
import com.larry.coursesamples.MainItemType;
import com.larry.coursesamples.R;
import com.larry.light.IAdapterListener;
import com.larry.light.LightAdapter;

/**
 * Created by Larry on 2017/4/2.
 */

public class RefAdapter extends LightAdapter<RefInfo> {

    private IAdapterListener adapterListener;


    protected RefAdapter(Context context) {
        super(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_main_item, parent, false);
        return new MainViewHolder(view);

    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        final RefInfo info = getItem(position);

        MainViewHolder mainViewHolder = (MainViewHolder) holder;
        mainViewHolder.tvContext.setText(info.getContext());
        mainViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapterListener != null) {
                    adapterListener.onItemClick(v, info, position);
                }
            }
        });

    }

    public void setAdapterListener(IAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    class MainViewHolder extends RecyclerView.ViewHolder {

        private View itemView;
        private TextView tvContext;

        MainViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;
            tvContext = (TextView) itemView.findViewById(R.id.tvContent);
        }
    }

}
