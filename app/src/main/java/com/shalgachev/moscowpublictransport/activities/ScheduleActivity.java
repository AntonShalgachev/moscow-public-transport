package com.shalgachev.moscowpublictransport.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.math.MathUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.shalgachev.moscowpublictransport.HourItemDividerDecoration;
import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.TimeUpdater;
import com.shalgachev.moscowpublictransport.adapters.ScheduleHoursAdapter;
import com.shalgachev.moscowpublictransport.data.Schedule;
import com.shalgachev.moscowpublictransport.data.ScheduleError;
import com.shalgachev.moscowpublictransport.data.ScheduleUtils;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.helpers.ExtraHelper;

public class ScheduleActivity extends AppCompatActivity {
    private static final String LOG_TAG = "ScheduleActivity";
    private Stop mStop;
    // TODO: 3/11/2018 use progress bar
    private ProgressDialog mProgressDialog;

    private RecyclerView mContentRecyclerView;
    private ScheduleHoursAdapter mScheduleHoursAdapter;

    Schedule mSchedule;

    private Handler mUpdaterHandler;
    private Handler mUIHandler;

    private TimeUpdater mTimeUpdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContentRecyclerView = findViewById(R.id.schedule_container);

        mScheduleHoursAdapter = new ScheduleHoursAdapter(this);
        mContentRecyclerView.setAdapter(mScheduleHoursAdapter);

        mContentRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        mContentRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    View child = recyclerView.getChildAt(i);
                    View hour = child.findViewById(R.id.schedule_item_hour);

                    float verticalOffset = 0.0f;

                    if (i == 0) {
                        float y = child.getY();
                        float cardHeight = child.getHeight();
                        float hourHeight = hour.getHeight();

                        float verticalMargin = getResources().getDimensionPixelSize(R.dimen.schedule_item_hour_vertical_margin);
                        float maxOffset = cardHeight - 2 * verticalMargin - hourHeight;
                        verticalOffset = MathUtils.clamp(-y, 0.0f, maxOffset);
                    }

                    hour.setTranslationY(verticalOffset);
                }
            }
        });

        mContentRecyclerView.addItemDecoration(new HourItemDividerDecoration(this));

        initActivity();
        loadData();

        HandlerThread updaterThread = new HandlerThread("TimeUpdaterHandlerThread");
        updaterThread.start();
        mUpdaterHandler = new Handler(updaterThread.getLooper());
        mUIHandler = new Handler();
    }

    private void initActivity()
    {
        mStop = (Stop) getIntent().getSerializableExtra(ExtraHelper.STOP_EXTRA);

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
        Log.i(LOG_TAG, "Trying to fetch schedule from the database");
        showProgressDialog(R.string.loading_schedule);

        ScheduleUtils.requestSchedule(getApplicationContext(), mStop, new ScheduleUtils.IScheduleResultListener() {
            @Override
            public void onCachedSchedule(Schedule schedule) {
                if (mProgressDialog != null)
                    mProgressDialog.dismiss();

                onScheduleAvailable(schedule);
            }

            @Override
            public void onFreshSchedule(Schedule schedule) {
                if (mProgressDialog != null)
                    mProgressDialog.dismiss();

                onScheduleAvailable(schedule);
            }

            @Override
            public void onError(ScheduleError error) {
                if (mProgressDialog != null)
                    mProgressDialog.dismiss();

                onScheduleError(error);
            }
        });
    }

    private void onScheduleAvailable(Schedule schedule) {
        Log.d(LOG_TAG, "New schedule is now available");
        mSchedule = schedule;

        stopUpdater();

        mTimeUpdater = new TimeUpdater(schedule.getTimepoints());
        Log.d(LOG_TAG, "Creating new time updater " + mTimeUpdater.toString());

        mTimeUpdater.setListener(new TimeUpdater.Listener() {
            @Override
            public void onTimeUpdated(TimeUpdater timeUpdater, long millisToNextUpdate) {
                Log.d(LOG_TAG, String.format("Time updater finished: %s", timeUpdater.toString()));
                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mScheduleHoursAdapter.updateSchedule(mSchedule);
                    }
                });

                if (timeUpdater == mTimeUpdater) {
                    Log.i(LOG_TAG, String.format("Scheduling updater to run in %d ms", millisToNextUpdate));
                    mUpdaterHandler.postDelayed(mTimeUpdater, millisToNextUpdate);
                } else {
                    Log.i(LOG_TAG, "Time updater changed, so not scheduling");
                }
            }
        });

        startUpdater();
    }

    private void onScheduleError(ScheduleError error) {
        Log.e(LOG_TAG, "Failed to retrieve schedule");

        Snackbar snackbar = Snackbar.make(mContentRecyclerView, error.localizedDescription(this), Snackbar.LENGTH_INDEFINITE);
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
        if (mSchedule == null && mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, getString(R.string.loading), getString(loadingStringId), true);
        }
    }

    void stopUpdater() {
        if (mTimeUpdater != null) {
            Log.d(LOG_TAG, "Pausing existing time updater " + mTimeUpdater.toString());
            mUpdaterHandler.removeCallbacks(mTimeUpdater);
        }
    }

    void startUpdater() {
        if (mTimeUpdater != null) {
            Log.d(LOG_TAG, "Starting time updater " + mTimeUpdater.toString());
            mUpdaterHandler.post(mTimeUpdater);
        } else {
            Log.i(LOG_TAG, "Time updater hasn't been created yet");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopUpdater();
    }

    @Override
    protected void onResume() {
        super.onResume();

        startUpdater();
    }
}
