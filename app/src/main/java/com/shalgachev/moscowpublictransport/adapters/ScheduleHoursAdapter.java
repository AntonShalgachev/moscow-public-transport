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

import java.util.List;

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
            private int mHorizontalSpace;
            private int mVerticalSpace;
            private int mVerticalOffset;
            private int mCols;

            public SpacesItemDecoration(int horizontalSpace, int verticalSpace, int verticalOffset, int cols) {
                this.mHorizontalSpace = horizontalSpace;
                this.mVerticalSpace = verticalSpace;
                this.mVerticalOffset = verticalOffset;
                this.mCols = cols;
            }

            @Override
            public void getItemOffsets(Rect outRect, View view,
                                       RecyclerView parent, RecyclerView.State state) {
                int rows = (parent.getAdapter().getItemCount() + mCols - 1) / mCols;
                int row = parent.getChildLayoutPosition(view) / mCols;

                outRect.left = mHorizontalSpace;
                outRect.right = mHorizontalSpace;
                outRect.bottom = mVerticalSpace;
                outRect.top = mVerticalSpace;

                if (row == 0)
                    outRect.top += mVerticalOffset;
                if (row == rows - 1)
                    outRect.bottom += mVerticalOffset;
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
            int columns = 8;
            GridLayoutManager layoutManager = new GridLayoutManager(context, columns) {
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            };
            mMinutesRecyclerView.setLayoutManager(layoutManager);
            mMinutesRecyclerView.setNestedScrollingEnabled(false);

            SpacesItemDecoration decoration = new SpacesItemDecoration(
                    context.getResources().getDimensionPixelSize(R.dimen.minutes_horizontal_spacing),
                    context.getResources().getDimensionPixelSize(R.dimen.minutes_vertical_spacing),
                    context.getResources().getDimensionPixelSize(R.dimen.minutes_vertical_offset),
                    columns);
            mMinutesRecyclerView.addItemDecoration(decoration);
        }
    }
}
