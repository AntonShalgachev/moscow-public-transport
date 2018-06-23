package com.shalgachev.moscowpublictransport.activities;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.data.ScheduleCacheTask;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link StopScheduleWidgetConfigureActivity StopScheduleWidgetConfigureActivity}
 */
public class StopScheduleWidget extends AppWidgetProvider {

    static void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager,
                                final int appWidgetId) {

        new ScheduleCacheTask(context.getApplicationContext(), ScheduleCacheTask.Args.getStopForWidgetId(appWidgetId), new ScheduleCacheTask.IScheduleReceiver() {
            @Override
            public void onResult(ScheduleCacheTask.Result result) {
                if (result.stop == null)
                    return;

                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stop_schedule_widget);
                views.setTextViewText(R.id.appwidget_text, result.stop.toString());

                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }).execute();
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

