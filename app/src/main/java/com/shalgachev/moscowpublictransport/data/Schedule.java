package com.shalgachev.moscowpublictransport.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by anton on 5/28/2017.
 */

public class Schedule {
    private ScheduleType mScheduleType;
    private TransportType mTransportType;
    private CharSequence mRoute;
    private Direction mDirection;
    private CharSequence mStop;
    private HashMap<Integer, Set<Integer>> mTimepoints;

    public void setAsTimepoints(TransportType transportType, CharSequence route, Direction direction, CharSequence stop, Map<Integer, Set<Integer>> timepoints) {
        mScheduleType = ScheduleType.TIMEPOINTS;
        mTransportType = transportType;
        mRoute = route;
        mDirection = direction;
        mStop = stop;
        mTimepoints = new HashMap<>(timepoints);
    }

    public ScheduleType getScheduleType() {
        return mScheduleType;
    }

    public TransportType getTransportType() {
        return mTransportType;
    }

    public CharSequence getRoute() {
        return mRoute;
    }

    public Direction getDirection() {
        return mDirection;
    }

    public CharSequence getStop() {
        return mStop;
    }

    public Set<Integer> getHours() {
        return mTimepoints.keySet();
    }

    public Set<Integer> getMinutes(int hour) {
        return mTimepoints.get(hour);
    }
}
