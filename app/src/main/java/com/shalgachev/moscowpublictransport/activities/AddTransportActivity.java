package com.shalgachev.moscowpublictransport.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.adapters.StopListPagerAdapter;
import com.shalgachev.moscowpublictransport.data.Direction;
import com.shalgachev.moscowpublictransport.data.Route;
import com.shalgachev.moscowpublictransport.data.ScheduleArgs;
import com.shalgachev.moscowpublictransport.data.ScheduleTask;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.data.StopListItem;
import com.shalgachev.moscowpublictransport.data.TransportType;
import com.shalgachev.moscowpublictransport.data.db.SavedStopsSQLiteHelper;
import com.shalgachev.moscowpublictransport.data.providers.BaseScheduleProvider;
import com.shalgachev.moscowpublictransport.helpers.ExtraHelper;
import com.shalgachev.moscowpublictransport.helpers.ToastHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AddTransportActivity extends AppCompatActivity {
    private static final String LOG_TAG = "AddTransportActivity";
    private static int REQUEST_ROUTE = 1;

    private TransportType mTransportType;
    private TextView mDirectionFromTextView;
    private TextView mDirectionToTextView;
    private Button mChooseRouteButton;
    private Route mRoute;
    private ArrayList<Direction> mDirections;
    private int mDirectionIdx;
    private List<Stop> mStops;
    private ArrayList<StopListItem> mStopListItems;
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

                Log.d(LOG_TAG, String.format("Selected route: '%s'", route.toString()));

                mRoute = route;
                mChooseRouteButton.setText(mRoute.name);
                float size = getResources().getDimension(R.dimen.route_button_selected_text_size);
                mChooseRouteButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);

                loadStops();
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
                saveStops();
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveStops() {
        if (mStopListItems == null)
            return;

        SavedStopsSQLiteHelper db = new SavedStopsSQLiteHelper(this);
        List<Stop> savedStops = db.getStopsOnMainMenu();

        int stopsSaved = 0;
        int stopsDeleted = 0;

        for (StopListItem stopListItem : mStopListItems) {
            Stop stop = stopListItem.stop;
            boolean isStopSaved = savedStops.contains(stop);
            if (stopListItem.selected && !isStopSaved) {
                db.addToMainMenu(stop);
                stopsSaved++;
            } else if (!stopListItem.selected && isStopSaved) {
                db.removeFromMainMenu(stop);
                stopsDeleted++;
            }
        }

        db.close();

        ToastHelper.showStopDeltaToast(this, stopsSaved, stopsDeleted);
    }

    private void initActivity() {
        TransportData transportData = getTransportData();

        setTitle(getString(R.string.add_transport_title, getString(transportData.titleExtraResource)));

        ImageView transportIcon = findViewById(R.id.image_transport_icon);
        transportIcon.setBackgroundResource(transportData.iconBackgroundResource);
        transportIcon.setImageResource(transportData.iconImageResource);

        ViewPager viewPager = findViewById(R.id.container);
        mPagerAdapter = new StopListPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(mPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.days_tabs);
        tabLayout.setupWithViewPager(viewPager);

        mDirectionFromTextView = findViewById(R.id.text_direction_from);
        mDirectionToTextView = findViewById(R.id.text_direction_to);

        mChooseRouteButton = findViewById(R.id.input_route_button);
    }

    public void executeScheduleTask(ScheduleTask task, @StringRes int loadingStringId) {
        task.setReceiver(new ScheduleTask.IScheduleReceiver() {
            @Override
            public void onScheduleProviderExecuted(BaseScheduleProvider.Result result) {
                if (mProgressDialog != null)
                    mProgressDialog.dismiss();

                if (result.operationType == BaseScheduleProvider.OperationType.STOPS) {
                    mStops = result.stops;
                    onStopsAvailable();
                } else {
                    Log.e(LOG_TAG, "Unexpected result type");
                }
            }
        });
        task.execute();

        // TODO: 3/10/2018 remove progress dialog; use progress bar
        mProgressDialog = ProgressDialog.show(this, getString(R.string.loading), getString(loadingStringId));
    }

    private void onStopsAvailable() {
        if (mStops.isEmpty()) {
            Log.e(LOG_TAG, "Failed to load stops: stops are empty");
            showErrorMessage(R.string.error_loading_stops);
            return;
        }

        SavedStopsSQLiteHelper db = new SavedStopsSQLiteHelper(this);
        List<Stop> savedStops = db.getStopsOnMainMenu();

        Set<Direction> directions = new HashSet<>();
        mStopListItems = new ArrayList<>();
        for (Stop stop : mStops) {
            directions.add(stop.direction);

            // TODO: 3/10/2018 implement next stop indicator
            StopListItem item = new StopListItem(stop, "<CHANGE ME PLZ>", false);
            mStopListItems.add(item);

            if (savedStops.contains(stop))
                item.selected = true;
        }

        mDirections = new ArrayList<>(directions);
        mDirectionIdx = 0;

        db.close();

        updateDirection();
        updateStops();
    }

    private void loadStops() {
        BaseScheduleProvider provider = BaseScheduleProvider.getInstance();
        ScheduleTask task = provider.createTask();
        task.setArgs(ScheduleArgs.asStopsArgs(mTransportType, mRoute));
        executeScheduleTask(task, R.string.loading_stops);
    }

    public void onClickSwapDirections(View view) {
        if (mDirections == null)
            return;

        mDirectionIdx = (mDirectionIdx + 1) % mDirections.size();

        updateDirection();
        updateStops();
    }

    public void onClickRouteInput(View view) {
        startRouteInputActivity();
    }

    private Direction getCurrentDirection() {
        if (mDirections == null)
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

    private void updateStops() {
        Direction currentDirection = getCurrentDirection();
        if (currentDirection == null)
            return;

        Map<CharSequence, ArrayList<StopListItem>> stopMap = new HashMap<>();
        for (StopListItem item : mStopListItems) {
            if (currentDirection.equals(item.stop.direction)) {
                if (!stopMap.containsKey(item.stop.daysMask))
                    stopMap.put(item.stop.daysMask, new ArrayList<StopListItem>());
                stopMap.get(item.stop.daysMask).add(item);
            }
        }

        mPagerAdapter.reset();
        for (Map.Entry<CharSequence, ArrayList<StopListItem>> entry : stopMap.entrySet()) {
            CharSequence daysMask = entry.getKey();
            ArrayList<StopListItem> items = entry.getValue();

            mPagerAdapter.addTab(daysMask, items);
        }
    }

    private TransportData getTransportData() {
        TransportData transportData = new TransportData();
        switch (mTransportType) {
            case BUS:
                transportData.titleExtraResource = R.string.add_bus_title_extra;
                transportData.iconBackgroundResource = R.drawable.bus_circle;
                transportData.iconImageResource = R.drawable.bus;
                break;
            case TROLLEY:
                transportData.titleExtraResource = R.string.add_trolley_title_extra;
                transportData.iconBackgroundResource = R.drawable.trolley_circle;
                transportData.iconImageResource = R.drawable.trolley;
                break;
            case TRAM:
                transportData.titleExtraResource = R.string.add_tram_title_extra;
                transportData.iconBackgroundResource = R.drawable.tram_circle;
                transportData.iconImageResource = R.drawable.tram;
                break;
            default:
                throw new IllegalArgumentException("Unexpected transport transportType");
        }

        return transportData;
    }

    private void showErrorMessage(@StringRes int messageId) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_NoActionBar);
        } else {
            builder = new AlertDialog.Builder(this);
        }

        builder.setTitle(R.string.error_title)
                .setMessage(messageId)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(R.drawable.ic_error_black_48dp)
                .show();
    }

    private class TransportData {
        private
        @StringRes
        int titleExtraResource;
        private
        @DrawableRes
        int iconBackgroundResource;
        private
        @DrawableRes
        int iconImageResource;
    }
}
