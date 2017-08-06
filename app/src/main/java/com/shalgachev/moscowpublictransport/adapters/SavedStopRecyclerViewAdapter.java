package com.shalgachev.moscowpublictransport.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.fragments.SavedStopFragment.OnListFragmentInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Stop} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class SavedStopRecyclerViewAdapter extends RecyclerView.Adapter<SavedStopRecyclerViewAdapter.ViewHolder> {

    private final List<Stop> mValues;
    private final OnListFragmentInteractionListener mListener;

    public SavedStopRecyclerViewAdapter(List<Stop> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_saved_stop, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.item = mValues.get(position);
        holder.mIdView.setText("");
        holder.contentView.setText(mValues.get(position).toString());

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView mIdView;
        public final TextView contentView;
        public Stop item;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            contentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + contentView.getText() + "'";
        }
    }
}
