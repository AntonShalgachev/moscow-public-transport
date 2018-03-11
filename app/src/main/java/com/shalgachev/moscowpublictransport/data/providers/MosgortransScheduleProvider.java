package com.shalgachev.moscowpublictransport.data.providers;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.data.Direction;
import com.shalgachev.moscowpublictransport.data.Route;
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
import java.util.List;
import java.util.Map;

/**
 * Created by anton on 6/25/2017.
 */

public class MosgortransScheduleProvider extends BaseScheduleProvider {
    private static final String LOG_TAG = "MosgortransSP";
    private static final String PROVIDER_ID = "mosgortrans";
    private static final String BASE_METADATA_URL = "http://www.mosgortrans.org/pass3/request.ajax.php?";
    private static final String BASE_SCHEDULE_URL = "http://www.mosgortrans.org/pass3/shedule.printable.php?";

    @NonNull
    private List<Route> getRoutes(TransportType transportType) throws ScheduleProviderException {
        if (!Utils.isInternetAvailable()) {
            throw new ScheduleProviderException(Result.ErrorCode.INTERNET_NOT_AVAILABLE);
        }

        String url = constructMetadataUrl(MetadataListType.ROUTES, transportType);
        Log.i(LOG_TAG, String.format("getRoutes: Fetching '%s'", url));

        List<String> routeNames = Utils.fetchUrlAsStringList(url);

        if (routeNames == null)
            throw new ScheduleProviderException(Result.ErrorCode.URL_FETCH_FAILED);

        List<Route> routes = new ArrayList<>();

        Map<Character, Integer> freqMap = new HashMap<>();

        for (String name : routeNames) {
            Route route = new Route(name, getProviderId());
            routes.add(route);

            for (int i = 0; i < name.length(); i++) {
                char c = name.charAt(i);

                if (Character.isDigit(c))
                    continue;
                if (Character.isLetter(c) && Character.isLowerCase(c))
                    continue;

                if (!freqMap.containsKey(c))
                    freqMap.put(c, 0);

                int freq = freqMap.get(c);
                freqMap.put(c, freq + 1);
            }
        }

        for (Map.Entry<Character, Integer> entry : freqMap.entrySet()) {
            Log.d("Temp", String.format("%s: %d", entry.getKey(), entry.getValue()));
        }

        return routes;
    }

    @NonNull
    private List<String> getDaysMasks(TransportType transportType, String route) throws ScheduleProviderException {
        if (!Utils.isInternetAvailable()) {
            throw new ScheduleProviderException(Result.ErrorCode.INTERNET_NOT_AVAILABLE);
        }

        String url = constructMetadataUrl(MetadataListType.DAYS_MASKS, transportType, route);
        Log.i(LOG_TAG, String.format("getDaysMasks: Fetching '%s'", url));

        List<String> masks = Utils.fetchUrlAsStringList(url);

        if (masks == null)
            throw new ScheduleProviderException(Result.ErrorCode.URL_FETCH_FAILED);

        return masks;
    }

    @NonNull
    private List<Direction> getDirections(TransportType transportType, String route, String daysMask) throws ScheduleProviderException {
        if (!Utils.isInternetAvailable()) {
            throw new ScheduleProviderException(Result.ErrorCode.INTERNET_NOT_AVAILABLE);
        }

        // TODO: 6/25/2017 return just 2 directions without query

        String url = constructMetadataUrl(MetadataListType.DIRECTIONS, transportType, route, daysMask);
        Log.i(LOG_TAG, String.format("getDirections: Fetching '%s'", url));

        List<String> directionList = Utils.fetchUrlAsStringList(url);

        if (directionList == null)
            throw new ScheduleProviderException(Result.ErrorCode.URL_FETCH_FAILED);

        if (directionList.size() != 2) {
            Log.e(LOG_TAG, String.format("getDirections(%s, %s, %s): Unusual direction list: has %d items, expected 2", transportType.name(), route, daysMask, directionList.size()));
        }

        List<Direction> directions = new ArrayList<>();

        for (int i = 0; i < directionList.size(); i++) {
            String id = (i == 0) ? "AB" : "BA";
            directions.add(new Direction(id));
        }

        return directions;
    }

    @NonNull
    private List<Stop> getStops(TransportType transportType, String route, String daysMask, Direction direction) throws ScheduleProviderException {
        if (!Utils.isInternetAvailable()) {
            throw new ScheduleProviderException(Result.ErrorCode.INTERNET_NOT_AVAILABLE);
        }

        String url = constructMetadataUrl(MetadataListType.STOPS, transportType, route, daysMask, direction);
        Log.i(LOG_TAG, String.format("getStops: Fetching '%s'", url));
        List<String> stopList = Utils.fetchUrlAsStringList(url);

        if (stopList == null)
            throw new ScheduleProviderException(Result.ErrorCode.URL_FETCH_FAILED);

        List<Stop> stops = new ArrayList<>();
        for (int i = 0; i < stopList.size(); i++) {
            Stop stop = new Stop(transportType, new Route(route, getProviderId()), daysMask, direction, stopList.get(i), i);
            stops.add(stop);
        }

        return stops;
    }

