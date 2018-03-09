package com.shalgachev.moscowpublictransport.data.providers;

import android.content.Context;

import com.shalgachev.moscowpublictransport.data.Route;
import com.shalgachev.moscowpublictransport.data.Schedule;
import com.shalgachev.moscowpublictransport.data.ScheduleArgs;
import com.shalgachev.moscowpublictransport.data.ScheduleTask;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.data.TransportType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by anton on 5/28/2017.
 */

public abstract class BaseScheduleProvider {
    private static Map<CharSequence, BaseScheduleProvider> mScheduleProviders;
    private ScheduleArgs mArgs;

    private static void createScheduleProviders() {
        Set<BaseScheduleProvider> providers = new HashSet<>();
        providers.add(new DummyScheduleProvider());
        providers.add(new MosgortransScheduleProvider());

        mScheduleProviders = new HashMap<>();
        for (BaseScheduleProvider provider : providers) {
            mScheduleProviders.put(provider.getProviderId(), provider);
        }
    }

    public static BaseScheduleProvider getScheduleProvider(CharSequence id) {
        if (mScheduleProviders == null)
            createScheduleProviders();

        if (!mScheduleProviders.containsKey(id))
            throw new IllegalArgumentException(String.format("Invalid schedule provider: %s", id));

        return mScheduleProviders.get(id);
    }

    // TODO: 1/9/2018 Remove this
    public static BaseScheduleProvider getTestScheduleProvider() {
        return getScheduleProvider("mosgortrans");
    }

    public ScheduleArgs getArgs() {
        return mArgs;
    }

    public void setArgs(ScheduleArgs args) {
        mArgs = args;
    }

    public ScheduleTask createTask() {
        return new ScheduleTask(this);
    }

    public abstract Result run();

    public abstract CharSequence getProviderId();
    public abstract CharSequence getProviderName(Context context);

    public enum OperationType {
        TYPES,
        ROUTES,
        STOPS,
        SCHEDULE
    }

    public class Result {
        public OperationType operationType;
        public List<TransportType> transportTypes;
        public List<Route> routes;
        public List<Stop> stops;
        public Schedule schedule;
    }
}
