package com.shalgachev.moscowpublictransport.activities;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.shalgachev.moscowpublictransport.data.Direction;
import com.shalgachev.moscowpublictransport.data.DummyScheduleProvider;
import com.shalgachev.moscowpublictransport.data.IScheduleProvider;
import com.shalgachev.moscowpublictransport.data.StopListItem;
import com.shalgachev.moscowpublictransport.data.TransportType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class AddTransportActivity extends AppCompatActivity {
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
            throw new IllegalArgumentException("Transport type is null");

        mScheduleProvider = new DummyScheduleProvider();

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
                if (mStopListItems != null) {
                    StringBuilder text = new StringBuilder("Following stops selected:\n");
                    for (StopListItem stopListItem : mStopListItems)
                        if (stopListItem.selected)
                            text.append(stopListItem.stop).append("\n");
                    Toast.makeText(this, text.toString(), Toast.LENGTH_SHORT).show();
                }
//                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
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

    private void loadRoutes() {
        List<CharSequence> routes = mScheduleProvider.getRoutes(mTransportType);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, routes);
        mRouteTextView.setAdapter(adapter);

        mRouteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_DONE){
                    mRoute = mRouteTextView.getText().toString();
                    loadDirections();
                }
                return false;
            }
        });
    }

    private void loadDirections() {
        Set<Direction> directionsSet = new TreeSet<>();

        for (CharSequence day : mScheduleProvider.getDaysMasks(mTransportType, mRoute)) {
            for (Direction direction : mScheduleProvider.getDirections(mTransportType, mRoute, day)) {
                directionsSet.add(direction);
            }
        }

        mDirections = new ArrayList<>(directionsSet);
        mDirectionIdx = 0;
        updateDirection();
        loadStops();
    }

    public void onClickSwapDirections(View view) {
        if (mDirections == null)
            return;

        mDirectionIdx = (mDirectionIdx + 1) % mDirections.size();
        updateDirection();
        loadStops();
    }

    private Direction getCurrentDirection() {
        return mDirections.get(mDirectionIdx);
    }

    private void updateDirection() {
        Direction direction = getCurrentDirection();
        mDirectionFromTextView.setText(direction.getFrom());
        mDirectionToTextView.setText(direction.getTo());
    }

    private void loadStops() {
        mStopListItems = new ArrayList<>();
        mPagerAdapter.reset();

        for (CharSequence daysMask : mScheduleProvider.getDaysMasks(mTransportType, mRoute)) {
            ArrayList<StopListItem> tabStopListItems = new ArrayList<>();
            for (CharSequence stop : mScheduleProvider.getStops(mTransportType, mRoute, daysMask, getCurrentDirection())) {
                StopListItem item = new StopListItem(mScheduleProvider.getProviderId(), mRoute, daysMask, getCurrentDirection(), stop, "Neva", false);
                mStopListItems.add(item);
                tabStopListItems.add(item);
            }

            mPagerAdapter.addTab(daysMask, tabStopListItems);
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
                throw new IllegalArgumentException("Unexpected transport type");
        }

        return transportData;
    }

    private class TransportData {
        private int titleExtraResource;
        private int iconBackgroundResource;
        private int iconImageResource;
    }

    private TransportType mTransportType;
    private IScheduleProvider mScheduleProvider;

    private AutoCompleteTextView mRouteTextView;
    private TextView mDirectionFromTextView;
    private TextView mDirectionToTextView;

    private CharSequence mRoute;
    private ArrayList<Direction> mDirections;
    private int mDirectionIdx;

    private ArrayList<StopListItem> mStopListItems;

    private StopListPagerAdapter mPagerAdapter;
}
