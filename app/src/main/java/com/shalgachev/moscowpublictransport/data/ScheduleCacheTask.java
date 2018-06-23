package com.shalgachev.moscowpublictransport.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.shalgachev.moscowpublictransport.data.db.ScheduleCacheSQLiteHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by anton on 3/18/2018.
 */

public class ScheduleCacheTask extends AsyncTask<Void, Void, ScheduleCacheTask.Result> {
    public static class Args
    {
        enum Operation
        {
            GET_SCHEDULE,
            SAVE_SCHEDULE,

            ADD_TO_MAIN_MENU,
            SYNCHRONIZE_STOPS_ON_MAIN_MENU,
            REMOVE_FROM_MAIN_MENU,
            GET_STOPS_ON_MAIN_MENU,

            ADD_WIDGET_SIMPLE_STOP,
            REMOVE_WIDGET_SIMPLE_STOP,
            GET_STOP_FOR_WIDGET_ID,
        }

        public Args(Operation operation) {
            this.operation = operation;
        }

        public static Args getSchedule(Stop stop) {
            Args args = new Args(Operation.GET_SCHEDULE);
            args.stop = stop;

            return args;
        }
        public static Args saveSchedule(Schedule schedule) {
            Args args = new Args(Operation.SAVE_SCHEDULE);
            args.schedule = schedule;

            return args;
        }
        public static Args addToMainMenu(Collection<Stop> stops) {
            Args args = new Args(Operation.ADD_TO_MAIN_MENU);
            args.stops = new ArrayList<>(stops);

            return args;
        }
        public static Args synchronizeStopsOnMainMenu(Collection<StopListItem> stops) {
            Args args = new Args(Operation.SYNCHRONIZE_STOPS_ON_MAIN_MENU);
            args.selectableStops = new ArrayList<>(stops);

            return args;
        }
        public static Args removeFromMainMenu(Collection<Stop> stops) {
            Args args = new Args(Operation.REMOVE_FROM_MAIN_MENU);
            args.stops = new ArrayList<>(stops);

            return args;
        }
        public static Args getStopsOnMainMenu(TransportType type) {
            Args args = new Args(Operation.GET_STOPS_ON_MAIN_MENU);
            args.type = type;

            return args;
        }

        public static Args addWidgetSimpleStop(Stop stop, int widgetId) {
            Args args = new Args(Operation.ADD_WIDGET_SIMPLE_STOP);
            args.stop = stop;
            args.widgetId = widgetId;

            return args;
        }

        public static Args removeWidgetSimpleStop(int widgetId) {
            Args args = new Args(Operation.REMOVE_WIDGET_SIMPLE_STOP);
            args.widgetId = widgetId;

            return args;
        }

        public static Args getStopForWidgetId(int widgetId) {
            Args args = new Args(Operation.GET_STOP_FOR_WIDGET_ID);
            args.widgetId = widgetId;

            return args;
        }

        Operation operation;

        TransportType type;
        Stop stop;
        List<Stop> stops;
        List<StopListItem> selectableStops;
        Schedule schedule;
        int widgetId;
    }

    public static class Result
    {
        public Schedule schedule;
        public List<Stop> stops;
        public Stop stop;
        public int stopsSaved;
        public int stopsDeleted;

        // TODO: 3/18/2018 add error codes
    }

    private Context mContext;
    private Args mArgs;
    private IScheduleReceiver mReceiver;

    public ScheduleCacheTask(Context context, Args args, IScheduleReceiver receiver) {
        mContext = context.getApplicationContext();
        mArgs = args;
        mReceiver = receiver;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Result result) {
        if (mReceiver != null)
            mReceiver.onResult(result);
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled(Result result) {
        super.onCancelled(result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected Result doInBackground(Void... voids) {
        Result result = new Result();

        ScheduleCacheSQLiteHelper db = new ScheduleCacheSQLiteHelper(mContext);

        switch (mArgs.operation) {
            case GET_SCHEDULE:
                result.schedule = db.getSchedule(mArgs.stop);
                break;
            case SAVE_SCHEDULE:
                db.saveSchedule(mArgs.schedule);
                break;
            case ADD_TO_MAIN_MENU:
                // TODO: 3/18/2018 handle list of stops in one db call
                for (Stop stop : mArgs.stops)
                    db.addToMainMenu(stop);
                break;
            case SYNCHRONIZE_STOPS_ON_MAIN_MENU:
                List<Stop> savedStops = db.getStopsOnMainMenu();

                result.stopsSaved = 0;
                result.stopsDeleted = 0;

                for (StopListItem stopListItem : mArgs.selectableStops) {
                    Stop stop = stopListItem.stop;
                    boolean isStopSaved = savedStops.contains(stop);
                    if (stopListItem.selected && !isStopSaved) {
                        db.addToMainMenu(stop);
                        result.stopsSaved++;
                    } else if (!stopListItem.selected && isStopSaved) {
                        db.removeFromMainMenu(stop);
                        result.stopsDeleted++;
                    }
                }
                break;
            case REMOVE_FROM_MAIN_MENU:
                // TODO: 3/18/2018 handle list of stops in one db call
                for (Stop stop : mArgs.stops)
                    db.removeFromMainMenu(stop);
                break;
            case GET_STOPS_ON_MAIN_MENU:
                result.stops = db.getStopsOnMainMenu(mArgs.type);
                break;
            case ADD_WIDGET_SIMPLE_STOP:
                db.addStopToWidgetSimpleStop(mArgs.stop, mArgs.widgetId);
                break;
            case REMOVE_WIDGET_SIMPLE_STOP:
                db.removeStopToWidgetSimpleStop(mArgs.widgetId);
                break;
            case GET_STOP_FOR_WIDGET_ID:
                result.stop = db.getStopForWidgetId(mArgs.widgetId);
                break;
        }

        db.close();

        return result;
    }

    public interface IScheduleReceiver {
        void onResult(ScheduleCacheTask.Result result);
    }
}
