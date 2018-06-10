package com.shalgachev.moscowpublictransport.data;

import java.io.Serializable;

/**
 * Created by anton on 3/31/2018.
 */

public class ScheduleDays implements Serializable {
    public Season season;
    public String daysId;

    public String daysMask;

    // additional fields
    public int firstHour;

    public ScheduleDays(String daysId, String daysMask, int firstHour) {
        this.daysId = daysId;
        this.daysMask = daysMask;
        this.season = Season.ALL;
        this.firstHour = firstHour;
        if (daysMask == null)
            throw new RuntimeException();
    }
    public ScheduleDays(String daysId, String daysMask, Season season, int firstHour) {
        this.daysId = daysId;
        this.daysMask = daysMask;
        this.season = season;
        this.firstHour = firstHour;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + daysId.hashCode();
        result = 31 * result + season.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s, %s", daysId, season.name());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScheduleDays days = (ScheduleDays) o;

        if (!daysId.equals(days.daysId)) return false;
        if (!season.equals(days.season)) return false;
        return true;
    }
}
