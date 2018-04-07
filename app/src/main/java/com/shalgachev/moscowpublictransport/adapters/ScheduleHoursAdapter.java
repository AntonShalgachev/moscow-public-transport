package com.shalgachev.moscowpublictransport.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.data.Schedule;
import com.shalgachev.moscowpublictransport.helpers.AnimationHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anton on 3/17/2018.
 */

public class ScheduleHoursAdapter extends RecyclerView.Adapter<ScheduleHoursAdapter.ViewHolder> {
    private static final String LOG_TAG = "ScheduleHoursAdapter";

    private Context mContext;
    private Schedule mSchedule;
    private Schedule.Timepoints mTimepoints;
    private RecyclerView.RecycledViewPool mMinutesPool;

    public ScheduleHoursAdapter(Context context, RecyclerView.RecycledViewPool minutesPool) {
        mContext = context;
        mMinutesPool = minutesPool;
    }

    public void updateSchedule(Schedule schedule, boolean animate) {
        mSchedule = schedule;
        mTimepoints = schedule.getTimepoints();

        // TODO: 3/22/2018 change me plz
        if (animate) {
            Log.d(LOG_TAG, "notifyItemRangeChanged");
            notifyItemRangeChanged(0, getItemCount(), new Object());
        } else {
            Log.d(LOG_TAG, "notifyDataSetChanged");
            notifyDataSetChanged();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_schedule_hour_item, parent, false);

        return new ViewHolder(view, mContext, mMinutesPool);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            Log.d(LOG_TAG, "Animating changes");
            holder.mAdapter.onDataUpdated();

            int hour = mTimepoints.getNthHour(position);
            List<Schedule.Timepoint> timepoints = mTimepoints.getHoursMap().get(hour);

            Context context = holder.view.getContext();

            // TODO: 3/23/2018 precompute this in the worker thread for performance
            boolean isEnabled = false;
            for (Schedule.Timepoint timepoint : timepoints)
                if (timepoint.isEnabled())
                    isEnabled = true;

            int animDuration = context.getResources().getInteger(R.integer.schedule_minute_animation_duration);

            if (isEnabled && !holder.isEnabled)
                AnimationHelper.animateTextColor(holder.mHourView, holder.colorDisabled, holder.colorEnabled).setDuration(animDuration).start();
            else if (!isEnabled && holder.isEnabled)
                AnimationHelper.animateTextColor(holder.mHourView, holder.colorEnabled, holder.colorDisabled).setDuration(animDuration).start();
            holder.isEnabled = isEnabled;
        } else {
            Log.d(LOG_TAG, "Recreating dataset from scratch");
            onBindViewHolder(holder, position);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int hour = mTimepoints.getNthHour(position);
        List<Schedule.Timepoint> timepoints = mTimepoints.getHoursMap().get(hour);
        holder.mHourView.setText(String.valueOf(hour));

        holder.mAdapter = new ScheduleMinutesAdapter(mSchedule, hour, timepoints);
        holder.mMinutesRecyclerView.setAdapter(holder.mAdapter);

        Context context = holder.view.getContext();

        // TODO: 3/23/2018 precompute this in the worker thread for performance
        boolean isEnabled = false;
        for (Schedule.Timepoint timepoint : timepoints)
            if (timepoint.isEnabled())
                isEnabled = true;

        float enabledElevation = context.getResources().getDimensionPixelSize(R.dimen.hour_card_enabled_elevation);
        float disabledElevation = context.getResources().getDimensionPixelSize(R.dimen.hour_card_disabled_elevation);
        holder.mCardView.setCardElevation(isEnabled ? enabledElevation : disabledElevation);

        holder.isEnabled = isEnabled;
        holder.mHourView.setTextColor(isEnabled ? holder.colorEnabled : holder.colorDisabled);
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
        public CardView mCardView;
        public TextView mHourView;
        public RecyclerView mMinutesRecyclerView;

        public boolean isEnabled;
        public int colorEnabled;
        public int colorDisabled;

        private ScheduleMinutesAdapter mAdapter;

        public ViewHolder(View view, Context context, RecyclerView.RecycledViewPool minutesPool) {
            super(view);
            this.view = view;
            mCardView = view.findViewById(R.id.schedule_item_hour_card);
            mHourView = view.findViewById(R.id.schedule_item_hour);
            mMinutesRecyclerView = view.findViewById(R.id.schedule_item_minutes_container);

            ColorStateList colors = mHourView.getTextColors();
            colorEnabled = colors.getColorForState(new int[]{android.R.attr.state_enabled}, 0);
            colorDisabled = colors.getColorForState(new int[]{-android.R.attr.state_enabled}, 0);

            setupRecyclerView(context, minutesPool);
        }

        private void setupRecyclerView(Context context, RecyclerView.RecycledViewPool minutesPool) {
            // TODO: 3/18/2018 change number of columns
            int columns = 7;
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
            mMinutesRecyclerView.setRecycledViewPool(minutesPool);
        }
    }
}
