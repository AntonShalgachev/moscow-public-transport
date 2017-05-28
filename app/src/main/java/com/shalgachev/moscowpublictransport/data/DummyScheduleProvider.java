package com.shalgachev.moscowpublictransport.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by anton on 5/28/2017.
 */

public class DummyScheduleProvider extends BaseScheduleProvider {
    @Override
    public Set<TransportType> getTransportTypes() {
        HashSet<TransportType> transportTypes = new HashSet<>();
        transportTypes.add(TransportType.BUS);
        transportTypes.add(TransportType.TRAM);
        transportTypes.add(TransportType.TROLLEY);

        return transportTypes;
    }

    @Override
    public Set<CharSequence> getRoutes(TransportType transportType) {
        return new HashSet<CharSequence>(Arrays.asList("268", "268К", "5Э"));
    }

    @Override
    public Set<CharSequence> getDays(TransportType transportType, CharSequence route) {
        return new HashSet<CharSequence>(Arrays.asList("1111100", "0000011"));
    }

    @Override
    public Set<Direction> getDirections(TransportType transportType, CharSequence route, CharSequence days) {
        return new HashSet<>(Arrays.asList(new Direction("A", "B"), new Direction("B", "A")));
    }

    @Override
    public Set<CharSequence> getStops(TransportType transportType, CharSequence route, CharSequence days, Direction direction) {
        return new HashSet<CharSequence>(Arrays.asList("Тестовая остановочка 1", "Тестовая остановочка 2"));
    }

    @Override
    public Schedule getSchedule(TransportType transportType, CharSequence route, CharSequence days, Direction direction, CharSequence stop) {
        validateParameters(transportType, route, days, direction, stop);

        HashMap<Integer, Set<Integer>> timepoints = new HashMap<>();
        timepoints.put(9, new HashSet<>(Arrays.asList(10, 16, 28, 49, 59)));
        timepoints.put(10, new HashSet<>(Arrays.asList(2, 14, 30, 50, 58)));
        timepoints.put(11, new HashSet<>(Arrays.asList(0, 10, 20, 30, 40, 50)));

        Schedule schedule = new Schedule();
        schedule.setAsTimepoints(transportType, route, direction, stop, timepoints);

        return schedule;
    }

    @Override
    public CharSequence getProviderName() {
        return "Dummy";
    }
}
