package com.shalgachev.moscowpublictransport.data;

import android.os.AsyncTask;

import com.shalgachev.moscowpublictransport.data.providers.BaseScheduleProvider;

/**
 * Created by anton on 6/24/2017.
 */

public class ScheduleTask extends AsyncTask<Void, Void, BaseScheduleProvider.Result> {
    BaseScheduleProvider mProvider;
    private IScheduleReceiver mReceiver;

    public ScheduleTask(BaseScheduleProvider provider) {
        mProvider = provider;
    }

    public void setReceiver(IScheduleReceiver receiver) {
        mReceiver = receiver;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(BaseScheduleProvider.Result result) {
        mReceiver.onScheduleProviderExecuted(result);
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled(BaseScheduleProvider.Result result) {
        super.onCancelled(result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected BaseScheduleProvider.Result doInBackground(Void... params) {
        BaseScheduleProvider.Result result = mProvider.run();
        result.operationType = mProvider.getArgs().operationType;
        return result;
    }
    public interface IScheduleReceiver {
        void onScheduleProviderExecuted(BaseScheduleProvider.Result result);
    }
}
