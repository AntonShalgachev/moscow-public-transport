package com.shalgachev.moscowpublictransport.data.providers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.data.Direction;
import com.shalgachev.moscowpublictransport.data.InternetUtils;
import com.shalgachev.moscowpublictransport.data.Route;
import com.shalgachev.moscowpublictransport.data.Schedule;
import com.shalgachev.moscowpublictransport.data.ScheduleArgs;
import com.shalgachev.moscowpublictransport.data.ScheduleDays;
import com.shalgachev.moscowpublictransport.data.ScheduleError;
import com.shalgachev.moscowpublictransport.data.ScheduleType;
import com.shalgachev.moscowpublictransport.data.ScheduleUtils;
import com.shalgachev.moscowpublictransport.data.Season;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.data.Stops;
import com.shalgachev.moscowpublictransport.data.Timepoint;
import com.shalgachev.moscowpublictransport.data.TransportType;
import com.shalgachev.moscowpublictransport.helpers.UrlBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by anton on 4/2/2018.
 */

public class MoscowPrivateScheduleProvider extends BaseScheduleProvider {
    private static final String LOG_TAG = "MoscowPrivateSP";
    private static final String PROVIDER_ID = "moscow_private";

    private static final int FIRST_HOUR = 5;

    private static final String BASE_URL = "http://lkcar.transport.mos.ru/ExternalService/api/schedule/";

    private static final String KEY_DAYS = "days";
    private static final String KEY_SEASONS = "seasons";
    private static final String KEY_ROUTE = "route";
    private static final String KEY_DIRECTION = "direction";
    private static final String KEY_STOP = "stop";

    private static final String PARAM_VEHICLE_TYPE_ID = "vehicleTypeId";
    private static final String PARAM_DAY = "day";
    private static final String PARAM_SEASON = "season";
    private static final String PARAM_ROUTE_ID = "routeId";
    private static final String PARAM_STOP_ID = "stopId";
    private static final String PARAM_DIRECTION_ID = "directionId";

    private static final List<Season> SEASONS;
    private static final Map<String, String> DAYS;

    static {
        DAYS = new HashMap<>();
        DAYS.put("WEEKDAYS", "1111100");
        DAYS.put("WEEKEND", "0000011");

        SEASONS = new ArrayList<>();
        SEASONS.add(Season.WINTER);
        SEASONS.add(Season.SUMMER);
    }

    private UrlBuilder createUrlBuilder(String key) throws ScheduleProviderException {
        try {
            return new UrlBuilder(BASE_URL + key);
        } catch (UnsupportedEncodingException e) {
            throw new ScheduleProviderException(ScheduleError.ErrorCode.INTERNAL_ERROR);
        }
    }

    @NonNull
    private String loadUrl(String url) throws ScheduleProviderException {
        Log.i(LOG_TAG, String.format("Fetching '%s'", url));

        String json = InternetUtils.fetchUrl(url);
        if (json == null)
            throw new ScheduleProviderException(ScheduleError.ErrorCode.URL_FETCH_FAILED);

        return json;
    }

    private JSONArray loadUrlAsArray(String url) throws JSONException, ScheduleProviderException {
        return new JSONArray(loadUrl(url));
    }

    private JSONObject loadUrlAsObject(String url) throws JSONException, ScheduleProviderException {
        return new JSONObject(loadUrl(url));
    }

    private List<TransportType> getTransportTypes() {
        return new ArrayList<>(Arrays.asList(
                TransportType.BUS,
                TransportType.TRAM,
                TransportType.TROLLEY
        ));
    }

    private String seasonToId(Season season) {
        return season.name();
    }

    private Season seasonFromId(String id) {
        return Season.valueOf(id);
    }

    private List<ScheduleDays> getScheduleDays() {
        List<ScheduleDays> scheduleDays = new ArrayList<>();

        for (Season season : SEASONS) {
            for (Map.Entry<String, String> dayMask : DAYS.entrySet()) {
                scheduleDays.add(new ScheduleDays(dayMask.getKey(), dayMask.getValue(), season, FIRST_HOUR));
            }
        }

        return scheduleDays;
    }

