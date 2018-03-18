package com.shalgachev.moscowpublictransport.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.adapters.ScheduleHoursAdapter;
import com.shalgachev.moscowpublictransport.data.Schedule;
import com.shalgachev.moscowpublictransport.data.ScheduleArgs;
import com.shalgachev.moscowpublictransport.data.ScheduleCacheTask;
import com.shalgachev.moscowpublictransport.data.ScheduleError;
import com.shalgachev.moscowpublictransport.data.ScheduleProviderTask;
import com.shalgachev.moscowpublictransport.data.ScheduleUtils;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.data.db.ScheduleCacheSQLiteHelper;
import com.shalgachev.moscowpublictransport.data.providers.BaseScheduleProvider;
import com.shalgachev.moscowpublictransport.helpers.ExtraHelper;

public class ScheduleActivity extends AppCompatActivity {
    private static final String LOG_TAG = "ScheduleActivity";
    private Stop mStop;
    // TODO: 3/11/2018 use progress bar
    private ProgressDialog mProgressDialog;
    private boolean mHasSchedule = false;

    private RecyclerView mContentRecyclerView;
    private ScheduleHoursAdapter mScheduleHoursAdapter;
    private LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContentRecyclerView = findViewById(R.id.schedule_container);

        mScheduleHoursAdapter = new ScheduleHoursAdapter(this);
        mContentRecyclerView.setAdapter(mScheduleHoursAdapter);

        mLayoutManager = new LinearLayoutManager(this);
        mContentRecyclerView.setLayoutManager(mLayoutManager);

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

        ScheduleUtils.requestSchedule(this, mStop, new ScheduleUtils.IScheduleResultListener() {
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
        mHasSchedule = true;

        mScheduleHoursAdapter.setSchedule(schedule);
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
        if (!mHasSchedule && mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, getString(R.string.loading), getString(loadingStringId), true);
        }
    }
}
