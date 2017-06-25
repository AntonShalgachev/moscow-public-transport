package com.shalgachev.moscowpublictransport.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.data.StopListItem;
import com.shalgachev.moscowpublictransport.fragments.StopListFragment.OnListFragmentInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link StopListItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyStopRecyclerViewAdapter extends RecyclerView.Adapter<MyStopRecyclerViewAdapter.ViewHolder> {

    private final List<StopListItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyStopRecyclerViewAdapter(List<StopListItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_stop_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.stopListItem = mValues.get(position);
        holder.stop.setText(mValues.get(position).stop.name);
        holder.nextIn.setText(mValues.get(position).next);
        holder.checkBox.setChecked(mValues.get(position).selected);

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.stopListItem);
                }

                holder.stopListItem.selected = !holder.stopListItem.selected;
                holder.checkBox.setChecked(holder.stopListItem.selected);
            }
        });

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                holder.stopListItem.selected = isChecked;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView stop;
        public final TextView nextIn;
        public final CheckBox checkBox;
        public StopListItem stopListItem;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            stop = (TextView) view.findViewById(R.id.stop_item_name);
            nextIn = (TextView) view.findViewById(R.id.stop_item_next);
            checkBox = (CheckBox) view.findViewById(R.id.stop_item_checkbox);
        }
    }
}
