package com.shalgachev.moscowpublictransport.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by anton on 5/28/2017.
 */

public class Schedule {
    private ScheduleType mScheduleType;
    private Stop mStop;
    private HashMap<Integer, Set<Integer>> mTimepoints;

    public void setAsTimepoints(Stop stop, Map<Integer, Set<Integer>> timepoints) {
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

    public Set<Integer> getHours() {
        return mTimepoints.keySet();
    }

    public Set<Integer> getMinutes(int hour) {
        return mTimepoints.get(hour);
    }
}
