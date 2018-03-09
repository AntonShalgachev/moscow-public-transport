package com.shalgachev.moscowpublictransport.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.adapters.RouteListAdapter;
import com.shalgachev.moscowpublictransport.data.Route;
import com.shalgachev.moscowpublictransport.data.ScheduleArgs;
import com.shalgachev.moscowpublictransport.data.ScheduleTask;
import com.shalgachev.moscowpublictransport.data.SelectableRoute;
import com.shalgachev.moscowpublictransport.data.TransportType;
import com.shalgachev.moscowpublictransport.data.providers.BaseScheduleProvider;

import java.util.ArrayList;
import java.util.List;

public class RouteInputActivity extends AppCompatActivity {
    private static String EXTRA_TRANSPORT_TYPE = "com.shalgachev.moscowpublictransport.intent.TRANSPORT_TYPE";
    private static String EXTRA_ROUTE = "com.shalgachev.moscowpublictransport.intent.ROUTE";

    TransportType mTransportType;
    ProgressBar mProgressBar;
    RecyclerView mRouteList;
    FrameLayout mButtonsFrame;

    RouteListAdapter mRouteListAdapter;

    public static Intent createIntent(Activity activity, TransportType type) {
        Intent intent = new Intent(activity, RouteInputActivity.class);
        intent.putExtra(EXTRA_TRANSPORT_TYPE, type);

        return intent;
    }

    public static Route extractRoute(Intent resultIntent)
    {
        Bundle extras = resultIntent.getExtras();
        if (extras != null)
            return (Route) extras.getSerializable(EXTRA_ROUTE);

        return null;
    }

    private void finishActivity(boolean discard)
    {
        if (discard) {
            setResult(RESULT_CANCELED);
        } else {
            setResult(RESULT_OK, createResultIntent());
        }

        finish();
    }

    private Intent createResultIntent() {
        Route route = mRouteListAdapter.getSelectedRoute();

        Intent intent = new Intent();

        if (route != null)
            intent.putExtra(EXTRA_ROUTE, route);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
            mTransportType = (TransportType) extras.getSerializable(EXTRA_TRANSPORT_TYPE);

        if (mTransportType == null)
            throw new IllegalArgumentException("Transport transportType is null");

        setContentView(R.layout.activity_route_input);

        mProgressBar = findViewById(R.id.progress);
        mRouteList = findViewById(R.id.route_list);
        mButtonsFrame = findViewById(R.id.buttons);

        mRouteList.setItemAnimator(new DefaultItemAnimator());
        mRouteList.setLayoutManager(new LinearLayoutManager(this));

        mRouteListAdapter = new RouteListAdapter(this);
        mRouteListAdapter.setListener(new RouteListAdapter.Listener() {
            @Override
            public void onFilterApplied() {
                invalidateOptionsMenu();
            }
        });
        mRouteList.setAdapter(mRouteListAdapter);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        loadRoutes();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_route_input, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.route_input_done).setEnabled(mRouteListAdapter.getSelectedRoute() != null);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finishActivity(true);
                break;
            case R.id.route_input_done:
                finishActivity(false);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadRoutes() {
        BaseScheduleProvider scheduleProvider = BaseScheduleProvider.getTestScheduleProvider();
        scheduleProvider.setArgs(ScheduleArgs.asRoutesArgs(mTransportType));
        executeScheduleProvider(scheduleProvider);
    }

    public void executeScheduleProvider(BaseScheduleProvider scheduleProvider) {
        ScheduleTask task = scheduleProvider.createTask();
        task.setReceiver(new ScheduleTask.IScheduleReceiver() {
            @Override
            public void onScheduleProviderExecuted(BaseScheduleProvider.Result result) {
                mProgressBar.setVisibility(View.GONE);

                List<SelectableRoute> routes = new ArrayList<>();

                for (Route route : result.routes) {
                    routes.add(new SelectableRoute(route));
                }

                mRouteListAdapter.setAvailableRoutes(routes);
            }
        });
        task.execute();

        mProgressBar.setVisibility(View.VISIBLE);
    }
}
