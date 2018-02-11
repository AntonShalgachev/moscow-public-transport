package com.shalgachev.moscowpublictransport.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.activities.ScheduleActivity;
import com.shalgachev.moscowpublictransport.data.ScheduleUtils;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.helpers.ExtraHelper;

import java.util.ArrayList;
import java.util.List;

public class SavedStopRecyclerViewAdapter extends SelectableAdapter<SavedStopRecyclerViewAdapter.ViewHolder> {

    static private final String LOG_TAG = "SavedStopAdapter";

    private List<Stop> mItems;
    private ViewHolder.ItemIterationListener mListener;
    private final Context mContext;

    public SavedStopRecyclerViewAdapter(ViewHolder.ItemIterationListener listener, Context context) {
        mListener = listener;
        mContext = context;
        mItems = new ArrayList<>();
    }

    public void updateStops(final List<Stop> items) {
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return mItems.size();
            }

            @Override
            public int getNewListSize() {
                return items.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return mItems.get(oldItemPosition).equals(items.get(newItemPosition));
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return areItemsTheSame(oldItemPosition, newItemPosition);
            }
        });

        mItems = new ArrayList<>(items);
        diffResult.dispatchUpdatesTo(this);
    }

    public List<Stop> getSelectedStops() {
        List<Stop> selectedStops = new ArrayList<>();

        if (isSelectingEnabled()) {
            List<Integer> selectedPositions = getSelectedItems();
            for (int position : selectedPositions) {
                selectedStops.add(mItems.get(position));
            }
        }

        return selectedStops;
    }

//    public void onSelectedRemoved() {
//        if (isSelectingEnabled()) {
//            List<Integer> selectedPositions = getSelectedItems();
//            for (int position : selectedPositions) {
//                notifyItemRemoved(position);
//            }
//        }
//    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_saved_stop, parent, false);
        return new ViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Stop stop = mItems.get(position);

        holder.item = stop;
        holder.mRouteView.setText(stop.route);
        holder.mDaysView.setText(ScheduleUtils.daysMaskToString(mContext, stop.daysMask, true));
        holder.mNameView.setText(stop.name);
        holder.mDirectionView.setText(mContext.getString(R.string.saved_stop_direction, stop.direction.getFrom(), stop.direction.getTo()));

        holder.mIsSelectedView.setVisibility(isSelectingEnabled() ? View.VISIBLE : View.INVISIBLE);
        holder.mIsSelectedView.setChecked(isSelected(position));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView mRouteView;
        public final TextView mDaysView;
        public final TextView mNameView;
        public final TextView mDirectionView;
        public final CheckBox mIsSelectedView;
        public Stop item;

        ItemIterationListener mListener;

        public ViewHolder(View view, ItemIterationListener listener) {
            super(view);
            this.view = view;
            mRouteView = view.findViewById(R.id.route);
            mDaysView = view.findViewById(R.id.days);
            mNameView = view.findViewById(R.id.name);
            mDirectionView = view.findViewById(R.id.direction);
            mIsSelectedView = view.findViewById(R.id.is_checked);

            mListener = listener;

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleClick();
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    handleLongClick();
                    return true;
                }
            });
        }

        void handleClick() {
            if (mListener != null)
                mListener.onItemClicked(item, getAdapterPosition());
        }

        void handleLongClick() {
            if (mListener != null)
                mListener.onItemLongClicked(item, getAdapterPosition());
        }

        public interface ItemIterationListener {
            void onItemClicked(Stop stop, int position);
            void onItemLongClicked(Stop stop, int position);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + item.toString() + "'";
        }
    }
}
