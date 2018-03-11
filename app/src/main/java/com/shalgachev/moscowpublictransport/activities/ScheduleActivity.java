package com.shalgachev.moscowpublictransport.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.data.Schedule;
import com.shalgachev.moscowpublictransport.data.ScheduleArgs;
import com.shalgachev.moscowpublictransport.data.ScheduleError;
import com.shalgachev.moscowpublictransport.data.ScheduleTask;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.data.db.SavedStopsSQLiteHelper;
import com.shalgachev.moscowpublictransport.data.providers.BaseScheduleProvider;
import com.shalgachev.moscowpublictransport.helpers.ExtraHelper;

public class ScheduleActivity extends AppCompatActivity {
    private static final String LOG_TAG = "ScheduleActivity";
    private Stop mStop;
    // TODO: 3/11/2018 use progress bar
    private ProgressDialog mProgressDialog;
    private boolean mHasSchedule = false;

    // TODO: 2/18/2018 remove this
    private TextView mTempContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTempContent = findViewById(R.id.temp_content);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        initActivity();
        loadData();
    }

    private void initActivity()
    {
        mStop = (Stop) getIntent().getSerializableExtra(ExtraHelper.STOP_EXTRA);

        mTempContent.setText(mStop.toString());

        setTitle(mStop.route.name);

        AppBarLayout appBar = findViewById(R.id.app_bar);
        ImageView image = appBar.findViewById(R.id.transport_image);

        switch (mStop.transportType) {
            case BUS:
                image.setImageResource(R.drawable.bus);
                break;
            case TROLLEY:
                image.setImageResource(R.drawable.trolley);
                break;
            case TRAM:
                image.setImageResource(R.drawable.tram);
                break;
        }
    }

    private void loadData()
    {
        final SavedStopsSQLiteHelper db = new SavedStopsSQLiteHelper(this);
        Log.i(LOG_TAG, "Trying to fetch schedule from the database");
        Schedule schedule = db.getSchedule(mStop);
        if (schedule != null) {
            Log.i(LOG_TAG, "Found saved schedule");
            onScheduleAvailable(schedule);
        } else {
            Log.i(LOG_TAG, "Schedule isn't saved");
            showProgressDialog(R.string.loading_schedule);
        }

        Log.i(LOG_TAG, "Updating schedule from net");

        BaseScheduleProvider.getUnitedProvider().createAndRunTask(
                ScheduleArgs.asScheduleArgs(mStop),
                new ScheduleTask.IScheduleReceiver() {
                    @Override
                    public void onScheduleProviderExecuted(BaseScheduleProvider.Result result) {
                        if (mProgressDialog != null)
                            mProgressDialog.dismiss();

                        if (result.error == null) {
                            onScheduleAvailable(result.schedule);
                            db.saveSchedule(result.schedule);
                        } else {
                            onScheduleError(result.error);
                        }
                        db.close();
                    }
                }
        );
    }

    private void onScheduleAvailable(Schedule schedule) {
        mHasSchedule = true;

        StringBuilder builder = new StringBuilder();

        for (Schedule.Timepoint timepoint : schedule.getTimepoints())
            builder.append(timepoint.hour).append(":").append(timepoint.minute).append("\n");

        mTempContent.setText(builder.toString());
    }

    private void onScheduleError(ScheduleError error) {
        Log.e(LOG_TAG, "Failed to retrieve schedule");

        Snackbar snackbar = Snackbar.make(mTempContent, error.localizedDescription(this), Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 3/11/2018 load only from net upon retry
                loadData();
            }
        });
        snackbar.show();
    }

    private void showProgressDialog(@StringRes int loadingStringId) {
        if (!mHasSchedule && mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, getString(R.string.loading), getString(loadingStringId), true);
        }
    }
}
