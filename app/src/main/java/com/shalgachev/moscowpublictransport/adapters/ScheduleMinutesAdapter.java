package com.shalgachev.moscowpublictransport.adapters;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shalgachev.moscowpublictransport.R;
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
    private int mHour;
    private List<Timepoint> mTimepoints;

    ScheduleMinutesAdapter(int hour, List<Timepoint> minutes) {
        mHour = hour;
        mTimepoints = new ArrayList<>(minutes);

        setHasStableIds(true);
    }

    public void onDataUpdated() {
        Log.v(LOG_TAG, String.format("Updating data for hour %d", mHour));
        notifyItemRangeChanged(0, getItemCount(), new Object());
    }

    @Override
    public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.v(LOG_TAG, String.format("Creating minutes view holder for hour %d, type %d", mHour, viewType));
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_schedule_minute_item, parent, false);

        return new ViewHolder(view);
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

            String countdownText = ScheduleUtils.getCountdownText(context, timepoint, true);
            int countdownColor = ScheduleUtils.getCountdownColor(context, timepoint);

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

            int disabledColor = holder.getColor(timepoint, false);
            int enabledColor = holder.getColor(timepoint, true);

            if (isMinuteEnabled && !holder.isEnabled)
                AnimationHelper.animateTextColor(holder.mMinuteView, disabledColor, enabledColor).setDuration(animDuration).start();
            else if (!isMinuteEnabled && holder.isEnabled)
                AnimationHelper.animateTextColor(holder.mMinuteView, enabledColor, disabledColor).setDuration(animDuration).start();
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
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Context context = holder.view.getContext();

        final Timepoint timepoint = mTimepoints.get(position);
        holder.mMinuteView.setText(String.format(Locale.US, "%02d", timepoint.minute));

        holder.mCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timepoint.note != null)
                    Snackbar.make(holder.view, timepoint.note, Snackbar.LENGTH_LONG).show();
            }
        });

        boolean isMinuteEnabled = timepoint.isEnabled();

        if (timepoint.isCountdownShown) {
            holder.mCountdownView.setText(ScheduleUtils.getCountdownText(context, timepoint, true));
            holder.mCountdownView.setTextColor(ScheduleUtils.getCountdownColor(context, timepoint));
            holder.mCountdownView.setAlpha(1.0f);
        } else {
            // TODO: 3/26/2018 warning antonsh
            holder.mCountdownView.setText("");
            holder.mCountdownView.setAlpha(0.0f);
        }

        // TODO: 3/26/2018 Implement grid layout manager which supports different heights of items and set visibility to countdown view

        holder.isEnabled = isMinuteEnabled;
        holder.mMinuteView.setTextColor(holder.getColor(timepoint, isMinuteEnabled));

        float enabledElevation = context.getResources().getDimensionPixelSize(R.dimen.minute_card_enabled_elevation);
        float disabledElevation = context.getResources().getDimensionPixelSize(R.dimen.minute_card_disabled_elevation);
        holder.mCard.setCardElevation(isMinuteEnabled ? enabledElevation : disabledElevation);
    }

    @Override
    public int getItemCount() {
        return mTimepoints.size();
    }

    @Override
    public long getItemId(int position) {
        return mTimepoints.get(position).getId();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        private CardView mCard;
        private TextView mMinuteView;
        private TextView mCountdownView;

        private boolean isEnabled;

        private int colorEnabled;
        private int colorDisabled;

        private ViewHolder(View view) {
            super(view);
            this.view = view;
            mCard = view.findViewById(R.id.schedule_item_minute_card);
            mMinuteView = view.findViewById(R.id.schedule_item_minute);
            mCountdownView = view.findViewById(R.id.schedule_item_countdown);

            ColorStateList colors = mMinuteView.getTextColors();
            colorEnabled = colors.getColorForState(new int[]{android.R.attr.state_enabled}, 0);
            colorDisabled = colors.getColorForState(new int[]{-android.R.attr.state_enabled}, 0);
        }

        private @ColorInt int getColor(Timepoint timepoint, boolean enabled) {
            if (timepoint.color != null)
                return timepoint.getColor(view.getContext(), enabled);

            return enabled ? colorEnabled : colorDisabled;
        }
    }
}
