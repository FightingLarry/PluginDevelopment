package com.larry.coursesamples.proxy;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.larry.light.IAdapterListener;
import com.larry.light.LightAdapter;

/**
 * Created by Larry on 2017/4/23.
 */

public class ProxyAdapter extends LightAdapter<String> {

    private IAdapterListener listener;

    protected ProxyAdapter(Context context, IAdapterListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        TextView textView = new TextView(mContext);

        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        int padding = 50;
        textView.setPadding(padding, padding, padding, padding);

        RecyclerView.LayoutParams layoutParams =
                new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                        RecyclerView.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(layoutParams);

        return new MyViewHolder(textView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final String title = getItem(position);
        MyViewHolder viewHolder = (MyViewHolder) holder;
        viewHolder.textView.setText(title);
        viewHolder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(v, title, position);
                }
            }
        });

    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;

        public MyViewHolder(View itemView) {
            super(itemView);

            textView = (TextView) itemView;
        }

    }
}
