package com.shalgachev.moscowpublictransport.helpers;

import android.content.Context;
import android.widget.Toast;

import com.shalgachev.moscowpublictransport.R;

/**
 * Created by anton on 2/12/2018.
 */

public class ToastHelper {
    public static void showStopDeltaToast(Context context, int stopsSaved, int stopsDeleted) {
        if (stopsSaved > 0 || stopsDeleted > 0) {
            StringBuilder text = new StringBuilder();
            String prefix = "";
            if (stopsSaved > 0) {
                text.append(prefix).append(context.getString(R.string.toast_stops_saved, stopsSaved));
                prefix = "\n";
            }
            if (stopsDeleted > 0) {
                text.append(prefix).append(context.getString(R.string.toast_stops_deleted, stopsDeleted));
                prefix = "\n";
            }

            Toast.makeText(context, text.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
