package com.shalgachev.moscowpublictransport.data;

import android.content.Context;
import android.text.TextUtils;

import com.shalgachev.moscowpublictransport.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anton on 6/3/2017.
 */

public class ScheduleUtils {
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
}
