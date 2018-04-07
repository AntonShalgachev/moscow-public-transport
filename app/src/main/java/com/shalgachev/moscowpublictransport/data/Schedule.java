package com.shalgachev.moscowpublictransport.data;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by anton on 5/28/2017.
 */

public class Schedule {

    private ScheduleType mScheduleType;
    private Stop mStop;
    private Timepoints mTimepoints;

    public void setAsTimepoints(@NonNull Stop stop, List<Timepoint> timepoints) {
        mScheduleType = ScheduleType.TIMEPOINTS;
        mStop = stop;
        mTimepoints = new Timepoints(timepoints, stop.days.firstHour);
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
