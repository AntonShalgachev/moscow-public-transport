package com.shalgachev.moscowpublictransport.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by anton on 4/8/2018.
 */

public class Stops {
    private Map<StopConfiguration, List<Stop>> mStops;
    private List<Direction> mDirections;
    private List<ScheduleDays> mScheduleDays;
    private List<Stop> mAllStops;

    public Stops(Map<StopConfiguration, List<Stop>> stops, List<Direction> directions, List<ScheduleDays> scheduleDays) {
        mStops = new HashMap<>(stops);
        mDirections = new ArrayList<>(directions);
        mScheduleDays = new ArrayList<>(scheduleDays);

        mAllStops = new ArrayList<>();
        for (Map.Entry<StopConfiguration, List<Stop>> entry : mStops.entrySet())
            mAllStops.addAll(entry.getValue());
    }

    public List<Direction> getDirections() {
        return mDirections;
    }

    public List<ScheduleDays> getScheduleDays() {
        return mScheduleDays;
    }

    public List<Stop> getStops(Direction direction, ScheduleDays days) {
        return mStops.get(new StopConfiguration(direction, days));
    }

    public List<Stop> getAllStops() {
        return mAllStops;
    }

    public boolean hasStops() {
        return !mAllStops.isEmpty();
    }

    public static class StopConfiguration {
        public Direction direction;
        public ScheduleDays days;

        public StopConfiguration(Direction direction, ScheduleDays days) {
            this.direction = direction;
            this.days = days;
        }

        @Override
        public int hashCode() {
            int result = 0;
            result = result * 31 + direction.hashCode();
            result = result * 31 + days.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;

            StopConfiguration stopConfiguration = (StopConfiguration) obj;

            if (!direction.equals(stopConfiguration.direction)) return false;
            if (!days.equals(stopConfiguration.days)) return false;
            return true;
        }

        @Override
        public String toString() {
            return String.format("%s (%s)", direction.toString(), days.toString());
        }
    }
}
