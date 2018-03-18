package com.shalgachev.moscowpublictransport.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

/**
 * Created by anton on 5/28/2017.
 */

public class Schedule {
    public static class Timepoint
    {
        public Timepoint(int h, int m) {
            hour = h;
            minute = m;
        }

        @Override
        public String toString() {
            return String.format(Locale.US, "%d:%d", hour, minute);
        }

        public static Timepoint valueOf(String str) {
            String[] values = str.split(":");
            if (values.length != 2)
                throw new IllegalArgumentException(String.format("Failed to parse timepoint '%s'", str));

            int hour = Integer.valueOf(values[0]);
            int minute = Integer.valueOf(values[1]);

            return new Timepoint(hour, minute);
        }

        public int hour;
        public int minute;
    }

    private class HourComparator implements Comparator<Integer> {
        private int mDayFirstHour;

        public HourComparator(int dayFirstHour) {
            mDayFirstHour = dayFirstHour;
        }

        @Override
        public int compare(Integer o1, Integer o2) {
            return normalize(o1) - normalize(o2);
        }

        private int normalize(int hour) {
            int val = hour - mDayFirstHour;
            if (val < 0)
                val += 24;
            return val;
        }
    }

    public class Timepoints
    {
        private List<Timepoint> mTimepoints;
        private TreeMap<Integer, List<Integer>> mHours;
        private Integer[] mSortedHours;
        // TODO: 3/18/2018 get first hour of the day from the schedule provider
        private int mFirstHour = 5;

        Timepoints(List<Timepoint> timepoints) {
            mTimepoints = new ArrayList<>(timepoints);

            mHours = new TreeMap<>(new HourComparator(mFirstHour));

            for (Schedule.Timepoint timepoint : timepoints) {
                int hour = timepoint.hour;
                int minute = timepoint.minute;

                if (!mHours.containsKey(hour))
                    mHours.put(hour, new ArrayList<Integer>());
                mHours.get(hour).add(minute);
            }

            mSortedHours = mHours.navigableKeySet().toArray(new Integer[]{});
        }

        public List<Timepoint> getTimepoints() {
            return mTimepoints;
        }

        public TreeMap<Integer, List<Integer>> getHoursMap() {
            return mHours;
        }

        public int getNthHour(int pos) {
            return mSortedHours[pos];
        }

        public int getFirstHour() {
            return mFirstHour;
        }
    }

    private ScheduleType mScheduleType;
    private Stop mStop;
    private Timepoints mTimepoints;

    public void setAsTimepoints(Stop stop, List<Timepoint> timepoints) {
        mScheduleType = ScheduleType.TIMEPOINTS;
        mStop = stop;
        mTimepoints = new Timepoints(timepoints);
    }

    public ScheduleType getScheduleType() {
        return mScheduleType;
    }

    public Stop getStop() {
        return mStop;
    }

    public Timepoints getTimepoints() {
        return mTimepoints;
    }
}
