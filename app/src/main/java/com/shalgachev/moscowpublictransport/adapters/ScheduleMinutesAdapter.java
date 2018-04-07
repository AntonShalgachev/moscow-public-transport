package com.shalgachev.moscowpublictransport.adapters;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.data.Schedule;
import com.shalgachev.moscowpublictransport.data.ScheduleUtils;
import com.shalgachev.moscowpublictransport.data.Timepoint;
import com.shalgachev.moscowpublictransport.helpers.AnimationHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by anton on 3/18/2018.
 */

public class ScheduleMinutesAdapter extends RecyclerView.Adapter<ScheduleMinutesAdapter.ViewHolder> {
    private static final String LOG_TAG = "ScheduleMinutesAdapter";
    private Schedule mSchedule;
    private int mHour;
    private List<Timepoint> mTimepoints;

    public ScheduleMinutesAdapter(Schedule schedule, int hour, List<Timepoint> minutes) {
        mSchedule = schedule;
        mHour = hour;
        mTimepoints = new ArrayList<>(minutes);
    }

    public void onDataUpdated() {
        Log.v(LOG_TAG, String.format("Updating data for hour %d", mHour));
        notifyItemRangeChanged(0, getItemCount(), new Object());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.v(LOG_TAG, String.format("Creating view holder for hour %d", mHour));
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_schedule_minute_item, parent, false);

        return new ViewHolder(view);
    }

    private String getCountdownText(Context context, Timepoint timepoint) {
        long diffInMinutes = timepoint.minutesFromNow();
        if (diffInMinutes > 0) {
            String intervalStr = ScheduleUtils.formatShortTimeInterval(context, diffInMinutes);
            return context.getString(R.string.schedule_next_in, intervalStr);
        } else if (diffInMinutes == 0) {
            return context.getString(R.string.schedule_now);
        } else {
            return context.getString(R.string.schedule_late);
        }
    }

    private int getCountdownColor(Context context, Timepoint timepoint) {
        long diffInMinutes = timepoint.minutesFromNow();

        // TODO: 3/21/2018 extract these values somewhere
        int closeThreshold = 5;
        int mediumThreshold = 10;

        @ColorRes int color;
        if (diffInMinutes < 0)
            color = R.color.next_in_late_color;
        else if (diffInMinutes <= closeThreshold)
            color = R.color.next_in_close_color;
        else if (diffInMinutes <= mediumThreshold)
            color = R.color.next_in_medium_color;
        else
            color = R.color.next_in_far_color;

        return context.getResources().getColor(color);
    }

    @Override
    public void onBindViewHolder(final @NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            // TODO: 3/23/2018 THIS IS COPY_PASTE, do smth about it
            Context context = holder.view.getContext();
            Timepoint timepoint = mTimepoints.get(position);

            int animDuration = context.getResources().getInteger(R.integer.schedule_minute_animation_duration);

            boolean isMinuteEnabled = timepoint.isEnabled();

            boolean isCountdownShown = timepoint.isCountdownShown;
            boolean wasCountdownShown = holder.mCountdownView.getAlpha() > 0.5f;

            String countdownText = getCountdownText(context, timepoint);
            int countdownColor = getCountdownColor(context, timepoint);

            if (!wasCountdownShown && isCountdownShown) {
                holder.mCountdownView.setTextColor(countdownColor);
                holder.mCountdownView.setText(countdownText);

                AnimationHelper.animateAlpha(holder.mCountdownView, 0.0f, 1.0f).setDuration(animDuration).start();

            } else if (wasCountdownShown && isCountdownShown) {
                int prevColor = holder.mCountdownView.getCurrentTextColor();
                if (prevColor != countdownColor) {
                    AnimationHelper.animateTextColor(holder.mCountdownView, prevColor, countdownColor).setDuration(animDuration).start();
                }

                holder.mCountdownView.setText(countdownText);
            } else if (wasCountdownShown && !isCountdownShown) {
                AnimationHelper.animateAlpha(holder.mCountdownView, 1.0f, 0.0f).setDuration(animDuration).start();
            }

            // TODO: 3/26/2018 Implement grid layout manager which supports different heights of items and set visibility to countdown view

            if (isMinuteEnabled && !holder.isEnabled)
                AnimationHelper.animateTextColor(holder.mMinuteView, holder.colorDisabled, holder.colorEnabled).setDuration(animDuration).start();
            else if (!isMinuteEnabled && holder.isEnabled)
                AnimationHelper.animateTextColor(holder.mMinuteView, holder.colorEnabled, holder.colorDisabled).setDuration(animDuration).start();
            holder.isEnabled = isMinuteEnabled;

            float enabledElevation = context.getResources().getDimensionPixelSize(R.dimen.minute_card_enabled_elevation);
            float disabledElevation = context.getResources().getDimensionPixelSize(R.dimen.minute_card_disabled_elevation);
            ObjectAnimator animator = ObjectAnimator.ofFloat(
                    holder.mCard, "cardElevation", holder.mCard.getCardElevation(),
                    isMinuteEnabled ? enabledElevation : disabledElevation);
            animator.setDuration(animDuration);
            animator.start();
        } else {
            onBindViewHolder(holder, position);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Context context = holder.view.getContext();

        Timepoint timepoint = mTimepoints.get(position);
        holder.mMinuteView.setText(String.format(Locale.US, "%02d", timepoint.minute));

        boolean isMinuteEnabled = timepoint.isEnabled();

        if (timepoint.isCountdownShown) {
            holder.mCountdownView.setText(getCountdownText(context, timepoint));
            holder.mCountdownView.setTextColor(getCountdownColor(context, timepoint));
            holder.mCountdownView.setAlpha(1.0f);
        } else {
            // TODO: 3/26/2018 warning antonsh
            holder.mCountdownView.setText("");
            holder.mCountdownView.setAlpha(0.0f);
        }

        // TODO: 3/26/2018 Implement grid layout manager which supports different heights of items and set visibility to countdown view

        holder.isEnabled = isMinuteEnabled;
        holder.mMinuteView.setTextColor(isMinuteEnabled ? holder.colorEnabled : holder.colorDisabled);

        float enabledElevation = context.getResources().getDimensionPixelSize(R.dimen.minute_card_enabled_elevation);
        float disabledElevation = context.getResources().getDimensionPixelSize(R.dimen.minute_card_disabled_elevation);
        holder.mCard.setCardElevation(isMinuteEnabled ? enabledElevation : disabledElevation);
    }

    @Override
    public int getItemCount() {
        return mTimepoints.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public CardView mCard;
        public TextView mMinuteView;
        public TextView mCountdownView;

        public boolean isEnabled;
        public int colorEnabled;
        public int colorDisabled;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            mCard = view.findViewById(R.id.schedule_item_minute_card);
            mMinuteView = view.findViewById(R.id.schedule_item_minute);
            mCountdownView = view.findViewById(R.id.schedule_item_countdown);

            ColorStateList colors = mMinuteView.getTextColors();
            colorEnabled = colors.getColorForState(new int[]{android.R.attr.state_enabled}, 0);
            colorDisabled = colors.getColorForState(new int[]{-android.R.attr.state_enabled}, 0);
        }
    }
}
