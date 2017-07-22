package com.shalgachev.moscowpublictransport.activities;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.adapters.StopListPagerAdapter;
import com.shalgachev.moscowpublictransport.data.db.SavedStopsSQLiteHelper;
import com.shalgachev.moscowpublictransport.data.providers.BaseScheduleProvider;
import com.shalgachev.moscowpublictransport.data.Direction;
import com.shalgachev.moscowpublictransport.data.ScheduleArgs;
import com.shalgachev.moscowpublictransport.data.ScheduleTask;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.data.StopListItem;
import com.shalgachev.moscowpublictransport.data.TransportType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AddTransportActivity extends AppCompatActivity implements ScheduleTask.IScheduleReceiver {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transport);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        mTransportType = (TransportType) getIntent().getExtras().getSerializable("transport_type");
        if (mTransportType == null)
            throw new IllegalArgumentException("Transport transportType is null");

        mScheduleProvider = BaseScheduleProvider.getScheduleProvider("mosgortrans");

        initActivity();

        loadRoutes();
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
        List<Stop> savedStops = db.getStops();

        int stopsSaved = 0;
        int stopsDeleted = 0;

        for (StopListItem stopListItem : mStopListItems) {
            Stop stop = stopListItem.stop;
            boolean isStopSaved = savedStops.contains(stop);
            if (stopListItem.selected && !isStopSaved) {
                db.addStop(stop);
                stopsSaved++;
            } else if (!stopListItem.selected && isStopSaved) {
                db.deleteStop(stop);
                stopsDeleted++;
            }
        }

        db.close();

        if (stopsSaved > 0 || stopsDeleted > 0) {
            StringBuilder text = new StringBuilder();
            String prefix = "";
            if (stopsSaved > 0) {
                text.append(prefix).append(getString(R.string.toast_stops_saved, stopsSaved));
                prefix = "\n";
            }
            if (stopsDeleted > 0) {
                text.append(prefix).append(getString(R.string.toast_stops_deleted, stopsDeleted));
                prefix = "\n";
            }
            Toast.makeText(this, text.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void initActivity() {
        TransportData transportData = getTransportData();

        setTitle(getString(R.string.add_transport_title, getString(transportData.titleExtraResource)));

        ImageView transportIcon = (ImageView) findViewById(R.id.image_transport_icon);
        transportIcon.setBackgroundResource(transportData.iconBackgroundResource);
        transportIcon.setImageResource(transportData.iconImageResource);

        ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        mPagerAdapter = new StopListPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(mPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.days_tabs);
        tabLayout.setupWithViewPager(viewPager);

        mRouteTextView = (AutoCompleteTextView) findViewById(R.id.text_route);
        mDirectionFromTextView = (TextView) findViewById(R.id.text_direction_from);
        mDirectionToTextView = (TextView) findViewById(R.id.text_direction_to);
    }

    public void executeScheduleProvider(@StringRes int loadingStringId) {
        ScheduleTask task = mScheduleProvider.createTask();
        task.setReceiver(this);
        task.execute();

        mProgressDialog = ProgressDialog.show(this, getString(R.string.loading), getString(loadingStringId));
    }

    @Override
    public void onScheduleProviderExecuted(BaseScheduleProvider.Result result) {
        mProgressDialog.dismiss();

        switch (result.operationType) {
            case TYPES:
                break;
            case ROUTES:
                mRoutes = result.routes;
                onRoutesAvailable();
                break;
            case STOPS:
                mStops = result.stops;
                onStopsAvailable();
                break;
            case SCHEDULE:
                break;
        }
    }

    private void onRoutesAvailable() {
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, mRoutes);
        mRouteTextView.setAdapter(adapter);

        mRouteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_DONE){
                    mRoute = mRouteTextView.getText().toString();

                    loadStops();
                }
                return false;
            }
        });
    }

    private void onStopsAvailable() {
        if (mStops.isEmpty()) {
            showErrorMessage(R.string.error_loading_stops);
            return;
        }

        SavedStopsSQLiteHelper db = new SavedStopsSQLiteHelper(this);
        List<Stop> savedStops = db.getStops();

        Set<Direction> directions = new HashSet<>();
        mStopListItems = new ArrayList<>();
        for (Stop stop : mStops) {
            directions.add(stop.direction);

            StopListItem item = new StopListItem(stop, "Neva", false);
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

    private void loadRoutes() {
        mScheduleProvider.setArgs(ScheduleArgs.asRoutesArgs(mTransportType));
        executeScheduleProvider(R.string.loading_routes);
    }

    private void loadStops() {
        mScheduleProvider.setArgs(ScheduleArgs.asStopsArgs(mTransportType, mRoute));
        executeScheduleProvider(R.string.loading_stops);
    }

    public void onClickSwapDirections(View view) {
        if (mDirections == null)
            return;

        mDirectionIdx = (mDirectionIdx + 1) % mDirections.size();

        updateDirection();
        updateStops();
    }

    private Direction getCurrentDirection() {
        if (mDirections == null)
            return null;

        return mDirections.get(mDirectionIdx);
    }

    private void updateDirection() {
        Direction direction = getCurrentDirection();
        mDirectionFromTextView.setText(direction.getFrom());
        mDirectionToTextView.setText(direction.getTo());
    }

    private void updateStops() {
        Map<CharSequence, ArrayList<StopListItem>> stopMap = new HashMap<>();
        for (StopListItem item : mStopListItems) {
            if (getCurrentDirection().equals(item.stop.direction)) {
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
        private @StringRes int titleExtraResource;
        private @DrawableRes int iconBackgroundResource;
        private @DrawableRes int iconImageResource;
    }

    private TransportType mTransportType;
    private BaseScheduleProvider mScheduleProvider;

    private AutoCompleteTextView mRouteTextView;
    private TextView mDirectionFromTextView;
    private TextView mDirectionToTextView;

    private List<CharSequence> mRoutes;
    private CharSequence mRoute;
    private ArrayList<Direction> mDirections;
    private int mDirectionIdx;
    private List<Stop> mStops;

    private ArrayList<StopListItem> mStopListItems;

    private StopListPagerAdapter mPagerAdapter;

    private ProgressDialog mProgressDialog;
}
