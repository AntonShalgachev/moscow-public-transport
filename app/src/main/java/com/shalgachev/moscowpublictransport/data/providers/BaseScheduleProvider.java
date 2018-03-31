package com.shalgachev.moscowpublictransport.data.providers;

import android.content.Context;
import android.util.Log;

import com.shalgachev.moscowpublictransport.data.Route;
import com.shalgachev.moscowpublictransport.data.Schedule;
import com.shalgachev.moscowpublictransport.data.ScheduleArgs;
import com.shalgachev.moscowpublictransport.data.ScheduleError;
import com.shalgachev.moscowpublictransport.data.ScheduleProviderTask;
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
        private ScheduleError mError;

        public ScheduleProviderException(ScheduleError error) {
            mError = error;
        }

        public ScheduleProviderException(ScheduleError.ErrorCode errorCode) {
            mError = new ScheduleError(errorCode);
        }

        public ScheduleError getError() {
            return mError;
        }
    }

    public enum OperationType {
        TYPES,
        ROUTES,
        STOPS,
        SCHEDULE
    }

    public static class Result {
        public OperationType operationType;
        public List<TransportType> transportTypes;
        public List<Route> routes;
        public List<Stop> stops;
        public Schedule schedule;

        public ScheduleError error;
    }

    private static final String LOG_TAG = "BaseScheduleProvider";
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

    // TODO: 3/31/2018 Provider ID should be enum
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

    public ScheduleProviderTask createAndRunTask(ScheduleArgs args, ScheduleProviderTask.IScheduleReceiver receiver) {
        ScheduleProviderTask task = createTask();
        task.setArgs(args);
        task.setReceiver(receiver);
        task.execute();

        return task;
    }

    public ScheduleProviderTask createTask() {
        return new ScheduleProviderTask(this);
    }

    public final Result run(ScheduleArgs args) {
        try {
            Log.d(LOG_TAG, "Running schedule task");
            Result result = runProvider(args);
            Log.d(LOG_TAG, "Finished running task");
            return result;
        } catch (ScheduleProviderException e) {
            Log.e(LOG_TAG, String.format("Error was encountered: '%s'", e.getError().code));
            e.printStackTrace();

            Result result = new Result();
            result.error = e.getError();
            return result;
        }
    }
    public abstract Result runProvider(ScheduleArgs args) throws ScheduleProviderException;

    public abstract String getProviderId();
    public abstract String getProviderName(Context context);
}