    private List<String> getIds(String key) throws ScheduleProviderException {
        throwIfNoInternet();

        String url = createUrlBuilder(key).build();

        try {
            List<String> ids = new ArrayList<>();

            JSONArray root = loadUrlAsArray(url);

            int numberOfItems = root.length();
            for (int i = 0; i < numberOfItems; i++) {
                JSONObject item = root.getJSONObject(i);
                String id = item.getString("id");

                ids.add(id);
            }

            return ids;
        } catch (JSONException e) {
            e.printStackTrace();
            throw new ScheduleProviderException(ScheduleError.ErrorCode.PARSING_ERROR);
        }
    }

    private void checkIfAPIOutdated() throws ScheduleProviderException {
        List<String> dayIds = getIds(KEY_DAYS);
        List<String> seasonIds = getIds(KEY_SEASONS);

        if (dayIds.size() != DAYS.size())
            throw new ScheduleProviderException(ScheduleError.ErrorCode.API_OUTDATED);
        for (String id : dayIds)
            if (!DAYS.containsKey(id))
                throw new ScheduleProviderException(ScheduleError.ErrorCode.API_OUTDATED);

        if (seasonIds.size() != SEASONS.size())
            throw new ScheduleProviderException(ScheduleError.ErrorCode.API_OUTDATED);
        for (String id : seasonIds)
            if (!SEASONS.contains(seasonFromId(id)))
                throw new ScheduleProviderException(ScheduleError.ErrorCode.API_OUTDATED);
    }

    private List<Direction> getDirections(Route route, ScheduleDays days) throws ScheduleProviderException {
        throwIfNoInternet();

        String daysId = days.daysId;
        String routeId = route.id;

        String url = createUrlBuilder(KEY_DIRECTION)
                .appendParam(PARAM_DAY, daysId)
                .appendParam(PARAM_ROUTE_ID, routeId)
                .build();

        try {
            List<Direction> directions = new ArrayList<>();

            JSONArray root = loadUrlAsArray(url);

            int numberOfDirs = root.length();
            for (int i = 0; i < numberOfDirs; i++) {
                JSONObject item = root.getJSONObject(i);
                int id = item.getInt("id");
                String name = item.getString("direction");

                Direction direction = new Direction(String.valueOf(id));
                direction.setName(name);

                directions.add(direction);
            }

            return directions;
        } catch (JSONException e) {
            e.printStackTrace();
            throw new ScheduleProviderException(ScheduleError.ErrorCode.PARSING_ERROR);
        }
    }

    private List<Stop> getStops(Route route, ScheduleDays days, Direction direction) throws ScheduleProviderException {
        throwIfNoInternet();

        String url = createUrlBuilder(KEY_STOP)
                .appendParam(PARAM_DAY, days.daysId)
                .appendParam(PARAM_SEASON, seasonToId(days.season))
                .appendParam(PARAM_DIRECTION_ID, direction.getId())
                .appendParam(PARAM_ROUTE_ID, route.id)
                .appendParam(PARAM_VEHICLE_TYPE_ID, getTransportTypeId(route.transportType))
                .build();

        try {
            List<Stop> stops = new ArrayList<>();

            JSONArray root = loadUrlAsArray(url);

            int numberOfDirs = root.length();
            for (int i = 0; i < numberOfDirs; i++) {
                JSONObject item = root.getJSONObject(i);
                int id = item.getInt("id");
                String name = item.getString("stopName");

                stops.add(new Stop(route, days, direction, name, id, ScheduleType.TIMEPOINTS));
            }

            return stops;
        } catch (JSONException e) {
            e.printStackTrace();
            throw new ScheduleProviderException(ScheduleError.ErrorCode.PARSING_ERROR);
        }
    }

