package com.shalgachev.moscowpublictransport.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by anton on 5/28/2017.
 */

public class Schedule {
    private ScheduleType mScheduleType;
    private Stop mStop;
    private HashMap<Integer, SortedSet<Integer>> mTimepoints;

    public void setAsTimepoints(Stop stop, Map<Integer, SortedSet<Integer>> timepoints) {
        mScheduleType = ScheduleType.TIMEPOINTS;
        mStop = stop;
        mTimepoints = new HashMap<>(timepoints);
    }

    public ScheduleType getScheduleType() {
        return mScheduleType;
    }

    public Stop getStop() {
        return mStop;
    }

    public SortedSet<Integer> getHours() {
        return new TreeSet<>(mTimepoints.keySet());
    }

    public SortedSet<Integer> getMinutes(int hour) {
        return mTimepoints.get(hour);
    }
}
