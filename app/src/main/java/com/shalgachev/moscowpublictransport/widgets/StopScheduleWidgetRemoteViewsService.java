package com.shalgachev.moscowpublictransport.widgets;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViewsService;

import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.helpers.ExtraHelper;

public class StopScheduleWidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Bundle extras = intent.getBundleExtra(ExtraHelper.BUNDLE_EXTRA);
        if (extras == null)
            return null;

        Stop stop = (Stop)extras.getSerializable(ExtraHelper.STOP_EXTRA);
        if (stop == null)
            return null;

        return new StopScheduleWidgetRemoteViewsFactory(getApplicationContext(), stop);
    }
}
