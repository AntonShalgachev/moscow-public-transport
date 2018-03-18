package com.shalgachev.moscowpublictransport.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.adapters.SavedStopPagerAdapter;
import com.shalgachev.moscowpublictransport.adapters.SavedStopRecyclerViewAdapter;
import com.shalgachev.moscowpublictransport.data.ScheduleCacheTask;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.data.db.ScheduleCacheSQLiteHelper;
import com.shalgachev.moscowpublictransport.fragments.SavedStopFragment;
import com.shalgachev.moscowpublictransport.helpers.ExtraHelper;
import com.shalgachev.moscowpublictransport.helpers.ToastHelper;

import java.util.List;

public class RouteListActivity extends AppCompatActivity implements SavedStopRecyclerViewAdapter.ViewHolder.ItemIterationListener {
    static final String LOG_TAG = "RouteListActivity";
    private ActionModeCallback mActionModeCallback = new ActionModeCallback();
    private ActionMode mActionMode;

    ViewPager mViewPager;
    SavedStopPagerAdapter mPagerAdapter;

    int mCurrentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_route_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPagerAdapter = new SavedStopPagerAdapter(getSupportFragmentManager(), this);

        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mPagerAdapter);
        mCurrentPage = mViewPager.getCurrentItem();

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Log.d(LOG_TAG, String.format("Page selected: %d", position));
                if (mActionMode != null) {
                    mActionMode.finish();
                }

                mCurrentPage = position;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_route_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Snackbar.make(getWindow().getDecorView(), "Settings", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClicked(Stop stop, int position) {
        if (mActionMode != null) {
            toggleSelection(stop, position);
        } else {
            Intent intent = new Intent(this, ScheduleActivity.class);
            intent.putExtra(ExtraHelper.STOP_EXTRA, stop);

            startActivity(intent);
        }
    }

    @Override
    public void onItemLongClicked(Stop stop, int position) {
        if (mActionMode == null) {
            mActionMode = startSupportActionMode(mActionModeCallback);

            SavedStopRecyclerViewAdapter adapter = getCurrentRecyclerAdapter();
            if (adapter != null)
                adapter.enableSelecting(true);
        }

        toggleSelection(stop, position);
    }

    private void toggleSelection(Stop stop, int position) {
        SavedStopRecyclerViewAdapter adapter = getCurrentRecyclerAdapter();
        if (adapter == null)
            return;

        adapter.toggleSelection(position);
        updateActionMode();
    }

    private class ActionModeCallback implements ActionMode.Callback {
        @SuppressWarnings("unused")
        private final String TAG = ActionModeCallback.class.getSimpleName();

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate (R.menu.menu_route_list_selection, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            SavedStopRecyclerViewAdapter adapter = getCurrentRecyclerAdapter();
            switch (item.getItemId()) {
                case R.id.action_remove:
                    if (adapter != null) {
                        final List<Stop> selectedStops = getCurrentRecyclerAdapter().getSelectedStops();
                        Log.d(TAG, String.format("Removing %d saved stops", selectedStops.size()));

                        new ScheduleCacheTask(getApplicationContext(), ScheduleCacheTask.Args.removeFromMainMenu(selectedStops), new ScheduleCacheTask.IScheduleReceiver() {
                            @Override
                            public void onResult(ScheduleCacheTask.Result result) {
                                getCurrentRecyclerFragment().updateStops();

                                ToastHelper.showStopDeltaToast(getApplicationContext(), 0, selectedStops.size());
                            }
                        }).execute();

                        // TODO: 3/18/2018 WARNING this breaks animations in the stop list
                        mActionMode.finish();
                    }

                    return true;

                case R.id.action_select_all:
                    if (adapter != null) {
                        adapter.selectAll();
                    }
                    updateActionMode();
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            SavedStopRecyclerViewAdapter adapter = getCurrentRecyclerAdapter();
            // TODO: 3/18/2018 WARNING this breaks animations in the stop list
            if (adapter != null)
                adapter.enableSelecting(false);

            mActionMode = null;
        }
    }

    SavedStopFragment getCurrentRecyclerFragment() {
        return mPagerAdapter.getFragment(mCurrentPage);
    }

    SavedStopRecyclerViewAdapter getCurrentRecyclerAdapter()
    {
        SavedStopFragment fragment = getCurrentRecyclerFragment();
        if (fragment == null)
            return null;

        return fragment.getRecyclerAdapter();
    }

    void updateActionMode() {
        SavedStopRecyclerViewAdapter adapter = getCurrentRecyclerAdapter();
        if (adapter == null)
            return;

        int count = adapter.getSelectedItemCount();
        if (count == 0) {
            mActionMode.finish();
        } else {
            mActionMode.setTitle(getString(R.string.action_mode_title, count));
            mActionMode.invalidate();
        }
    }
}
