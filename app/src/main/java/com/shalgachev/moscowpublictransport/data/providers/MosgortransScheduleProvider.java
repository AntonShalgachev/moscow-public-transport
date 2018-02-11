package com.shalgachev.moscowpublictransport.data.providers;

import android.net.Uri;
import android.util.Log;

import com.shalgachev.moscowpublictransport.data.Direction;
import com.shalgachev.moscowpublictransport.data.Schedule;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.data.TransportType;
import com.shalgachev.moscowpublictransport.data.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by anton on 6/25/2017.
 */

public class MosgortransScheduleProvider extends BaseScheduleProvider {
    private static final String LOG_TAG = "MosgortransSP";
    private static final CharSequence PROVIDER_ID = "mosgortrans";
    private static final String BASE_METADATA_URL = "http://www.mosgortrans.org/pass3/request.ajax.php?";
    private static final String BASE_SCHEDULE_URL = "http://www.mosgortrans.org/pass3/shedule.printable.php?";

    private static List<CharSequence> getRoutes(TransportType transportType) {
        String url = constructMetadataUrl(MetadataListType.ROUTES, transportType);
        Log.i(LOG_TAG, String.format("getRoutes: Fetching '%s'", url));

        return Utils.fetchUrlAsList(url);
    }

    private static List<CharSequence> getDaysMasks(TransportType transportType, CharSequence route) {
        String url = constructMetadataUrl(MetadataListType.DAYS_MASKS, transportType, route);
        Log.i(LOG_TAG, String.format("getDaysMasks: Fetching '%s'", url));

        return Utils.fetchUrlAsList(url);
    }

    private static List<Direction> getDirections(TransportType transportType, CharSequence route, CharSequence daysMask) {
        // TODO: 6/25/2017 return just 2 directions without query

        String url = constructMetadataUrl(MetadataListType.DIRECTIONS, transportType, route, daysMask);
        Log.i(LOG_TAG, String.format("getDirections: Fetching '%s'", url));

        List<CharSequence> directionList = Utils.fetchUrlAsList(url);
        if (directionList.size() != 2) {
            Log.w(LOG_TAG, String.format("getDirections(%s, %s, %s): Unusual direction list: has %d items, expected 2", transportType.name(), route, daysMask, directionList.size()));
        }

        List<Direction> directions = new ArrayList<>();

        for (int i = 0; i < directionList.size(); i++) {
            CharSequence id = (i == 0) ? "AB" : "BA";
            directions.add(new Direction(id));
        }

        return directions;
    }

    private static List<Stop> getStops(TransportType transportType, CharSequence route, CharSequence daysMask, Direction direction) {
        String url = constructMetadataUrl(MetadataListType.STOPS, transportType, route, daysMask, direction);
        Log.i(LOG_TAG, String.format("getStops: Fetching '%s'", url));
        List<CharSequence> stopList = Utils.fetchUrlAsList(url);

        List<Stop> stops = new ArrayList<>();
        for (int i = 0; i < stopList.size(); i++) {
            Stop stop = new Stop(PROVIDER_ID, transportType, route, daysMask, direction, stopList.get(i), i);
            stops.add(stop);
        }

        return stops;
    }

    private static List<Stop> getStops(TransportType transportType, CharSequence route) {
        List<Stop> allStops = new ArrayList<>();
        for (CharSequence mask : getDaysMasks(transportType, route)) {
            for (Direction direction : getDirections(transportType, route, mask)) {
                List<Stop> stops = getStops(transportType, route, mask, direction);
                direction.setEndpoints(stops.get(0).name, stops.get(stops.size() - 1).name);

                allStops.addAll(stops);
            }
        }

        return allStops;
    }