    @NonNull
    private List<Stop> getStops(TransportType transportType, Route route) throws ScheduleProviderException {
        Log.i(LOG_TAG, "Loading stops for route " + route.toString());
        List<Stop> allStops = new ArrayList<>();

        if (route.providerId.equals(getProviderId())) {
            for (String mask : getDaysMasks(transportType, route.name)) {
                for (Direction direction : getDirections(transportType, route.name, mask)) {
                    List<Stop> stops = getStops(transportType, route.name, mask, direction);
                    direction.setEndpoints(stops.get(0).name, stops.get(stops.size() - 1).name);

                    allStops.addAll(stops);
                }
            }
        } else {
            Log.e(LOG_TAG, "Attempt to retrieve stops of unknown provider");
        }

        return allStops;
    }

    @NonNull
    private Schedule getSchedule(Stop stop) throws ScheduleProviderException {
        if (stop == null) {
            throw new ScheduleProviderException(Result.ErrorCode.INVALID_STOP);
        }

        if (!Utils.isInternetAvailable()) {
            throw new ScheduleProviderException(Result.ErrorCode.INTERNET_NOT_AVAILABLE);
        }

        List<Schedule.Timepoint> timepoints = new ArrayList<>();

        String url = constructScheduleUrl(stop);
        Log.i(LOG_TAG, String.format("getSchedule: Fetching '%s'", url));

        try {
            Document doc = Jsoup.connect(url).get();

            Element warning = doc.selectFirst("td[class=warning]");
            if (warning != null)
                throw new ScheduleProviderException(Result.ErrorCode.INVALID_SCHEDULE_URL);

            Elements timeTags = doc.select("span[class~=(?:hour|minute)]");

            int hour = -1;
            for (Element tag : timeTags) {
                String tagClass = tag.className();
                String tagText = tag.text();

                int value;
                try {
                    value = Integer.parseInt(tagText);
                } catch (NumberFormatException e) {
                    Log.w(LOG_TAG, String.format("Failed to parse data '%s'", tagText));
                    continue;
                }

                switch (tagClass) {
                    case "hour":
                        hour = value;
                        break;
                    case "minute":
                        timepoints.add(new Schedule.Timepoint(hour,value));
                        break;

                    default:
                        Log.e(LOG_TAG, String.format("Unknown tag class '%s'", tagClass));
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ScheduleProviderException(Result.ErrorCode.INTERNAL_ERROR);
        }

        if (timepoints.isEmpty())
            throw new ScheduleProviderException(Result.ErrorCode.EMPTY_SCHEDULE);

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

    private static String constructMetadataUrl(MetadataListType listType, TransportType transportType, String route, String daysMask, String directionId) {
        final String LIST_TYPE_PARAM = "list";
        final String TRANSPORT_TYPE_PARAM = "type";
        final String ROUTE_PARAM = "way";
        final String DAYS_MASK_PARAM = "date";
        final String DIRECTION_PARAM = "direction";

        return Uri.parse(BASE_METADATA_URL).buildUpon()
                .appendQueryParameter(LIST_TYPE_PARAM, getMetadataListTypeId(listType))
                .appendQueryParameter(TRANSPORT_TYPE_PARAM, getTransportTypeId(transportType))
                .appendQueryParameter(ROUTE_PARAM, route)
                .appendQueryParameter(DAYS_MASK_PARAM, daysMask)
                .appendQueryParameter(DIRECTION_PARAM, directionId)
                .build().toString();
    }

    private static String constructMetadataUrl(MetadataListType listType, TransportType transportType) {
        return constructMetadataUrl(listType, transportType, "", "", "");
    }

    private static String constructMetadataUrl(MetadataListType listType, TransportType transportType, String route) {
        return constructMetadataUrl(listType, transportType, route, "", "");
    }

    private static String constructMetadataUrl(MetadataListType listType, TransportType transportType, String route, String daysMask) {
        return constructMetadataUrl(listType, transportType, route, daysMask, "");
    }

    private static String constructMetadataUrl(MetadataListType listType, TransportType transportType, String route, String daysMask, Direction direction) {
        String directionId = direction != null ? direction.getId() : "";

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
                .appendQueryParameter(ROUTE_PARAM, stop.route.name)
                .appendQueryParameter(DAYS_MASK_PARAM, stop.daysMask)
                .appendQueryParameter(DIRECTION_PARAM, stop.direction.getId())
                .appendQueryParameter(WAYPOINT_PARAM, String.valueOf(stop.id))
                .build().toString();
    }

    @Override
    public Result run() {
        Log.d(LOG_TAG, "Running task");
        Result result = new Result();

        try {
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
        } catch (ScheduleProviderException e) {
            e.printStackTrace();
            result.errorCode = e.getError();
        }

        Log.d(LOG_TAG, "Finished running task");

        return result;
    }

    private List<TransportType> getTransportTypes() {
        return new ArrayList<>(Arrays.asList(
                TransportType.BUS,
                TransportType.TRAM,
                TransportType.TROLLEY
        ));
    }

    @Override
    public String getProviderId() {
        return PROVIDER_ID;
    }

    @Override
    public String getProviderName(Context context) {
        return context.getString(R.string.provider_name_mosgortrans);
    }

    private enum MetadataListType {
        ROUTES,
        DAYS_MASKS,
        DIRECTIONS,
        STOPS
    }
}
