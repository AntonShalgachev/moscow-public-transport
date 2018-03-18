package com.shalgachev.moscowpublictransport.data;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.data.providers.BaseScheduleProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anton on 6/3/2017.
 */

public class ScheduleUtils {
    private static final String LOG_TAG = "ScheduleUtils";

    public static CharSequence daysMaskToString(Context context, CharSequence mask, boolean shortDays) {
        CharSequence result;
        if (mask.equals("1111111")) {
            result = context.getString(R.string.date_all);
        } else if (mask.equals("1111100")) {
            result = context.getString(R.string.date_weekdays);
        } else if (mask.equals("0000011")) {
            result = context.getString(R.string.date_weekends);
        } else {
            final int[] dayIds;
            final int[] dayIdsCapital;
            if (shortDays) {
                dayIds = new int[]{
                        R.string.date_monday_short,
                        R.string.date_tuesday_short,
                        R.string.date_wednesday_short,
                        R.string.date_thursday_short,
                        R.string.date_friday_short,
                        R.string.date_saturday_short,
                        R.string.date_sunday_short
                };
                dayIdsCapital = new int[]{
                        R.string.date_monday_short_capital,
                        R.string.date_tuesday_short_capital,
                        R.string.date_wednesday_short_capital,
                        R.string.date_thursday_short_capital,
                        R.string.date_friday_short_capital,
                        R.string.date_saturday_short_capital,
                        R.string.date_sunday_short_capital
                };
            } else {
                dayIds = new int[]{
                        R.string.date_monday,
                        R.string.date_tuesday,
                        R.string.date_wednesday,
                        R.string.date_thursday,
                        R.string.date_friday,
                        R.string.date_saturday,
                        R.string.date_sunday
                };
                dayIdsCapital = new int[]{
                        R.string.date_monday_capital,
                        R.string.date_tuesday_capital,
                        R.string.date_wednesday_capital,
                        R.string.date_thursday_capital,
                        R.string.date_friday_capital,
                        R.string.date_saturday_capital,
                        R.string.date_sunday_capital
                };
            }

            List<CharSequence> dayStrings = new ArrayList<>();
            for (int i = 0; i < mask.length(); i++) {
                if (mask.charAt(i) == '1') {
                    if (dayStrings.isEmpty()) {
                        dayStrings.add(context.getString(dayIdsCapital[i]));
                    } else {
                        dayStrings.add(context.getString(dayIds[i]));
                    }
                }
            }

            result = TextUtils.join(context.getString(R.string.date_delimiter), dayStrings);
        }

        // TODO: 6/3/2017 Capitalize first letter
        return result;
    }

    public static String formatShortTimeInterval(Context context, long minutes) {
        if (minutes < 0)
            return "";

        if (minutes < 60) {
            return context.getString(R.string.interval_min, minutes);
        } else {
            long hours = minutes / 60;
            return context.getString(R.string.interval_hour, hours);
        }
    };

    public static void requestSchedule(final Context context, final Stop stop, final IScheduleResultListener listener) {
        Log.i(LOG_TAG, String.format("Requested schedule for stop '%s'", stop.toString()));
        Log.i(LOG_TAG, String.format("Context: '%s'", context.toString()));

        new ScheduleCacheTask(context, ScheduleCacheTask.Args.getSchedule(stop), new ScheduleCacheTask.IScheduleReceiver() {
            @Override
            public void onResult(ScheduleCacheTask.Result result) {
                // TODO: 3/18/2018 use error codes instead
                if (result.schedule != null) {
                    Log.i(LOG_TAG, "Found saved schedule");
                    if (listener != null)
                        listener.onCachedSchedule(result.schedule);
                } else {
                    Log.i(LOG_TAG, "Schedule isn't saved");
                }

                Log.i(LOG_TAG, "Fetching schedule from net");

                BaseScheduleProvider.getUnitedProvider().createAndRunTask(
                        ScheduleArgs.asScheduleArgs(stop),
                        new ScheduleProviderTask.IScheduleReceiver() {
                            @Override
                            public void onScheduleProviderExecuted(BaseScheduleProvider.Result result) {
                                if (result.error == null) {
                                    Log.i(LOG_TAG, "Schedule fetched");
                                    if (listener != null)
                                        listener.onFreshSchedule(result.schedule);

                                    new ScheduleCacheTask(context, ScheduleCacheTask.Args.saveSchedule(result.schedule), new ScheduleCacheTask.IScheduleReceiver() {
                                        @Override
                                        public void onResult(ScheduleCacheTask.Result result) {
                                            Log.i(LOG_TAG, "Schedule saved");
                                        }
                                    }).execute();

                                } else {
                                    Log.e(LOG_TAG, "Error while refreshing schedule");
                                    if (listener != null)
                                        listener.onError(result.error);
                                }
                            }
                        }
                );
            }
        }).execute();
    }

    public interface IScheduleResultListener {
        void onCachedSchedule(Schedule schedule);
        void onFreshSchedule(Schedule schedule);
        void onError(ScheduleError error);
    }
}
