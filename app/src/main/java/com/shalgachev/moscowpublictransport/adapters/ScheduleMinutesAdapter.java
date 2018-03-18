package com.shalgachev.moscowpublictransport.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.data.Schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Created by anton on 3/18/2018.
 */

public class ScheduleMinutesAdapter extends RecyclerView.Adapter<ScheduleMinutesAdapter.ViewHolder> {
    private int mHour;
    private List<Integer> mMinutes;

    public ScheduleMinutesAdapter(int hour, List<Integer> minutes) {
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
