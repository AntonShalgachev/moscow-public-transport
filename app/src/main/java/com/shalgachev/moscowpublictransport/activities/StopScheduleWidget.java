package com.shalgachev.moscowpublictransport.activities;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.data.ScheduleCacheTask;
import com.shalgachev.moscowpublictransport.data.ScheduleUtils;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.helpers.ExtraHelper;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link StopScheduleWidgetConfigureActivity StopScheduleWidgetConfigureActivity}
 */
public class StopScheduleWidget extends AppWidgetProvider {
    private static final String LOG_TAG = "StopScheduleWidget";

    static void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager,
                                final int appWidgetId) {

        new ScheduleCacheTask(context.getApplicationContext(), ScheduleCacheTask.Args.getStopForWidgetId(appWidgetId), new ScheduleCacheTask.IScheduleReceiver() {
            @Override
            public void onResult(ScheduleCacheTask.Result result) {
                Log.v(LOG_TAG, String.format("Widget (id %d) received stop %s", appWidgetId, result.stop));

                if (result.stop == null)
                    return;

                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stop_schedule_widget);

                inflateStop(context, views, result.stop);

                Intent intent = new Intent(context, ScheduleActivity.class);
                intent.putExtra(ExtraHelper.STOP_EXTRA, result.stop);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, 0);
                views.setOnClickPendingIntent(R.id.container, pendingIntent);

                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }).execute();
    }

    static void inflateStop(Context context, RemoteViews views, Stop stop) {
        views.setTextViewText(R.id.stop_direction, context.getString(R.string.saved_stop_direction_short, stop.direction.getTo()));
        views.setTextViewText(R.id.schedule_days, ScheduleUtils.scheduleDaysToString(context, stop.days));

        views.setImageViewResource(R.id.route_icon, ScheduleUtils.getTransportIcon(stop.route.transportType));
        views.setTextViewText(R.id.route_name, stop.route.name);

        views.setTextViewText(R.id.stop_name, stop.name);

        int backRes = ScheduleUtils.getTransportWidgetBack(stop.route.transportType);
        views.setInt(R.id.header_container, "setBackgroundResource", backRes);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(final Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            new ScheduleCacheTask(context.getApplicationContext(), ScheduleCacheTask.Args.removeWidgetSimpleStop(appWidgetId), new ScheduleCacheTask.IScheduleReceiver() {
                @Override
                public void onResult(ScheduleCacheTask.Result result) {

                }
            }).execute();
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

