package com.shalgachev.moscowpublictransport.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.math.MathUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.shalgachev.moscowpublictransport.HourItemDividerDecoration;
import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.TimeUpdater;
import com.shalgachev.moscowpublictransport.adapters.ScheduleHoursAdapter;
import com.shalgachev.moscowpublictransport.behaviors.TransitionTextViewBehavior;
import com.shalgachev.moscowpublictransport.behaviors.TransitionViewBehavior;
import com.shalgachev.moscowpublictransport.data.Schedule;
import com.shalgachev.moscowpublictransport.data.ScheduleError;
import com.shalgachev.moscowpublictransport.data.ScheduleUtils;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.helpers.ExtraHelper;
import com.shalgachev.moscowpublictransport.helpers.TimeHelpers;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class ScheduleActivity extends AppCompatActivity {
    private static final String LOG_TAG = "ScheduleActivity";

    private static final float TOP_CONTAINER_THRESHOLD = 0.4f;

    private Stop mStop;
    // TODO: 3/11/2018 use progress bar
    private ProgressDialog mProgressDialog;

    private RecyclerView mContentRecyclerView;
    private LinearLayoutManager mContentLayoutManager;
    private ScheduleHoursAdapter mScheduleHoursAdapter;
    private Toolbar mToolbar;

    private ImageView mTransportIcon;
    private TextView mTitleView;
    private TextView mSubtitleView;

    private List<TextView> mTitleViews = new ArrayList<>();
    private List<TextView> mSubtitleViews = new ArrayList<>();

    Schedule mSchedule;
    boolean mScheduleUpdated;

    private Handler mUpdaterHandler;
    private Handler mUIHandler;

    private TimeUpdater mTimeUpdater;

    private boolean mTopContainerIsShown = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mContentRecyclerView = findViewById(R.id.schedule_container);

        final RecyclerView.RecycledViewPool minutesPool = new RecyclerView.RecycledViewPool();
        minutesPool.setMaxRecycledViews(0, 100);
        mScheduleHoursAdapter = new ScheduleHoursAdapter(this, minutesPool);
        mContentRecyclerView.setAdapter(mScheduleHoursAdapter);
        mContentRecyclerView.getRecycledViewPool().setMaxRecycledViews(0, 30);

        mContentLayoutManager = new LinearLayoutManager(this);
        mContentRecyclerView.setLayoutManager(mContentLayoutManager);

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
        initBehaviors();
        loadData();

        HandlerThread updaterThread = new HandlerThread("TimeUpdaterHandlerThread");
        updaterThread.start();
        mUpdaterHandler = new Handler(updaterThread.getLooper());
        mUIHandler = new Handler();
    }

    private void initActivity()
    {
        mStop = (Stop) getIntent().getSerializableExtra(ExtraHelper.STOP_EXTRA);

        AppBarLayout appBar = findViewById(R.id.app_bar);
        final View topContainer = findViewById(R.id.expanded_top_container);

        appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float maxScrollSize = appBarLayout.getTotalScrollRange();

                float percentage = (Math.abs((float)verticalOffset)) / maxScrollSize;

                Log.d(LOG_TAG, String.format("Percentage: %f", percentage));

                if (percentage >= TOP_CONTAINER_THRESHOLD && mTopContainerIsShown) {
                    mTopContainerIsShown = false;

                    topContainer.animate()
                            .alpha(0)
                            .setDuration(200)
                            .start();
                }

                if (percentage < TOP_CONTAINER_THRESHOLD && !mTopContainerIsShown) {
                    mTopContainerIsShown = true;

                    topContainer.animate()
                            .alpha(1)
                            .start();
                }
            }
        });

        mTransportIcon = findViewById(R.id.transport_icon);
        mTitleView = findViewById(R.id.title);
        mSubtitleView = findViewById(R.id.subtitle);

        TextView direction = findViewById(R.id.expanded_direction);
        TextView days = findViewById(R.id.expanded_days);

        addTextView(mTitleViews, R.id.expanded_title);
        addTextView(mTitleViews, R.id.collapsed_title);
        addTextView(mTitleViews, R.id.title);
        addTextView(mSubtitleViews, R.id.expanded_subtitle);
        addTextView(mSubtitleViews, R.id.collapsed_subtitle);
        addTextView(mSubtitleViews, R.id.subtitle);

        for (TextView view : mTitleViews)
            view.setText(mStop.route.name);
        for (TextView view : mSubtitleViews)
            view.setText(mStop.name);

        direction.setText(getString(R.string.saved_stop_direction_short, mStop.direction.getTo()));
        days.setText(ScheduleUtils.scheduleDaysToString(this, mStop.days));

        switch (mStop.route.transportType) {
            case BUS:
                mTransportIcon.setImageResource(R.drawable.bus);
                break;
            case TROLLEY:
                mTransportIcon.setImageResource(R.drawable.trolley);
                break;
            case TRAM:
                mTransportIcon.setImageResource(R.drawable.tram);
                break;
        }
    }

    private void addTextView(List<TextView> container, @IdRes int viewId)
    {
        TextView view = findViewById(viewId);
        container.add(view);
    }

    private void initBehavior(View view, @IdRes int expandedId, @IdRes int collapsedId)
    {
        View expandedView = findViewById(expandedId);
        View collapsedView = findViewById(collapsedId);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)view.getLayoutParams();
        if (view instanceof TextView && collapsedView instanceof TextView && expandedView instanceof TextView)
            params.setBehavior(new TransitionTextViewBehavior((TextView)collapsedView, (TextView)expandedView, mToolbar));
        else
            params.setBehavior(new TransitionViewBehavior<>(collapsedView, expandedView, mToolbar));
    }

    private void initBehaviors()
    {
        initBehavior(mTransportIcon, R.id.expanded_transport_icon, R.id.collapsed_transport_icon);
        initBehavior(mTitleView, R.id.expanded_title, R.id.collapsed_title);
        initBehavior(mSubtitleView, R.id.expanded_subtitle, R.id.collapsed_subtitle);
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
            public void onScheduleCached(boolean first) {
                if (first)
                    Toast.makeText(ScheduleActivity.this, R.string.schedule_cached_toast, Toast.LENGTH_LONG).show();
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

        if (mSchedule != null)
            Toast.makeText(this, R.string.schedule_refreshed_toast, Toast.LENGTH_LONG).show();

        mSchedule = schedule;
        mScheduleUpdated = false;

        stopUpdater();

        mTimeUpdater = new TimeUpdater(schedule.getTimepoints(), this);
        Log.d(LOG_TAG, "Creating new time updater " + mTimeUpdater.toString());

        mTimeUpdater.setListener(new TimeUpdater.Listener() {
            @Override
            public void onTimeUpdated(TimeUpdater timeUpdater) {
                Log.d(LOG_TAG, String.format("Time updater finished: %s", timeUpdater.toString()));
                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        boolean shouldAnimate = mScheduleUpdated;
                        mScheduleHoursAdapter.updateSchedule(mSchedule, shouldAnimate);
                        if (!mScheduleUpdated) {
                            int firstActiveHourPos = mSchedule.getTimepoints().firstActiveHourPos;
                            int offset = 0;
                            if (firstActiveHourPos > 1) {
                                firstActiveHourPos -= 1;
                                offset = 100;
                            }
                            mContentLayoutManager.scrollToPositionWithOffset(firstActiveHourPos, offset);
                        }

                        mScheduleUpdated = true;
                    }
                });

                if (timeUpdater == mTimeUpdater) {
                    long millisToNextUpdate = TimeHelpers.millisUntilNextMinute();
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