    private Stops getStops(Route route) throws ScheduleProviderException {
        if (!route.providerId.equals(getProviderId()))
            throw new ScheduleProviderException(ScheduleError.ErrorCode.WRONG_PROVIDER);

        checkIfAPIOutdated();

        Map<Stops.StopConfiguration, List<Stop>> stopsMap = new HashMap<>();
        List<Direction> directions = new ArrayList<>();
        List<ScheduleDays> scheduleDays = new ArrayList<>();

        for (ScheduleDays days : getScheduleDays()) {
            if (!scheduleDays.contains(days))
                scheduleDays.add(days);

            for (Direction direction : getDirections(route, days)) {
                if (!directions.contains(direction))
                    directions.add(direction);

                Stops.StopConfiguration configuration = new Stops.StopConfiguration(direction, days);

                List<Stop> stops = getStops(route, days, direction);
                if (stops.isEmpty()) {
                    Log.w(LOG_TAG, String.format("Stops empty for configuration '%s'", configuration.toString()));
                    continue;
                }

                Pair<String, String> endpoints = ScheduleUtils.inferDirectionEndpoints(direction.getName(), stops);
                direction.setEndpoints(endpoints.first, endpoints.second);

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
    private List<Route> getRoutes(TransportType transportType) throws ScheduleProviderException {
        throwIfNoInternet();

        String url = createUrlBuilder(KEY_ROUTE).appendParam(PARAM_VEHICLE_TYPE_ID, getTransportTypeId(transportType)).build();

        try {
            List<Route> routes = new ArrayList<>();

            JSONArray root = loadUrlAsArray(url);

            int numberOfRoutes = root.length();
            for (int i = 0; i < numberOfRoutes; i++) {
                JSONObject routeObj = root.getJSONObject(i);
                int id = routeObj.getInt("id");
                String name = routeObj.getString("routeName");

                routes.add(new Route(transportType, String.valueOf(id), name, getProviderId()));
            }

            return routes;
        } catch (JSONException e) {
            e.printStackTrace();
            throw new ScheduleProviderException(ScheduleError.ErrorCode.PARSING_ERROR);
        }
    }

    private Schedule getSchedule(Stop stop) throws ScheduleProviderException {
        if (stop == null)
            throw new ScheduleProviderException(ScheduleError.ErrorCode.INVALID_STOP);
        if (!stop.route.providerId.equals(getProviderId()))
            throw new ScheduleProviderException(ScheduleError.ErrorCode.WRONG_PROVIDER);

        throwIfNoInternet();

        Route route = stop.route;
        ScheduleDays days = stop.days;
        Direction direction = stop.direction;
        String url = createUrlBuilder("")
                .appendParam(PARAM_DAY, days.daysId)
                .appendParam(PARAM_SEASON, seasonToId(days.season))
                .appendParam(PARAM_DIRECTION_ID, direction.getId())
                .appendParam(PARAM_ROUTE_ID, route.id)
                .appendParam(PARAM_VEHICLE_TYPE_ID, getTransportTypeId(route.transportType))
                .appendParam(PARAM_STOP_ID, String.valueOf(stop.id))
                .build();

        List<Timepoint> timepoints = new ArrayList<>();

        try {
            JSONObject root = loadUrlAsObject(url);

            Iterator<String> hours = root.keys();
            while (hours.hasNext()) {
                String hourKey = hours.next();
                int hour = Integer.valueOf(hourKey);
                JSONArray minutes = root.getJSONArray(hourKey);

                for (int i = 0; i < minutes.length(); i++) {
                    int minute = Integer.valueOf(minutes.getString(i));

                    timepoints.add(new Timepoint(hour, minute));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            throw new ScheduleProviderException(ScheduleError.ErrorCode.PARSING_ERROR);
        }

        if (timepoints.isEmpty())
            throw new ScheduleProviderException(ScheduleError.ErrorCode.EMPTY_SCHEDULE);

        Schedule schedule = new Schedule();
        schedule.setAsTimepoints(stop, timepoints);

        return schedule;
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

    private static String getTransportTypeId(TransportType type) {
        switch (type) {
            case BUS:
                return "700";
            case TRAM:
                return "900";
            case TROLLEY:
                return "800";
        }

        return "";
    }

    @Override
    public String getProviderId() {
        return PROVIDER_ID;
    }

    @Override
    public String getProviderName(Context context) {
        return context.getString(R.string.provider_name_moscow_private);
    }
}
