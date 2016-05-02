package com.example.joeyhanlon.hydrawear;

/**
 * Created by joeyhanlon on 4/27/16.
 */

import android.content.Context;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class WearableAdapter extends WearableListView.Adapter {
    private final LayoutInflater mInflater;

    private ArrayList<String> modeNames;

    public WearableAdapter(Context context, ArrayList<String> mn) {
        mInflater = LayoutInflater.from(context);
        modeNames = mn;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(
            ViewGroup viewGroup, int i) {
        return new ItemViewHolder(mInflater.inflate(R.layout.list_item, null));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder viewHolder,
                                 int position) {
        ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
        CircledImageView circledView = itemViewHolder.mCircledImageView;
        circledView.setImageResource(R.drawable.circle);
        TextView textView = itemViewHolder.mItemTextView;
        textView.setText(modeNames.get(position));
    }

    @Override
    public int getItemCount() {
        return modeNames.size();
    }

    public void updateListItems(ArrayList<String> listItems) {
        modeNames = listItems;
        this.notifyDataSetChanged();
    }

    private static class ItemViewHolder extends WearableListView.ViewHolder {
        private CircledImageView mCircledImageView;
        private TextView mItemTextView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mCircledImageView = (CircledImageView) itemView.findViewById(R.id.circle);
            mItemTextView = (TextView) itemView.findViewById(R.id.name);
        }
    }
}