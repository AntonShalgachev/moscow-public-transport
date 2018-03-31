package com.shalgachev.moscowpublictransport.data;

import java.io.Serializable;

/**
 * Created by anton on 3/31/2018.
 */

public class ScheduleDays implements Serializable {
    public String daysMask;
    public Season season;

    // additional fields
    public int firstHour;

    public ScheduleDays(String daysMask) {
        this.daysMask = daysMask;
        this.season = Season.ALL;
    }
    public ScheduleDays(String daysMask, Season season) {
        this.daysMask = daysMask;
        this.season = season;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + daysMask.hashCode();
        result = 31 * result + season.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScheduleDays days = (ScheduleDays) o;

        if (!daysMask.equals(days.daysMask)) return false;
        if (!season.equals(days.season)) return false;
        return true;
    }
}
