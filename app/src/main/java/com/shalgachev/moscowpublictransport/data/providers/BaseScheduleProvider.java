package com.shalgachev.moscowpublictransport.data.providers;

import android.content.Context;

import com.shalgachev.moscowpublictransport.data.Route;
import com.shalgachev.moscowpublictransport.data.Schedule;
import com.shalgachev.moscowpublictransport.data.ScheduleArgs;
import com.shalgachev.moscowpublictransport.data.ScheduleTask;
import com.shalgachev.moscowpublictransport.data.ScheduleType;
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
    protected static class ScheduleProviderException extends Exception {
        private Result.ErrorCode mError;

        public ScheduleProviderException(Result.ErrorCode error) {
            super("Schedule provider exception");
            mError = error;
        }

        public Result.ErrorCode getError() {
            return mError;
        }
    }

    private static Map<CharSequence, BaseScheduleProvider> mScheduleProviders;

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

    public static BaseScheduleProvider getUnitedProvider() {
        // TODO: 1/9/2018 get united schedule provider instead of mosgortrans
        return getScheduleProvider("mosgortrans");
    }

    public ScheduleTask createAndRunTask(ScheduleArgs args, ScheduleTask.IScheduleReceiver receiver) {
        ScheduleTask task = createTask();
        task.setArgs(args);
        task.setReceiver(receiver);
        task.execute();

        return task;
    }

    public ScheduleTask createTask() {
        return new ScheduleTask(this);
    }

    public abstract Result run(ScheduleArgs args);

    public abstract String getProviderId();
    public abstract String getProviderName(Context context);

    public enum OperationType {
        TYPES,
        ROUTES,
        STOPS,
        SCHEDULE
    }

    public static class Result {
        public enum ErrorCode {
            NONE,
            INTERNET_NOT_AVAILABLE,
            URL_FETCH_FAILED,
            INVALID_STOP,
            INVALID_SCHEDULE_URL,
            EMPTY_SCHEDULE,
            INTERNAL_ERROR,
        }

        public OperationType operationType;
        public List<TransportType> transportTypes;
        public List<Route> routes;
        public List<Stop> stops;
        public Schedule schedule;

        public ErrorCode errorCode = ErrorCode.NONE;
    }
}
