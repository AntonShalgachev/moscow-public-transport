package com.shalgachev.moscowpublictransport.data.providers;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.data.Direction;
import com.shalgachev.moscowpublictransport.data.InternetUtils;
import com.shalgachev.moscowpublictransport.data.Route;
import com.shalgachev.moscowpublictransport.data.Schedule;
import com.shalgachev.moscowpublictransport.data.ScheduleArgs;
import com.shalgachev.moscowpublictransport.data.ScheduleDays;
import com.shalgachev.moscowpublictransport.data.ScheduleError;
import com.shalgachev.moscowpublictransport.data.ScheduleType;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.data.Stops;
import com.shalgachev.moscowpublictransport.data.Timepoint;
import com.shalgachev.moscowpublictransport.data.TransportType;
import com.shalgachev.moscowpublictransport.helpers.UrlBuilder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
    private static final String BASE_METADATA_URL = "http://www.mosgortrans.org/pass3/request.ajax.php";
    private static final String BASE_SCHEDULE_URL = "http://www.mosgortrans.org/pass3/shedule.printable.php";

    private static final int FIRST_HOUR = 5;

    // for some reason Mosgortrans sends these strings in a list of routes
    private static final List<String> EXCLUDED_ROUTE_NAMES = Arrays.asList("route", "stations", "streets");

    // Mosgortrans for some reason mangles '№' into 'в„–' in stop names
    private static String fixStopName(String route) {
        return route.replace("в„–", "№");
    }

    @NonNull
    private List<Route> getRoutes(TransportType transportType) throws ScheduleProviderException {
        throwIfNoInternet();

        String url = constructMetadataUrl(MetadataListType.ROUTES, transportType);
        Log.i(LOG_TAG, String.format("getRoutes: Fetching '%s'", url));

        List<String> routeNames = InternetUtils.fetchUrlAsStringList(url);

        if (routeNames == null)
            throw new ScheduleProviderException(ScheduleError.ErrorCode.URL_FETCH_FAILED);

        List<Route> routes = new ArrayList<>();

        for (String name : routeNames)
            if (!EXCLUDED_ROUTE_NAMES.contains(name))
                routes.add(new Route(transportType, name, name, getProviderId()));

        return routes;
    }

    @NonNull
    private List<String> getDaysMasks(Route route) throws ScheduleProviderException {
        throwIfNoInternet();

        String url = constructMetadataUrl(MetadataListType.DAYS_MASKS, route.transportType, route.name);
        Log.i(LOG_TAG, String.format("getDaysMasks: Fetching '%s'", url));

        List<String> masks = InternetUtils.fetchUrlAsStringList(url);

        if (masks == null)
            throw new ScheduleProviderException(ScheduleError.ErrorCode.URL_FETCH_FAILED);

        return masks;
    }

    @NonNull
    private List<Direction> getDirections(Route route, String daysMask) throws ScheduleProviderException {
        throwIfNoInternet();

        // TODO: 6/25/2017 return just 2 directions without query

        String url = constructMetadataUrl(MetadataListType.DIRECTIONS, route.transportType, route.name, daysMask);
        Log.i(LOG_TAG, String.format("getDirections: Fetching '%s'", url));

        List<String> directionList = InternetUtils.fetchUrlAsStringList(url);

        if (directionList == null)
            throw new ScheduleProviderException(ScheduleError.ErrorCode.URL_FETCH_FAILED);

        if (directionList.size() != 2) {
            Log.e(LOG_TAG, String.format("getDirections(%s, %s, %s): Unusual direction list: has %d items, expected 2", route.transportType.name(), route, daysMask, directionList.size()));
        }

        List<Direction> directions = new ArrayList<>();

        for (int i = 0; i < directionList.size(); i++) {
            String id = (i == 0) ? "AB" : "BA";
            directions.add(new Direction(id));
        }

        return directions;
    }

    @NonNull
    private List<Stop> getStops(Route route, ScheduleDays days, Direction direction) throws ScheduleProviderException {
        throwIfNoInternet();

        String url = constructMetadataUrl(MetadataListType.STOPS, route.transportType, route.name, days.daysMask, direction);
        Log.i(LOG_TAG, String.format("getStops: Fetching '%s'", url));
        List<String> stopList = InternetUtils.fetchUrlAsStringList(url);

        if (stopList == null)
            throw new ScheduleProviderException(ScheduleError.ErrorCode.URL_FETCH_FAILED);

        List<Stop> stops = new ArrayList<>();
        for (int i = 0; i < stopList.size(); i++) {
            String stopName = fixStopName(stopList.get(i));
            Stop stop = new Stop(route, days, direction, stopName, i, ScheduleType.TIMEPOINTS);
            stops.add(stop);
        }

        return stops;
    }

    @NonNull
    private Stops getStops(Route route) throws ScheduleProviderException {
        if (!route.providerId.equals(getProviderId()))
            throw new ScheduleProviderException(ScheduleError.ErrorCode.WRONG_PROVIDER);

        Log.i(LOG_TAG, "Loading stops for route " + route.toString());

        Map<Stops.StopConfiguration, List<Stop>> stopsMap = new HashMap<>();
        List<Direction> directions = new ArrayList<>();
        List<ScheduleDays> scheduleDays = new ArrayList<>();

        for (String mask : getDaysMasks(route)) {
            ScheduleDays days = new ScheduleDays(mask, mask, FIRST_HOUR);
            if (!scheduleDays.contains(days))
                scheduleDays.add(days);

            for (Direction direction : getDirections(route, mask)) {
                if (!directions.contains(direction))
                    directions.add(direction);

                Stops.StopConfiguration configuration = new Stops.StopConfiguration(direction, days);

                List<Stop> stops = getStops(route, days, direction);
                if (stops.isEmpty()) {
                    Log.w(LOG_TAG, String.format("Stops empty for configuration '%s'", configuration.toString()));
                    continue;
                }

                direction.setEndpoints(stops.get(0).name, stops.get(stops.size() - 1).name);

                stopsMap.put(configuration, stops);
            }
        }

        Stops stops = new Stops(stopsMap, directions, scheduleDays);

        if (!stops.hasStops()) {
            Log.w(LOG_TAG, String.format("There are no stops for route '%s'", route.toString()));
            throw new ScheduleProviderException(ScheduleError.ErrorCode.NO_STOPS);
        }

        return stops;
    }

    @NonNull
    private Schedule getSchedule(Stop stop) throws ScheduleProviderException {
        if (stop == null)
            throw new ScheduleProviderException(ScheduleError.ErrorCode.INVALID_STOP);
        if (!stop.route.providerId.equals(getProviderId()))
            throw new ScheduleProviderException(ScheduleError.ErrorCode.WRONG_PROVIDER);

        throwIfNoInternet();

        List<Timepoint> timepoints = new ArrayList<>();

        String url = constructScheduleUrl(stop);
        if (url == null)
            throw new ScheduleProviderException(ScheduleError.ErrorCode.INTERNAL_ERROR);

        Log.i(LOG_TAG, String.format("getSchedule: Fetching '%s'", url));

        try {
            Document doc = Jsoup.connect(url).get();

            Element warning = doc.selectFirst("td[class=warning]");
            if (warning != null)
                throw new ScheduleProviderException(ScheduleError.ErrorCode.INVALID_SCHEDULE_URL);

            Elements timeTags = doc.select("span[class~=(?:hour|minute)]");

            int hour = -1;
            for (Element tag : timeTags) {
                String tagClass = tag.className();
                String tagText = tag.text();

                if (tagText.isEmpty())
                    continue;

                int value;
                String note = null;
                try {
                    String[] words = tagText.split(" ");
                    if (words.length == 0) {
                        Log.e(LOG_TAG, String.format("Failed to parse data '%s'", tagText));
                        throw new ScheduleProviderException(ScheduleError.ErrorCode.PARSING_ERROR);
                    }

                    if (words.length > 1)
                        note = words[1];
                    value = Integer.parseInt(words[0]);
                } catch (NumberFormatException e) {
                    Log.e(LOG_TAG, String.format("Failed to parse data '%s'", tagText));
                    throw new ScheduleProviderException(ScheduleError.ErrorCode.PARSING_ERROR);
                }

                switch (tagClass) {
                    case "hour":
                        hour = value;
                        break;
                    case "minute":
                        timepoints.add(new Timepoint(hour,value));
                        break;

                    default:
                        Log.e(LOG_TAG, String.format("Unknown tag class '%s'", tagClass));
                        throw new ScheduleProviderException(ScheduleError.ErrorCode.PARSING_ERROR);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ScheduleProviderException(ScheduleError.ErrorCode.INTERNAL_ERROR);
        }

        if (timepoints.isEmpty())
            throw new ScheduleProviderException(ScheduleError.ErrorCode.EMPTY_SCHEDULE);

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

        try {
            UrlBuilder builder = new UrlBuilder(BASE_SCHEDULE_URL, "windows-1251");
            builder.appendParam(TRANSPORT_TYPE_PARAM, getTransportTypeId(stop.route.transportType))
                    .appendParam(ROUTE_PARAM, stop.route.name)
                    .appendParam(DAYS_MASK_PARAM, stop.days.daysMask)
                    .appendParam(DIRECTION_PARAM, stop.direction.getId())
                    .appendParam(WAYPOINT_PARAM, String.valueOf(stop.id));

            return builder.build();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Result runProvider(ScheduleArgs args) throws ScheduleProviderException {
        Result result = new Result();

        switch (args.operationType) {
            case TYPES:
                result.transportTypes = getTransportTypes();
                break;
            case ROUTES:
                result.routes = getRoutes(args.transportType);
                break;
            case STOPS:
                result.stops = getStops(args.route);
                break;
            case SCHEDULE:
                result.schedule = getSchedule(args.stop);
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
