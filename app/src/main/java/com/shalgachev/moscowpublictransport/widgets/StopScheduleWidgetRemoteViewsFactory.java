package com.shalgachev.moscowpublictransport.widgets;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.support.annotation.DrawableRes;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.data.Schedule;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.data.Timepoint;
import com.shalgachev.moscowpublictransport.data.Timepoints;
import com.shalgachev.moscowpublictransport.data.db.ScheduleCacheSQLiteHelper;

import java.util.Locale;

public class StopScheduleWidgetRemoteViewsFactory implements RemoteViewsFactory {
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
            if (schedule == null)
                return;

            mTimepoints = schedule.getTimepoints();
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
        String time = String.format(Locale.US, "%d:%d", timepoint.hour, timepoint.minute);

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_stop_schedule_item);
        rv.setTextViewText(R.id.timepoint_text, time);

        @DrawableRes int timepointIconRes;
        if (position == 0)
            timepointIconRes = R.drawable.schedule_item_top;
        else if (position == getCount() - 1)
            timepointIconRes = R.drawable.schedule_item_bottom;
        else
            timepointIconRes = R.drawable.schedule_item_middle;
        rv.setImageViewResource(R.id.timepoint_icon, timepointIconRes);

        rv.setOnClickFillInIntent(R.id.timepoint_container, new Intent());

        return rv;
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
