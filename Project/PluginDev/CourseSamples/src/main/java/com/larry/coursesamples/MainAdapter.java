package com.larry.coursesamples;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.larry.light.LightAdapter;

/**
 * Created by Larry on 2017/4/2.
 */

public class MainAdapter extends LightAdapter<MainInfo> {

    protected MainAdapter(Context context) {
        super(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_main_item, parent, false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MainViewHolder mainViewHolder = (MainViewHolder) holder;
        MainInfo info = mList.get(position);

        mainViewHolder.tvContext.setText(info.getContext());
        // mainViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
        // @Override
        // public void onClick(View v) {
        //
        // }
        // });
    }

    class MainViewHolder extends RecyclerView.ViewHolder {

        private View itemView;
        private TextView tvContext;

        MainViewHolder(View itemView) {
            super(itemView);

            itemView = itemView;
            tvContext = (TextView) itemView.findViewById(R.id.tvContent);
        }
    }
}
