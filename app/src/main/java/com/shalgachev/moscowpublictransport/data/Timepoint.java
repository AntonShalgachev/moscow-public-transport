package com.shalgachev.moscowpublictransport.data;

import java.util.Locale;

/**
 * Created by anton on 4/8/2018.
 */
public class Timepoint {
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
    public boolean isEnabled;

    public boolean isEnabled() {
        return isEnabled;
    }

    public int minutesFromNow() {
        long millis = millisFromNow;
        long coef = 1000 * 60;

        long minutes = millis / coef;
        if ((millis ^ coef) < 0 && (minutes * coef != millis)) {
            minutes--;
        }

        return (int) minutes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Timepoint timepoint = (Timepoint) o;

        if (hour != timepoint.hour) return false;
        if (minute != timepoint.minute) return false;
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
