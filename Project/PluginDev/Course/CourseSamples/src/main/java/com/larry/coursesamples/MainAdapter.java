package com.larry.coursesamples;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.larry.light.IAdapterListener;
import com.larry.light.LightAdapter;

/**
 * Created by Larry on 2017/4/2.
 */

public class MainAdapter extends LightAdapter<MainInfo> {

    private IAdapterListener adapterListener;



    protected MainAdapter(Context context) {
        super(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (MainItemType.Line.ordinal() == viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_main_line, parent, false);
            return new MainLineViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_main_item, parent, false);
            return new MainViewHolder(view);
        }

    }


    @Override
    public int getItemViewType(int position) {
        return getItem(position).getMainItemType().ordinal();
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        final MainInfo info = getItem(position);

        if (MainItemType.Line == getItem(position).getMainItemType()) {

            MainLineViewHolder mainLineViewHolder = (MainLineViewHolder) holder;
            mainLineViewHolder.tvLine.setText(info.getContext());


        } else {
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

    class MainLineViewHolder extends RecyclerView.ViewHolder {

        private TextView tvLine;

        MainLineViewHolder(View itemView) {
            super(itemView);
            tvLine = (TextView) itemView.findViewById(R.id.tvLine);
        }
    }
}
