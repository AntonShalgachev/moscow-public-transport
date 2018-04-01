package com.shalgachev.moscowpublictransport.data.db;

import com.shalgachev.moscowpublictransport.data.ScheduleType;

/**
 * Created by anton on 3/31/2018.
 */

public class StopTraits {
    public String stopName;
    public String routeName;
    public String directionFrom;
    public String directionTo;
    public String daysMask;
    public int firstHour;
    ScheduleType scheduleType;
}
