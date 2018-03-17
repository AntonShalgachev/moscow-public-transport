package com.shalgachev.moscowpublictransport.adapters;

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

public class ScheduleHourAdapter extends RecyclerView.Adapter<ScheduleHourAdapter.ViewHolder> {
    class HourComparator implements Comparator<Integer> {
        @Override
        public int compare(Integer o1, Integer o2) {
            return normalize(o1) - normalize(o2);
        }

        private int normalize(int hour) {
            int val = hour - 5;
            if (val < 0)
                val += 24;
            return val;
        }
    }

    private Schedule mSchedule;
    private TreeMap<Integer, List<Integer>> mHoursMap;

    public void setSchedule(Schedule schedule) {
        mSchedule = schedule;
        mHoursMap = new TreeMap<>(new HourComparator());

        for (Schedule.Timepoint timepoint : schedule.getTimepoints()) {
            int hour = timepoint.hour;
            int minute = timepoint.minute;

            if (!mHoursMap.containsKey(hour))
                mHoursMap.put(hour, new ArrayList<Integer>());
            mHoursMap.get(hour).add(minute);
        }

        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_schedule_hour_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int hour = (int) mHoursMap.keySet().toArray()[position];
        holder.mHourView.setText(String.valueOf(hour));
    }

    @Override
    public int getItemCount() {
        return mHoursMap != null ? mHoursMap.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public TextView mHourView;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            mHourView = view.findViewById(R.id.schedule_item_hour);
        }
    }
}
