package com.shalgachev.moscowpublictransport.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by anton on 4/8/2018.
 */
public class Timepoints {
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

    private static class TimepointComparator implements Comparator<Timepoint> {
        private HourComparator hourComparator;

        public TimepointComparator(int dayFirstHour) {
            hourComparator = new HourComparator(dayFirstHour);
        }

        @Override
        public int compare(Timepoint o1, Timepoint o2) {
            int hourCompare = hourComparator.compare(o1.hour, o2.hour);
            if (hourCompare != 0)
                return hourCompare;

            return o1.minute - o2.minute;
        }
    }

    private List<Timepoint> mTimepoints;
    private TreeMap<Integer, List<Timepoint>> mHours;
    private Integer[] mSortedHours;
    private int mFirstHour = 5;

    public Timepoints(List<Timepoint> timepoints, int firstHour) {
        mTimepoints = new ArrayList<>(timepoints);
        mFirstHour = firstHour;

        Collections.sort(mTimepoints, new TimepointComparator(firstHour));

        mHours = new TreeMap<>(new HourComparator(firstHour));

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
