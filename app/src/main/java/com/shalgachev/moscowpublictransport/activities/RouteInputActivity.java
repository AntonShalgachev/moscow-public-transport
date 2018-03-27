package com.shalgachev.moscowpublictransport.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.AnimRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.adapters.RouteListAdapter;
import com.shalgachev.moscowpublictransport.data.Route;
import com.shalgachev.moscowpublictransport.data.ScheduleArgs;
import com.shalgachev.moscowpublictransport.data.ScheduleError;
import com.shalgachev.moscowpublictransport.data.ScheduleProviderTask;
import com.shalgachev.moscowpublictransport.data.TransportType;
import com.shalgachev.moscowpublictransport.data.providers.BaseScheduleProvider;
import com.shalgachev.moscowpublictransport.fragments.ButtonsFragment;

import java.util.List;

public class RouteInputActivity extends AppCompatActivity implements ButtonsFragment.OnFragmentInteractionListener {
    private static final int MAX_INPUT_LENGTH = 10;
    private static String EXTRA_TRANSPORT_TYPE = "com.shalgachev.moscowpublictransport.intent.TRANSPORT_TYPE";
    private static String EXTRA_ROUTE = "com.shalgachev.moscowpublictransport.intent.ROUTE";

    TransportType mTransportType;
    ProgressBar mProgressBar;
    RecyclerView mRouteList;
    TextView mRouteTextView;
    TextView mRoutePromptTextView;

    RouteListAdapter mRouteListAdapter;

    String mRouteInput = "";

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
        mRouteTextView = findViewById(R.id.route_input);
        mRoutePromptTextView = findViewById(R.id.route_input_prompt);

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

        addButtons();
        loadRoutes();
        onInputChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_route_input, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // TODO: 3/9/2018 do something more intuitive
        boolean hasSelectedRoute = mRouteListAdapter.getSelectedRoute() != null;
        menu.findItem(R.id.route_input_done).setVisible(hasSelectedRoute);

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
        mProgressBar.setVisibility(View.VISIBLE);

        BaseScheduleProvider.getUnitedProvider().createAndRunTask(
                ScheduleArgs.asRoutesArgs(mTransportType),
                new ScheduleProviderTask.IScheduleReceiver() {
                    @Override
                    public void onScheduleProviderExecuted(BaseScheduleProvider.Result result) {
                        mProgressBar.setVisibility(View.GONE);

                        if (result.error == null) {
                            onRoutesAvailable(result.routes);
                        } else {
                            onProviderError(result.error);
                        }
                    }
                }
        );
    }

    void onProviderError(ScheduleError error) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.container), error.localizedDescription(this), Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadRoutes();
            }
        });
        snackbar.show();
    }

    void onRoutesAvailable(List<Route> routes) {
        mRouteListAdapter.setAvailableRoutes(routes);
    }

    void addButtons() {
        ButtonsFragment fragment = ButtonsFragment.newInstance(ButtonsFragment.Type.Digits);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.buttons, fragment);
        transaction.commit();
    }

    void onInputChanged() {
        mRoutePromptTextView.setVisibility(mRouteInput.isEmpty() ? View.VISIBLE : View.INVISIBLE);
        mRouteTextView.setText(mRouteInput);
        mRouteListAdapter.filter(mRouteInput);
    }

    @Override
    public void onCharacterInput(CharSequence str) {
        if (mRouteInput.length() >= MAX_INPUT_LENGTH)
            return;

        mRouteInput += str;
        if (mRouteInput.length() > MAX_INPUT_LENGTH) {
            mRouteInput = mRouteInput.substring(0, MAX_INPUT_LENGTH - 1);
        }

        onInputChanged();
    }

    public void onCharacterDelete(View view) {
        if (mRouteInput.length() > 0) {
            mRouteInput = mRouteInput.substring(0, mRouteInput.length() - 1);
            onInputChanged();
        }
    }

    @Override
    public void onTransitionRequested(ButtonsFragment.Type currentType) {
        ButtonsFragment.Type nextType = ButtonsFragment.Type.Alpha;
        @AnimRes int enterAnim = R.anim.slide_in_left;
        @AnimRes int exitAnim = R.anim.slide_out_right;

        switch(currentType) {
            case Digits:
                nextType = ButtonsFragment.Type.Alpha;
                enterAnim = R.anim.slide_in_right;
                exitAnim = R.anim.slide_out_left;
                break;
            case Alpha:
                nextType = ButtonsFragment.Type.Digits;
                enterAnim = R.anim.slide_in_left;
                exitAnim = R.anim.slide_out_right;
                break;
        }

        ButtonsFragment fragment = ButtonsFragment.newInstance(nextType);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(enterAnim, exitAnim);
        transaction.replace(R.id.buttons, fragment);
        transaction.commit();
    }
}
