package com.shalgachev.moscowpublictransport.adapters;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.data.Schedule;
import com.shalgachev.moscowpublictransport.data.ScheduleUtils;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Created by anton on 3/18/2018.
 */

public class ScheduleMinutesAdapter extends RecyclerView.Adapter<ScheduleMinutesAdapter.ViewHolder> {
    private Schedule mSchedule;
    private int mHour;
    private List<Integer> mMinutes;

    public ScheduleMinutesAdapter(Schedule schedule, int hour, List<Integer> minutes) {
        mSchedule = schedule;
        mHour = hour;
        mMinutes = new ArrayList<>(minutes);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_schedule_minute_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int minute = mMinutes.get(position);
        holder.mMinuteView.setText(String.format(Locale.US, "%02d", minute));

        // TODO: 3/18/2018 move this logic somewhere else?
        Calendar timepointCalendar = Calendar.getInstance();
        timepointCalendar.set(Calendar.HOUR, 0);
        timepointCalendar.set(Calendar.MINUTE, 0);
        timepointCalendar.set(Calendar.SECOND, 0);
        timepointCalendar.set(Calendar.MILLISECOND, 0);

        int firstHour = mSchedule.getTimepoints().getFirstHour();
        int hour = mHour;
        if (hour < firstHour && timepointCalendar.get(Calendar.HOUR) >= firstHour)
            hour += 24;

        timepointCalendar.add(Calendar.HOUR, hour);
        timepointCalendar.add(Calendar.MINUTE, minute+1);
        timepointCalendar.add(Calendar.MILLISECOND, -1);

        Calendar nowCalendar = Calendar.getInstance();

        long diffInMillis = timepointCalendar.getTimeInMillis() - nowCalendar.getTimeInMillis();
        long diffInMinutes = diffInMillis / 1000 / 60;

        boolean isDiffPositive = diffInMillis > 0;

        // TODO: 3/19/2018 highlight only N next timepoints
        int maxDiff = 90;
        int closeThreshold = maxDiff / 3;
        int mediumThreshold = 2 * maxDiff / 3;
        if (isDiffPositive && diffInMinutes >= 0 && diffInMinutes < maxDiff) {
            Context context = holder.view.getContext();
            String intervalStr = ScheduleUtils.formatShortTimeInterval(context, diffInMinutes);
            holder.mCountdownView.setText(context.getString(R.string.schedule_next_in, intervalStr));
            @ColorRes int color;
            if (diffInMinutes < closeThreshold)
                color = R.color.next_in_close_color;
            else if (diffInMinutes < mediumThreshold)
                color = R.color.next_in_medium_color;
            else
                color = R.color.next_in_far_color;

            holder.mCountdownView.setTextColor(context.getResources().getColor(color));
        } else {
            holder.mCountdownView.setVisibility(View.GONE);
        }
        holder.mMinuteView.setEnabled(isDiffPositive);
    }

    @Override
    public int getItemCount() {
        return mMinutes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public TextView mMinuteView;
        public TextView mCountdownView;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            mMinuteView = view.findViewById(R.id.schedule_item_minute);
            mCountdownView = view.findViewById(R.id.schedule_item_countdown);
        }
    }
}
