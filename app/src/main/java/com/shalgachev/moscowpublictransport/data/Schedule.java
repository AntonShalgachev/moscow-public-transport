package com.shalgachev.moscowpublictransport.data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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

    private ScheduleType mScheduleType;
    private Stop mStop;
    private List<Timepoint> mTimepoints;

    public void setAsTimepoints(Stop stop, List<Timepoint> timepoints) {
        mScheduleType = ScheduleType.TIMEPOINTS;
        mStop = stop;
        mTimepoints = new ArrayList<>(timepoints);
    }

    public ScheduleType getScheduleType() {
        return mScheduleType;
    }

    public Stop getStop() {
        return mStop;
    }

    public List<Timepoint> getTimepoints() {
        return new ArrayList<>(mTimepoints);
    }
}
