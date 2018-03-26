package com.shalgachev.moscowpublictransport;

import android.util.Log;

import com.shalgachev.moscowpublictransport.data.Schedule;
import com.shalgachev.moscowpublictransport.data.ScheduleUtils;

import java.util.Calendar;

/**
 * Created by anton on 3/22/2018.
 */

public class TimeUpdater implements Runnable {
    private static final String LOG_TAG = "TimeUpdater";
    private final Schedule.Timepoints mTimepoints;
    private Listener mListener;

    public interface Listener {
        void onTimeUpdated(TimeUpdater timeUpdater, long millisToNextUpdate);
    }

    public TimeUpdater(Schedule.Timepoints timepoints) {
        mTimepoints = timepoints;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public void run() {
        if (mTimepoints == null)
            return;

        synchronized (mTimepoints) {
            Log.i(LOG_TAG, "Updating time...");
            Calendar nowCalendar = Calendar.getInstance();

            final int maxCountdowns = 3;
            int shownCountdowns = 0;
            for (Schedule.Timepoint timepoint : mTimepoints.getTimepoints()) {
                Calendar timepointCalendar = ScheduleUtils.getTimepointCalendar(timepoint, mTimepoints.getFirstHour());

                timepoint.millisFromNow = (timepointCalendar.getTimeInMillis() - nowCalendar.getTimeInMillis());

                if (shownCountdowns < maxCountdowns && timepoint.isEnabled()) {
                    timepoint.isCountdownShown = true;
                    shownCountdowns += 1;
                } else {
                    timepoint.isCountdownShown = false;
                }
            }
        }

        Calendar nowCalendar = Calendar.getInstance();

        int seconds = nowCalendar.get(Calendar.SECOND);
        int milliseconds = nowCalendar.get(Calendar.MILLISECOND);

        long millisToNextUpdate = (60 - seconds) * 1000 - milliseconds;

        if (mListener != null)
            mListener.onTimeUpdated(this, millisToNextUpdate);
    }
}
