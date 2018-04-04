package com.shalgachev.moscowpublictransport.data.providers;

import android.content.Context;

import com.shalgachev.moscowpublictransport.data.Route;
import com.shalgachev.moscowpublictransport.data.ScheduleArgs;
import com.shalgachev.moscowpublictransport.data.ScheduleError;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.data.TransportType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by anton on 4/4/2018.
 */

public class ScheduleProviderProxy extends BaseScheduleProvider {
    private Map<CharSequence, BaseScheduleProvider> mScheduleProviders;

    ScheduleProviderProxy(Set<BaseScheduleProvider> providers) {
        mScheduleProviders = new HashMap<>();
        for (BaseScheduleProvider provider : providers) {
            mScheduleProviders.put(provider.getProviderId(), provider);
        }
    }

    public BaseScheduleProvider getScheduleProviderByName(CharSequence id) {
        if (!mScheduleProviders.containsKey(id))
            throw new IllegalArgumentException(String.format("Invalid schedule provider: %s", id));

        return mScheduleProviders.get(id);
    }

    private Result runTypes(ScheduleArgs args) throws ScheduleProviderException {
        Result result = new Result();
        Set<TransportType> allTypes = new HashSet<>();
        for (BaseScheduleProvider provider : mScheduleProviders.values()) {
            List<TransportType> types = provider.runProvider(args).transportTypes;
            allTypes.addAll(types);
        }

        result.transportTypes = new ArrayList<>(allTypes);

        return result;
    }

    private Result runRoutes(ScheduleArgs args) throws ScheduleProviderException {
        Result result = new Result();
        result.routes = new ArrayList<>();

        for (BaseScheduleProvider provider : mScheduleProviders.values()) {
            List<Route> routes = provider.runProvider(args).routes;
            result.routes.addAll(routes);
        }

        Collections.sort(result.routes, new Comparator<Route>() {
            @Override
            public int compare(Route o1, Route o2) {
                String leftName = o1.name;
                String rightName = o2.name;

                if (leftName.length() != rightName.length())
                    return leftName.length() - rightName.length();

                return leftName.compareTo(rightName);
            }
        });

        return result;
    }

    @Override
    public Result runProvider(ScheduleArgs args) throws ScheduleProviderException {
        switch (args.operationType) {
            case TYPES:
                return runTypes(args);
            case ROUTES:
                return runRoutes(args);
            case STOPS:
                return getScheduleProviderByName(args.route.providerId).runProvider(args);
            case SCHEDULE:
                return getScheduleProviderByName(args.stop.route.providerId).runProvider(args);
        }

        throw new ScheduleProviderException(ScheduleError.ErrorCode.INTERNAL_ERROR);
    }

    @Override
    public String getProviderId() {
        return "proxy";
    }

    @Override
    public String getProviderName(Context context) {
        return null;
    }
}
