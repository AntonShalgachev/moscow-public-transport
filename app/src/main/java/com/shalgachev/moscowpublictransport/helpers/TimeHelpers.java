package com.shalgachev.moscowpublictransport.helpers;

import java.util.Calendar;

public class TimeHelpers {
    public static long millisUntilNextMinute() {
        Calendar nowCalendar = Calendar.getInstance();

        int seconds = nowCalendar.get(Calendar.SECOND);
        int milliseconds = nowCalendar.get(Calendar.MILLISECOND);

        return (60 - seconds) * 1000 - milliseconds;
    }
}
