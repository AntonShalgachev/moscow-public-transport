package com.shalgachev.moscowpublictransport.data;

import android.content.Context;
import android.os.AsyncTask;

import com.shalgachev.moscowpublictransport.data.db.ScheduleCacheSQLiteHelper;

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
        }

        public static Args getSchedule(Stop stop) {
            Args args = new Args();
            args.operation = Operation.GET_SCHEDULE;
            args.stop = stop;

            return args;
        }

        public static Args saveSchedule(Schedule schedule) {
            Args args = new Args();
            args.operation = Operation.SAVE_SCHEDULE;
            args.schedule = schedule;

            return args;
        }

        Operation operation;

        Stop stop;
        Schedule schedule;
    }

    public static class Result
    {
        public Schedule schedule;

        // TODO: 3/18/2018 add error codes
    }

    private Context mContext;
    private Args mArgs;
    private IScheduleReceiver mReceiver;

    public ScheduleCacheTask(Context context, Args args, IScheduleReceiver receiver) {
        mContext = context;
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
        }

        db.close();

        return result;
    }

    public interface IScheduleReceiver {
        void onResult(ScheduleCacheTask.Result result);
    }
}
