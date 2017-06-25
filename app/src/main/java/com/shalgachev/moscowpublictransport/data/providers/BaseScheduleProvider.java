package com.shalgachev.moscowpublictransport.data.providers;

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
    public class Result {
        public OperationType operationType;
        public List<TransportType> transportTypes;
        public List<CharSequence> routes;
        public List<Stop> stops;
        public Schedule schedule;
    }

    public enum OperationType {
        TYPES,
        ROUTES,
        STOPS,
        SCHEDULE
    }

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

    public void setArgs(ScheduleArgs args) {
        mArgs = args;
    }
    public ScheduleArgs getArgs() {
        return mArgs;
    }

    public ScheduleTask createTask() {
        return new ScheduleTask(this);
    }

    public abstract Result run();
    public abstract CharSequence getProviderId();

    private ScheduleArgs mArgs;

    private static Map<CharSequence, BaseScheduleProvider> mScheduleProviders;
}
