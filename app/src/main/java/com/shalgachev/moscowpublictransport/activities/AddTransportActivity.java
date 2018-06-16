package com.shalgachev.moscowpublictransport.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.adapters.StopListPagerAdapter;
import com.shalgachev.moscowpublictransport.data.Direction;
import com.shalgachev.moscowpublictransport.data.Route;
import com.shalgachev.moscowpublictransport.data.ScheduleArgs;
import com.shalgachev.moscowpublictransport.data.ScheduleCacheTask;
import com.shalgachev.moscowpublictransport.data.ScheduleDays;
import com.shalgachev.moscowpublictransport.data.ScheduleError;
import com.shalgachev.moscowpublictransport.data.ScheduleProviderTask;
import com.shalgachev.moscowpublictransport.data.Season;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.data.StopListItem;
import com.shalgachev.moscowpublictransport.data.Stops;
import com.shalgachev.moscowpublictransport.data.TransportType;
import com.shalgachev.moscowpublictransport.data.providers.BaseScheduleProvider;
import com.shalgachev.moscowpublictransport.helpers.ExtraHelper;
import com.shalgachev.moscowpublictransport.helpers.ToastHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddTransportActivity extends AppCompatActivity {
    private static final String LOG_TAG = "AddTransportActivity";
    private static int REQUEST_ROUTE = 1;

    private TransportType mTransportType;
    private TextSwitcher mDirectionFromTextView;
    private TextSwitcher mDirectionToTextView;
    private Button mChooseRouteButton;
    private ImageButton mChangeDirectionButton;
    private RadioGroup mSeasonGroup;
    private TextView mSeasonTitle;
    private Route mRoute;
    private List<Direction> mDirections;
    private int mDirectionIdx;
    private Season mSelectedSeason = Season.ALL;
    private Stops mStops;
    private Map<Stop, StopListItem> mStopListItems;
    private StopListPagerAdapter mPagerAdapter;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transport);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null)
            mTransportType = (TransportType) extras.getSerializable(ExtraHelper.TRANSPORT_TYPE_EXTRA);

        if (mTransportType == null)
            throw new IllegalArgumentException("Transport transportType is null");

        initActivity();

        startRouteInputActivity();
    }

    private void startRouteInputActivity() {
        if (mRoute != null)
            startActivityForResult(RouteInputActivity.createIntent(this, mTransportType, mRoute.name), REQUEST_ROUTE);
        else
            startActivityForResult(RouteInputActivity.createIntent(this, mTransportType), REQUEST_ROUTE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ROUTE) {
            Log.d(LOG_TAG, "Route input finished");
            if (resultCode == Activity.RESULT_OK) {
                Route route = RouteInputActivity.extractRoute(data);

                if (route == null)
                    throw new AssertionError("Route shouldn't be null");

                if (route.equals(mRoute))
                    return;

                Log.d(LOG_TAG, String.format("Selected route: '%s'", route.toString()));

                mRoute = route;
                mChooseRouteButton.setText(mRoute.name);
                float size = getResources().getDimension(R.dimen.route_button_selected_text_size);
                mChooseRouteButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);

                loadStops();
            } else if (mRoute == null) {
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_transport, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.add_transport_done:
                saveStopsAndFinish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveStopsAndFinish() {
        if (mStopListItems == null)
            return;

        new ScheduleCacheTask(getApplicationContext(), ScheduleCacheTask.Args.synchronizeStopsOnMainMenu(mStopListItems.values()), new ScheduleCacheTask.IScheduleReceiver() {
            @Override
            public void onResult(ScheduleCacheTask.Result result) {
                ToastHelper.showStopDeltaToast(getApplicationContext(), result.stopsSaved, result.stopsDeleted);
                finish();
            }
        }).execute();
    }

    private void initActivity() {
        TransportData transportData = getTransportData();

        setTitle(getString(R.string.add_transport_title, getString(transportData.titleExtraResource)));

        ImageView transportIcon = findViewById(R.id.image_transport_icon);
        transportIcon.setImageResource(transportData.iconImageResource);

        ViewPager viewPager = findViewById(R.id.stops_container);
        mPagerAdapter = new StopListPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(mPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.days_tabs);
        tabLayout.setupWithViewPager(viewPager);

        mDirectionFromTextView = findViewById(R.id.text_direction_from);
        mDirectionToTextView = findViewById(R.id.text_direction_to);

        initTextSwitcher(mDirectionFromTextView);
        initTextSwitcher(mDirectionToTextView);

        mChooseRouteButton = findViewById(R.id.input_route_button);
        mChangeDirectionButton = findViewById(R.id.button_toggle_direction);

        mSeasonGroup = findViewById(R.id.season_group);
        mSeasonGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mSelectedSeason = Season.ALL;
                switch (checkedId) {
                    case R.id.season_winter:
                        mSelectedSeason = Season.WINTER;
                        break;
                    case R.id.season_summer:
                        mSelectedSeason = Season.SUMMER;
                        break;
                }

                updateStops();
            }
        });

        mSeasonTitle = findViewById(R.id.season_title);
    }

    void initTextSwitcher(TextSwitcher ts) {
        ts.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView textView = new TextView(AddTransportActivity.this);
                float size = getResources().getDimension(R.dimen.direction_name_size);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);

                textView.setGravity(Gravity.START);
                textView.setMaxWidth(getResources().getDimensionPixelSize(R.dimen.direction_name_max_width));
                return textView;
            }
        });

        Animation outAnim = AnimationUtils.loadAnimation(this, R.anim.slide_direction_out_bottom);
        Animation inAnim = AnimationUtils.loadAnimation(this, R.anim.slide_direction_in_top);

        outAnim.setInterpolator(new DecelerateInterpolator());
        inAnim.setInterpolator(new DecelerateInterpolator());

        ts.setOutAnimation(outAnim);
        ts.setInAnimation(inAnim);
    }

    private void onProviderError(ScheduleError error) {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();

        Snackbar snackbar = Snackbar.make(findViewById(R.id.container), error.localizedDescription(this), Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadStops();
            }
        });
        snackbar.show();
    }

    private void onStopsAvailable() {
        if (!mStops.hasStops()) {
            Log.e(LOG_TAG, "Failed to load stops: stops are empty");
        }

        mStopListItems = new HashMap<>();
        for (Stop stop : mStops.getAllStops()) {
            // TODO: 3/10/2018 implement next stop indicator
            StopListItem item = new StopListItem(stop, "<CHANGE ME PLZ>", false);
            mStopListItems.put(stop, item);
        }

        mDirections = mStops.getDirections();
        mDirectionIdx = 0;

        updateSeasons();
        updateDirection();

        new ScheduleCacheTask(getApplicationContext(), ScheduleCacheTask.Args.getStopsOnMainMenu(mTransportType), new ScheduleCacheTask.IScheduleReceiver() {
            @Override
            public void onResult(ScheduleCacheTask.Result result) {
                List<Stop> stopsOnMainMenu = result.stops;

                if (mProgressDialog != null)
                    mProgressDialog.dismiss();

                // TODO: 3/18/2018 handle errors

                for (Stop stop : stopsOnMainMenu) {
                    if (stop == null)
                        continue;

                    StopListItem stopListItem = mStopListItems.get(stop);
                    if (stopListItem == null)
                        continue;

                    stopListItem.selected = true;
                }

                updateStops();
            }
        }).execute();
    }

    private void loadStops() {
        // TODO: 3/10/2018 remove progress dialog; use progress bar
        mProgressDialog = ProgressDialog.show(this, getString(R.string.loading), getString(R.string.loading_stops));

        BaseScheduleProvider.getUnitedProvider().createAndRunTask(
                ScheduleArgs.asStopsArgs(mTransportType, mRoute),
                new ScheduleProviderTask.IScheduleReceiver() {
                    @Override
                    public void onScheduleProviderExecuted(BaseScheduleProvider.Result result) {
                        if (result.error == null) {
                            mStops = result.stops;
                            onStopsAvailable();
                        } else {
                            onProviderError(result.error);
                        }
                    }
                }
        );
    }

    public void onClickSwapDirections(View view) {
        if (mDirections == null)
            return;

        mDirectionIdx = (mDirectionIdx + 1) % mDirections.size();

        updateDirection();
        updateStops();

        mChangeDirectionButton.setRotation(0.0f);

        long animDuration = getResources().getInteger(R.integer.direction_transition_duration);
        mChangeDirectionButton.animate().rotation(360.0f).setDuration(animDuration).setInterpolator(new DecelerateInterpolator()).start();
    }

    public void onClickRouteInput(View view) {
        startRouteInputActivity();
    }

    private Direction getCurrentDirection() {
        if (mDirections == null)
            return null;
        if (mDirectionIdx >= mDirections.size())
            return null;

        return mDirections.get(mDirectionIdx);
    }

    private void updateDirection() {
        Direction currentDirection = getCurrentDirection();
        if (currentDirection == null)
            return;

        mDirectionFromTextView.setText(currentDirection.getFrom());
        mDirectionToTextView.setText(currentDirection.getTo());
    }

    private void updateSeasons() {
        boolean hasSpecialSeasons = false;
        for (ScheduleDays scheduleDays : mStops.getScheduleDays()) {
            if (scheduleDays.season != Season.ALL) {
                hasSpecialSeasons = true;
                break;
            }
        }

        if (hasSpecialSeasons) {
            // TODO: 6/13/2018 Choose actual for the current time season
            mSelectedSeason = Season.WINTER;
        } else {
            mSelectedSeason = Season.ALL;
        }

        mSeasonTitle.setVisibility(hasSpecialSeasons ? View.VISIBLE : View.GONE);
        mSeasonGroup.setVisibility(hasSpecialSeasons ? View.VISIBLE : View.GONE);

        int idToCheck = -1;
        switch (mSelectedSeason) {
            case WINTER:
                idToCheck = R.id.season_winter;
                break;
            case SUMMER:
                idToCheck = R.id.season_summer;
                break;
        }
        mSeasonGroup.check(idToCheck);
    }

    private void updateStops() {
        Direction currentDirection = getCurrentDirection();
        if (currentDirection == null)
            return;

        mPagerAdapter.reset();
        for (ScheduleDays scheduleDays : mStops.getScheduleDays()) {
            if (scheduleDays.season != mSelectedSeason)
                continue;

            List<Stop> stops = mStops.getStops(currentDirection, scheduleDays);

            List<StopListItem> stopListItems = new ArrayList<>();
            for (Stop stop : stops)
                stopListItems.add(mStopListItems.get(stop));

            mPagerAdapter.addTab(scheduleDays.daysMask, stopListItems);
        }
    }

    private TransportData getTransportData() {
        TransportData transportData = new TransportData();
        switch (mTransportType) {
            case BUS:
                transportData.titleExtraResource = R.string.add_bus_title_extra;
                transportData.iconImageResource = R.drawable.bus_in_circle;
                break;
            case TROLLEY:
                transportData.titleExtraResource = R.string.add_trolley_title_extra;
                transportData.iconImageResource = R.drawable.trolley_in_circle;
                break;
            case TRAM:
                transportData.titleExtraResource = R.string.add_tram_title_extra;
                transportData.iconImageResource = R.drawable.tram_in_circle;
                break;
            default:
                throw new IllegalArgumentException("Unexpected transport transportType");
        }

        return transportData;
    }

    private class TransportData {
        private
        @StringRes
        int titleExtraResource;
        private
        @DrawableRes
        int iconImageResource;
    }
}
