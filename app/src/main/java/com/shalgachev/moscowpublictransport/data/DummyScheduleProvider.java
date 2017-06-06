package com.shalgachev.moscowpublictransport.data;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by anton on 5/28/2017.
 */

public class DummyScheduleProvider implements IScheduleProvider {
    @Override
    public List<TransportType> getTransportTypes() {
        ArrayList<TransportType> transportTypes = new ArrayList<>();
        transportTypes.add(TransportType.BUS);
        transportTypes.add(TransportType.TRAM);
        transportTypes.add(TransportType.TROLLEY);

        return transportTypes;
    }

    @Override
    public List<CharSequence> getRoutes(TransportType transportType) {
        return new ArrayList<CharSequence>(Arrays.asList("268", "268К", "5Э"));
    }

    @Override
    public List<CharSequence> getDaysMasks(TransportType transportType, CharSequence route) {
        return new ArrayList<CharSequence>(Arrays.asList("1111100", "0000011"));
    }

    @Override
    public List<Direction> getDirections(TransportType transportType, CharSequence route, CharSequence daysMask) {
        return new ArrayList<>(Arrays.asList(new Direction("1", "Нижние подзалупки", "Верхние подзалупки"), new Direction("2", "Верхние подзалупки", "Нижние подзалупки")));
    }

    @Override
    public List<CharSequence> getStops(TransportType transportType, CharSequence route, CharSequence daysMask, Direction direction) {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException ex) {
            Log.w("DummyScheduleProvider", "I was interrupded...");
        }

        if (direction.getId().equals("1"))
            return new ArrayList<CharSequence>(Arrays.asList("Нижние подзалупки", "Дратути", "Берлин", "Карманово", "WTF", "Верхние подзалупки"));
        else
            return new ArrayList<CharSequence>(Arrays.asList("Верхние подзалупки", "WTF", "Карманово", "Берлин", "Дратути", "Нижние подзалупки"));
    }

    @Override
    public Schedule getSchedule(TransportType transportType, CharSequence route, CharSequence daysMask, Direction direction, CharSequence stop) {
        HashMap<Integer, Set<Integer>> timepoints = new HashMap<>();
        timepoints.put(9, new HashSet<>(Arrays.asList(10, 16, 28, 49, 59)));
        timepoints.put(10, new HashSet<>(Arrays.asList(2, 14, 30, 50, 58)));
        timepoints.put(11, new HashSet<>(Arrays.asList(0, 10, 20, 30, 40, 50)));

        Schedule schedule = new Schedule();
        schedule.setAsTimepoints(transportType, route, direction, stop, timepoints);

        return schedule;
    }

    @Override
    public CharSequence getProviderId() {
        return "dummy";
    }

    @Override
    public CharSequence getProviderName() {
        return "Dummy";
    }
}
