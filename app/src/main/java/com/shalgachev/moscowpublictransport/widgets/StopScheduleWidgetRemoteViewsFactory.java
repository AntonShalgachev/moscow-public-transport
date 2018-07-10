package com.shalgachev.moscowpublictransport.widgets;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.support.annotation.DrawableRes;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.TimeUpdater;
import com.shalgachev.moscowpublictransport.data.Schedule;
import com.shalgachev.moscowpublictransport.data.ScheduleUtils;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.data.Timepoint;
import com.shalgachev.moscowpublictransport.data.Timepoints;
import com.shalgachev.moscowpublictransport.data.db.ScheduleCacheSQLiteHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StopScheduleWidgetRemoteViewsFactory implements RemoteViewsFactory {
    private static final String LOG_TAG = "StopScheduleWidgetRVF";

    private Context mContext;
    private Stop mStop;
    private Timepoints mTimepoints;

    public StopScheduleWidgetRemoteViewsFactory(Context context, Stop stop) {
        mContext = context.getApplicationContext();
        mStop = stop;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        ScheduleCacheSQLiteHelper db = null;

        try {
            db = new ScheduleCacheSQLiteHelper(mContext);

            Schedule schedule = db.getSchedule(mStop);
            if (schedule == null) {
                Log.w(LOG_TAG, String.format("No schedule is available for stop %s", mStop));
                return;
            }

            Timepoints allTimepoints = schedule.getTimepoints();

            new TimeUpdater(allTimepoints, mContext).run();

            List<Timepoint> activeTimepoints = new ArrayList<>();

            for (Timepoint timepoint : allTimepoints.getTimepoints()) {
                if (timepoint.isEnabled())
                    activeTimepoints.add(timepoint);
            }

            mTimepoints = new Timepoints(activeTimepoints, allTimepoints.getFirstHour());
        } finally {
            if (db != null)
                db.close();
        }
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mTimepoints != null ? mTimepoints.getTimepoints().size() : 0;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION || mTimepoints == null) {
            return null;
        }

        Timepoint timepoint = mTimepoints.getTimepoints().get(position);
        String time = String.format(Locale.US, "%02d:%02d", timepoint.hour, timepoint.minute);

        String timeLeft = ScheduleUtils.getCountdownText(mContext, timepoint, false);
        int timeLeftColor = ScheduleUtils.getCountdownColor(mContext, timepoint);

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_stop_schedule_item);

        rv.setTextViewText(R.id.timepoint_text, time);

        rv.setTextViewText(R.id.next_in_text, timeLeft);
        rv.setTextColor(R.id.next_in_text, timeLeftColor);
        rv.setImageViewResource(R.id.timepoint_icon, getScheduleItemDrawable(position));

        rv.setOnClickFillInIntent(R.id.timepoint_container, new Intent());

        return rv;
    }

    @DrawableRes int getScheduleItemDrawable(int position) {
        int count = getCount();

        if (count < 2)
            return R.drawable.schedule_item_single;

        if (position == 0)
            return R.drawable.schedule_item_top;
        if (position == count - 1)
            return R.drawable.schedule_item_bottom;

        return R.drawable.schedule_item_middle;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
