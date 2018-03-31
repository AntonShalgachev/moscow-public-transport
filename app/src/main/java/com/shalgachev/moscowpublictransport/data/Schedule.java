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

        public int hour;
        public int minute;

        // used in schedule activity to notify remaining time
        public long millisFromNow;
        public boolean isCountdownShown;

        public boolean isEnabled() {
            return millisFromNow > 0;
        }
        public int secondsFromNow() {
            return (int)(millisFromNow / 1000 / 60);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Timepoint timepoint = (Timepoint) o;

            if (hour !=  timepoint.hour) return false;
            if (minute !=  timepoint.minute) return false;
            return true;
        }

        @Override
        public int hashCode() {
            int result = 0;
            result = 31 * result + hour;
            result = 31 * result + minute;
            return result;
        }
    }

    private static class HourComparator implements Comparator<Integer> {
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

    public static class Timepoints
    {
        private List<Timepoint> mTimepoints;
        private TreeMap<Integer, List<Timepoint>> mHours;
        private Integer[] mSortedHours;
        // TODO: 3/18/2018 get first hour of the day from the schedule provider
        private int mFirstHour = 5;

        Timepoints(List<Timepoint> timepoints) {
            mTimepoints = new ArrayList<>(timepoints);

            mHours = new TreeMap<>(new HourComparator(mFirstHour));

            for (Timepoint timepoint : timepoints) {
                int hour = timepoint.hour;

                if (!mHours.containsKey(hour))
                    mHours.put(hour, new ArrayList<Timepoint>());
                mHours.get(hour).add(timepoint);
            }

            mSortedHours = mHours.navigableKeySet().toArray(new Integer[]{});
        }

        public List<Timepoint> getTimepoints() {
            return mTimepoints;
        }

        public TreeMap<Integer, List<Timepoint>> getHoursMap() {
            return mHours;
        }

        public int getNthHour(int pos) {
            return mSortedHours[pos];
        }

        public int getFirstHour() {
            return mFirstHour;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Timepoints timepoints = (Timepoints) o;

            if (!mTimepoints.equals(timepoints.mTimepoints)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            int result = 0;
            result = 31 * result + mTimepoints.hashCode();
            return result;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Schedule schedule = (Schedule) o;

        if (!mScheduleType.equals(schedule.mScheduleType)) return false;
        if (!mStop.equals(schedule.mStop)) return false;
        if (!mTimepoints.equals(schedule.mTimepoints)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + mScheduleType.hashCode();
        result = 31 * result + mStop.hashCode();
        result = 31 * result + mTimepoints.hashCode();
        return result;
    }
}
