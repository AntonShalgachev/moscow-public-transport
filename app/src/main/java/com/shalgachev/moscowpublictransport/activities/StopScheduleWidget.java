package com.shalgachev.moscowpublictransport.activities;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.data.Schedule;
import com.shalgachev.moscowpublictransport.data.ScheduleCacheTask;
import com.shalgachev.moscowpublictransport.data.ScheduleUtils;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.data.db.ScheduleCacheSQLiteHelper;
import com.shalgachev.moscowpublictransport.helpers.ExtraHelper;
import com.shalgachev.moscowpublictransport.widgets.StopScheduleWidgetRemoteViewsService;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link StopScheduleWidgetConfigureActivity StopScheduleWidgetConfigureActivity}
 */
public class StopScheduleWidget extends AppWidgetProvider {
    private static final String LOG_TAG = "StopScheduleWidget";

    static void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager,
                                final int appWidgetId) {
        ScheduleCacheSQLiteHelper db = null;

        try {
            db = new ScheduleCacheSQLiteHelper(context);

            Stop stop = db.getStopForWidgetId(appWidgetId);

            Log.v(LOG_TAG, String.format("Widget (id %d) received stop %s", appWidgetId, stop));

            if (stop == null)
                return;

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stop_schedule_widget);

            inflateStop(context, views, stop);

            Intent listIntent = new Intent(context, StopScheduleWidgetRemoteViewsService.class);
            Bundle extras = new Bundle();
            extras.putSerializable(ExtraHelper.STOP_EXTRA, stop);
            listIntent.putExtra(ExtraHelper.BUNDLE_EXTRA, extras);
            listIntent.setData(Uri.fromParts("content", String.valueOf(appWidgetId), null));
            views.setRemoteAdapter(R.id.timepoint_list, listIntent);

            // template to handle the click listener for each item
            Intent clickIntentTemplate = new Intent(context, ScheduleActivity.class);
            clickIntentTemplate.putExtra(ExtraHelper.STOP_EXTRA, stop);
            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(appWidgetId, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.timepoint_list, clickPendingIntentTemplate);

            Intent intent = new Intent(context, ScheduleActivity.class);
            intent.putExtra(ExtraHelper.STOP_EXTRA, stop);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, 0);
            views.setOnClickPendingIntent(R.id.container, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        } finally {
            if (db != null)
                db.close();
        }
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
        PendingResult pendingResult = goAsync();
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        pendingResult.finish();
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

    @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getAction();
        if (action != null && action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            ComponentName cn = new ComponentName(context, StopScheduleWidget.class);
            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.timepoint_list);
        }
        super.onReceive(context, intent);
    }
}

