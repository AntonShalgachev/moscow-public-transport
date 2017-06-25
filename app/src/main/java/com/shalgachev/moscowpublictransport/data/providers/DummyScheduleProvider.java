package com.shalgachev.moscowpublictransport.data.providers;

import android.net.Uri;
import android.util.Log;

import com.shalgachev.moscowpublictransport.data.Direction;
import com.shalgachev.moscowpublictransport.data.Schedule;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.data.TransportType;
import com.shalgachev.moscowpublictransport.data.Utils;

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

public class DummyScheduleProvider extends BaseScheduleProvider {
    @Override
    public Result run() {
        Result result = new Result();

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException ex) {
            Log.w("DummyScheduleProvider", "I was interrupded...");
        }

        switch (getArgs().operationType) {
            case TYPES:
                result.transportTypes = getTransportTypes();
                break;
            case ROUTES:
                result.routes = getRoutes();
                break;
            case STOPS:
                result.stops = getStops();
                break;
            case SCHEDULE:
                result.schedule = getSchedule();
                break;
        }

        return result;
    }

    private List<TransportType> getTransportTypes() {
        return new ArrayList<>(Arrays.asList(
                TransportType.BUS,
                TransportType.TRAM,
                TransportType.TROLLEY
        ));
    }

    private List<CharSequence> getRoutes() {
        return new ArrayList<CharSequence>(Arrays.asList("268", "268К", "5Э"));
    }

    private List<CharSequence> getDaysMasks() {
        return new ArrayList<CharSequence>(Arrays.asList("1111100", "0000011"));
    }

    private List<Direction> getDirections() {
        return new ArrayList<>(Arrays.asList(new Direction("1", "Нижние подзалупки", "Верхние подзалупки"), new Direction("2", "Верхние подзалупки", "Нижние подзалупки")));
    }

    private List<Stop> getStops(CharSequence route, CharSequence daysMask, Direction direction) {
        List<CharSequence> stopNames;

        if (daysMask.equals("1111100")) {
            if (direction.getId().equals("1")) {
                stopNames = new ArrayList<CharSequence>(Arrays.asList("Нижние подзалупки", "Дратути", "Берлин", "Карманово", "WTF", "Верхние подзалупки"));
            } else {
                stopNames = new ArrayList<CharSequence>(Arrays.asList("Верхние подзалупки", "WTF", "Карманово", "Берлин", "Дратути", "Нижние подзалупки"));
            }
        } else {
            if (direction.getId().equals("1")) {
                stopNames = new ArrayList<CharSequence>(Arrays.asList("Нижние подзалупки (вых)", "Дратути (вых)", "Берлин (вых)", "Карманово (вых)", "WTF (вых)", "Верхние подзалупки (вых)"));
            } else {
                stopNames = new ArrayList<CharSequence>(Arrays.asList("Верхние подзалупки (вых)", "WTF (вых)", "Карманово (вых)", "Берлин (вых)", "Дратути (вых)", "Нижние подзалупки (вых)"));
            }
        }

        List<Stop> stops = new ArrayList<>();
        for(CharSequence name : stopNames) {
            Stop stop = new Stop(getProviderId(), getArgs().transportType, route, daysMask, direction, name);
            stops.add(stop);
        }

        return stops;
    }

    private List<Stop> getStops() {
        CharSequence route = getArgs().route;

        List<Stop> stops = new ArrayList<>();
        for (CharSequence mask : getDaysMasks()) {
            for (Direction direction : getDirections()) {
                for (Stop stop : getStops(route, mask, direction)) {
                    stops.add(stop);
                }
            }
        }

        return stops;
    }

    private Schedule getSchedule() {
        HashMap<Integer, Set<Integer>> timepoints = new HashMap<>();
        timepoints.put(9, new HashSet<>(Arrays.asList(10, 16, 28, 49, 59)));
        timepoints.put(10, new HashSet<>(Arrays.asList(2, 14, 30, 50, 58)));
        timepoints.put(11, new HashSet<>(Arrays.asList(0, 10, 20, 30, 40, 50)));

        Schedule schedule = new Schedule();
        schedule.setAsTimepoints(null, timepoints);

        return schedule;
    }

    public CharSequence getProviderId() {
        return "dummy";
    }
}
