package com.shalgachev.moscowpublictransport.data;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.data.providers.BaseScheduleProvider;
import com.shalgachev.moscowpublictransport.helpers.StringUtils;

import org.jsoup.helper.StringUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by anton on 6/3/2017.
 */

public class ScheduleUtils {
    private static final String LOG_TAG = "ScheduleUtils";

    public static CharSequence daysMaskToString(Context context, CharSequence mask) {
        CharSequence result;
        if (mask.equals("1111111")) {
            result = context.getString(R.string.date_all);
        } else if (mask.equals("1111100")) {
            result = context.getString(R.string.date_weekdays);
        } else if (mask.equals("0000011")) {
            result = context.getString(R.string.date_weekends);
        } else {
            boolean shortDays = StringUtils.countMatches(mask, '1') > 1;

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

    public static String scheduleDaysToString(Context context, ScheduleDays days) {
        Season season = days.season;
        CharSequence maskStr = daysMaskToString(context, days.daysMask);

        if (season == Season.ALL)
            return context.getString(R.string.schedule_days_all_seasons, maskStr);

        String seasonStr = null;
        switch (season) {
            case WINTER:
                seasonStr = context.getString(R.string.season_winter);
                break;
            case SUMMER:
                seasonStr = context.getString(R.string.season_summer);
                break;
        }

        return context.getString(R.string.schedule_days_seasons, maskStr, seasonStr);
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
    }

    public static Calendar getTimepointCalendar(Schedule.Timepoint timepoint, int firstHour) {
        Calendar timepointCalendar = Calendar.getInstance();

        int currentHour = timepointCalendar.get(Calendar.HOUR_OF_DAY);

        int hourOffset = 0;

        // it's between 0 and 'firstHour' now, so the schedule should start a day before;
        if (currentHour < firstHour)
            hourOffset -= 24;

        // hour is between 0 and 'firstHour', thus it's the next day
        if (timepoint.hour < firstHour)
            hourOffset += 24;

        int hour = timepoint.hour + hourOffset;
        int minute = timepoint.minute;

        timepointCalendar.set(Calendar.HOUR_OF_DAY, 0);
        timepointCalendar.set(Calendar.MINUTE, 0);
        timepointCalendar.set(Calendar.SECOND, 0);
        timepointCalendar.set(Calendar.MILLISECOND, 0);

        timepointCalendar.add(Calendar.HOUR, hour);
        timepointCalendar.add(Calendar.MINUTE, minute+1);
        timepointCalendar.add(Calendar.MILLISECOND, -1);

        return timepointCalendar;
    }

    public static void requestSchedule(final Context context, final Stop stop, final IScheduleResultListener listener) {
        Log.i(LOG_TAG, String.format("Requested schedule for stop '%s'", stop.toString()));

        new ScheduleCacheTask(context, ScheduleCacheTask.Args.getSchedule(stop), new ScheduleCacheTask.IScheduleReceiver() {
            @Override
            public void onResult(ScheduleCacheTask.Result result) {
                final Schedule cachedSchedule = result.schedule;

                final boolean hasCached = cachedSchedule != null;
                // TODO: 3/18/2018 use error codes instead
                if (hasCached) {
                    Log.i(LOG_TAG, "Found saved schedule");
                    if (listener != null)
                        listener.onCachedSchedule(cachedSchedule);
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

                                    Schedule freshSchedule = result.schedule;
                                    if (cachedSchedule != null && freshSchedule != null && freshSchedule.equals(cachedSchedule)) {
                                        Log.i(LOG_TAG, "Schedule hasn't changed");
                                        return;
                                    }

                                    if (listener != null)
                                        listener.onFreshSchedule(freshSchedule);

                                    new ScheduleCacheTask(context, ScheduleCacheTask.Args.saveSchedule(freshSchedule), new ScheduleCacheTask.IScheduleReceiver() {
                                        @Override
                                        public void onResult(ScheduleCacheTask.Result result) {
                                            Log.i(LOG_TAG, "Schedule saved");
                                            if (listener != null)
                                                listener.onScheduleCached(!hasCached);
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
        void onScheduleCached(boolean first);
        void onError(ScheduleError error);
    }
}
