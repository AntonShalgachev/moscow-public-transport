package com.shalgachev.moscowpublictransport.adapters;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.data.Schedule;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by anton on 3/17/2018.
 */

public class ScheduleHoursAdapter extends RecyclerView.Adapter<ScheduleHoursAdapter.ViewHolder> {

    private Context mContext;
    private Schedule mSchedule;
    private Schedule.Timepoints mTimepoints;

    public ScheduleHoursAdapter(Context context) {
        mContext = context;
    }

    public void setSchedule(Schedule schedule) {
        mSchedule = schedule;
        mTimepoints = schedule.getTimepoints();

        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_schedule_hour_item, parent, false);

        return new ViewHolder(view, mContext);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int hour = mTimepoints.getNthHour(position);
        List<Integer> minutes = mTimepoints.getHoursMap().get(hour);
        holder.mHourView.setText(String.valueOf(hour));
        holder.mMinutesRecyclerView.setAdapter(new ScheduleMinutesAdapter(mSchedule, hour, minutes));
    }

    @Override
    public int getItemCount() {
        return mTimepoints != null ? mTimepoints.getHoursMap().size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private class SpacesItemDecoration extends RecyclerView.ItemDecoration {
            private int space;

            public SpacesItemDecoration(int space) {
                this.space = space;
            }

            @Override
            public void getItemOffsets(Rect outRect, View view,
                                       RecyclerView parent, RecyclerView.State state) {
                outRect.left = space;
                outRect.right = space;
                outRect.bottom = space / 2;
                outRect.top = space / 2;
            }
        }

        // TODO: 3/18/2018 increase elevation of future timepoints
        public View view;
        public TextView mHourView;
        public RecyclerView mMinutesRecyclerView;

        public ViewHolder(View view, Context context) {
            super(view);
            this.view = view;
            mHourView = view.findViewById(R.id.schedule_item_hour);
            mMinutesRecyclerView = view.findViewById(R.id.schedule_item_minutes_container);

            setupRecyclerView(context);
        }

        private void setupRecyclerView(Context context) {
            // TODO: 3/18/2018 change number of columns
            GridLayoutManager layoutManager = new GridLayoutManager(context, 8) {
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            };
            mMinutesRecyclerView.setLayoutManager(layoutManager);
            mMinutesRecyclerView.setNestedScrollingEnabled(false);

            SpacesItemDecoration decoration = new SpacesItemDecoration(context.getResources().getDimensionPixelSize(R.dimen.minutes_spacing));
            mMinutesRecyclerView.addItemDecoration(decoration);
        }
    }
}
