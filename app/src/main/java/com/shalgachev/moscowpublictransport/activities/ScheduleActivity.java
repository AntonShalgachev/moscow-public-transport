package com.shalgachev.moscowpublictransport.activities;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.style.TtsSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.data.Schedule;
import com.shalgachev.moscowpublictransport.data.ScheduleArgs;
import com.shalgachev.moscowpublictransport.data.ScheduleTask;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.data.db.SavedStopsSQLiteHelper;
import com.shalgachev.moscowpublictransport.data.providers.BaseScheduleProvider;
import com.shalgachev.moscowpublictransport.helpers.ExtraHelper;

public class ScheduleActivity extends AppCompatActivity {
    private static final String LOG_TAG = "ScheduleActivity";
    private Stop mStop;
    private BaseScheduleProvider mScheduleProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initActivity();
        loadData();
    }

    private void initActivity()
    {
        mStop = (Stop) getIntent().getSerializableExtra(ExtraHelper.STOP_EXTRA);

        TextView tempContent = findViewById(R.id.temp_content);

        if (tempContent != null)
            tempContent.setText(mStop.toString());

        setTitle(mStop.route);

        AppBarLayout appBar = findViewById(R.id.app_bar);
        ImageView image = appBar.findViewById(R.id.image);

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

        // TODO: 1/9/2018 Remove this
        TextView tempTitle = appBar.findViewById(R.id.temp_title);
        tempTitle.setText(mStop.providerId);
    }

    private void loadData()
    {
        final SavedStopsSQLiteHelper db = new SavedStopsSQLiteHelper(this);
        Log.i(LOG_TAG, "Trying to fetch schedule from the database");
        Schedule schedule = db.getSchedule(mStop);
        if (schedule != null) {
            Log.i(LOG_TAG, "Found saved schedule");
            onScheduleAvailable(schedule);
            return;
        }

        Log.i(LOG_TAG, "Schedule isn't saved, loading from net");

        // TODO: 1/8/2018 Test provider
        mScheduleProvider = BaseScheduleProvider.getTestScheduleProvider();

        // TODO: 1/9/2018 Test data
        mScheduleProvider.setArgs(ScheduleArgs.asScheduleArgs(mStop));
        ScheduleTask task = mScheduleProvider.createTask();
        task.setReceiver(new ScheduleTask.IScheduleReceiver() {
            @Override
            public void onScheduleProviderExecuted(BaseScheduleProvider.Result result) {
                if (result != null && result.schedule != null) {
                    onScheduleAvailable(result.schedule);
                    db.saveSchedule(result.schedule);
                } else {
                    onScheduleError();
                }
            }
        });
        task.execute();
    }

    private void onScheduleAvailable(Schedule schedule) {
        TextView tempContent = findViewById(R.id.temp_content);

        if (tempContent != null) {
            StringBuilder builder = new StringBuilder();

            for (Schedule.Timepoint timepoint : schedule.getTimepoints())
                builder.append(timepoint.hour).append(":").append(timepoint.minute).append("\n");

            tempContent.setText(builder.toString());
        }
    }

    private void onScheduleError() {
        Log.e(LOG_TAG, "Failed to retrieve schedule");
    }
}
