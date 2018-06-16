package com.shalgachev.moscowpublictransport.data;

import android.content.Context;

import com.shalgachev.moscowpublictransport.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anton on 3/11/2018.
 */

public class ScheduleError {
    public enum ErrorCode {
        INTERNET_NOT_AVAILABLE,
        URL_FETCH_FAILED,
        INVALID_STOP,
        INVALID_SCHEDULE_URL,
        EMPTY_SCHEDULE,
        NO_STOPS,
        INTERNAL_ERROR,
        PARSING_ERROR,
        NOT_IMPLEMENTED,
        API_OUTDATED,
        WRONG_PROVIDER,
    }

    private static final Map<ErrorCode, Integer> ERROR_DESCRIPTION_IDS;
    static {
        ERROR_DESCRIPTION_IDS = new HashMap<>();

        ERROR_DESCRIPTION_IDS.put(ErrorCode.INTERNET_NOT_AVAILABLE, R.string.schedule_error_internet_not_available);
        ERROR_DESCRIPTION_IDS.put(ErrorCode.INVALID_SCHEDULE_URL, R.string.schedule_error_invalid_schedule_url);
    }

    public ScheduleError(ErrorCode code) {
        this.code = code;
    }

    public String localizedDescription(Context context) {
        if (ERROR_DESCRIPTION_IDS.containsKey(code)) {
            return context.getString(ERROR_DESCRIPTION_IDS.get(code));
        } else {
            return context.getString(R.string.schedule_error_unknown, code);
        }
    }

    public ErrorCode code;
}
