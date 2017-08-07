package com.shalgachev.moscowpublictransport.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.data.ScheduleUtils;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.fragments.SavedStopFragment.OnListFragmentInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Stop} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class SavedStopRecyclerViewAdapter extends RecyclerView.Adapter<SavedStopRecyclerViewAdapter.ViewHolder> {

    private final List<Stop> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final Context mContext;

    public SavedStopRecyclerViewAdapter(List<Stop> items, OnListFragmentInteractionListener listener, Context context) {
        mValues = items;
        mListener = listener;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_saved_stop, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Stop stop = mValues.get(position);
        holder.item = stop;
        holder.mRouteView.setText(stop.route);
        holder.mDaysView.setText(ScheduleUtils.daysMaskToString(mContext, stop.daysMask, true));
        holder.mNameView.setText(stop.name);
        holder.mDirectionView.setText(mContext.getString(R.string.saved_stop_direction, stop.direction.getFrom(), stop.direction.getTo()));

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
        public final TextView mRouteView;
        public final TextView mDaysView;
        public final TextView mNameView;
        public final TextView mDirectionView;
        public Stop item;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            mRouteView = (TextView) view.findViewById(R.id.route);
            mDaysView = (TextView) view.findViewById(R.id.days);
            mNameView = (TextView) view.findViewById(R.id.name);
            mDirectionView = (TextView) view.findViewById(R.id.direction);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + item.toString() + "'";
        }
    }
}