    private static Schedule getSchedule(Stop stop) {
//        HashMap<Integer, SortedSet<Integer>> timepoints = new HashMap<>();
//        timepoints.put(9, new TreeSet<>(Arrays.asList(10, 16, 28, 49, 59)));
//        timepoints.put(10, new TreeSet<>(Arrays.asList(2, 14, 30, 50, 58)));
//        timepoints.put(11, new TreeSet<>(Arrays.asList(0, 10, 20, 30, 40, 50)));

        List<Schedule.Timepoint> timepoints = new ArrayList<>();

        String url = constructScheduleUrl(stop);
        Log.i(LOG_TAG, String.format("getSchedule: Fetching '%s'", url));

        try {
            Document doc = Jsoup.connect(url).get();
            Elements timeTags = doc.select("span[class~=(?:hour|minute)]");

            int hour = -1;
            for (Element tag : timeTags) {
                String tagClass = tag.className();
                String tagText = tag.text();

                int value;
                try {
                    value = Integer.parseInt(tagText);
                } catch (NumberFormatException e) {
                    Log.e(LOG_TAG, String.format("Failed to parse data '%s'", tagText));
                    continue;
                }

                if (tagClass.equals("hour")) {
                    hour = value;
                } else if (tagClass.equals("minute")) {
                    timepoints.add(new Schedule.Timepoint(hour,value));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Schedule schedule = new Schedule();
        schedule.setAsTimepoints(stop, timepoints);

        return schedule;
    }



    private static String getTransportTypeId(TransportType type) {
        switch (type) {
            case BUS:
                return "avto";
            case TRAM:
                return "tram";
            case TROLLEY:
                return "trol";
        }

        return "";
    }

    private static String getMetadataListTypeId(MetadataListType type) {
        switch (type) {
            case ROUTES:
                return "ways";
            case DAYS_MASKS:
                return "days";
            case DIRECTIONS:
                return "directions";
            case STOPS:
                return "waypoints";
        }

        return "";
    }

    private static String constructMetadataUrl(MetadataListType listType, TransportType transportType, CharSequence route, CharSequence daysMask, CharSequence directionId) {
        final String LIST_TYPE_PARAM = "list";
        final String TRANSPORT_TYPE_PARAM = "type";
        final String ROUTE_PARAM = "way";
        final String DAYS_MASK_PARAM = "date";
        final String DIRECTION_PARAM = "direction";

        return Uri.parse(BASE_METADATA_URL).buildUpon()
                .appendQueryParameter(LIST_TYPE_PARAM, getMetadataListTypeId(listType))
                .appendQueryParameter(TRANSPORT_TYPE_PARAM, getTransportTypeId(transportType))
                .appendQueryParameter(ROUTE_PARAM, route.toString())
                .appendQueryParameter(DAYS_MASK_PARAM, daysMask.toString())
                .appendQueryParameter(DIRECTION_PARAM, directionId.toString())
                .build().toString();
    }

    private static String constructMetadataUrl(MetadataListType listType, TransportType transportType) {
        return constructMetadataUrl(listType, transportType, "", "", "");
    }

    private static String constructMetadataUrl(MetadataListType listType, TransportType transportType, CharSequence route) {
        return constructMetadataUrl(listType, transportType, route, "", "");
    }

    private static String constructMetadataUrl(MetadataListType listType, TransportType transportType, CharSequence route, CharSequence daysMask) {
        return constructMetadataUrl(listType, transportType, route, daysMask, "");
    }

    private static String constructMetadataUrl(MetadataListType listType, TransportType transportType, CharSequence route, CharSequence daysMask, Direction direction) {
        CharSequence directionId = "";
        if (direction != null)
            directionId = direction.getId();

        return constructMetadataUrl(listType, transportType, route, daysMask, directionId);
    }

    private static String constructScheduleUrl(Stop stop) {
        final String TRANSPORT_TYPE_PARAM = "type";
        final String ROUTE_PARAM = "way";
        final String DAYS_MASK_PARAM = "date";
        final String DIRECTION_PARAM = "direction";
        final String WAYPOINT_PARAM = "waypoint";

        // TODO: 2/11/2018 Properly handle Russian letters, which need to be encoded in windows-1251
        return Uri.parse(BASE_SCHEDULE_URL).buildUpon()
                .appendQueryParameter(TRANSPORT_TYPE_PARAM, getTransportTypeId(stop.transportType))
                .appendQueryParameter(ROUTE_PARAM, stop.route.toString())
                .appendQueryParameter(DAYS_MASK_PARAM, stop.daysMask.toString())
                .appendQueryParameter(DIRECTION_PARAM, stop.direction.getId().toString())
                .appendQueryParameter(WAYPOINT_PARAM, String.valueOf(stop.id))
                .build().toString();
    }

    @Override
    public Result run() {
        Result result = new Result();

        switch (getArgs().operationType) {
            case TYPES:
                result.transportTypes = getTransportTypes();
                break;
            case ROUTES:
                result.routes = getRoutes(getArgs().transportType);
                break;
            case STOPS:
                result.stops = getStops(getArgs().transportType, getArgs().route);
                break;
            case SCHEDULE:
                result.schedule = getSchedule(getArgs().stop);
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

    public CharSequence getProviderId() {
        return PROVIDER_ID;
    }

    private enum MetadataListType {
        ROUTES,
        DAYS_MASKS,
        DIRECTIONS,
        STOPS
    }
}
