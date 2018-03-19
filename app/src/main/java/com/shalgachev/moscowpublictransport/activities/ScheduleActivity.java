package com.shalgachev.moscowpublictransport.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.math.MathUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
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

import java.util.Calendar;

public class ScheduleActivity extends AppCompatActivity {
    private static class HourItemDividerDecoration extends RecyclerView.ItemDecoration {
        private static final String TAG = "HourItemDivider";
        private static final int[] ATTRS = new int[]{ android.R.attr.listDivider };

        private Drawable mDivider;
        private int mLeftMargin;

        public HourItemDividerDecoration(Context context) {
            final TypedArray a = context.obtainStyledAttributes(ATTRS);
            mDivider = a.getDrawable(0);
            if (mDivider == null) {
                Log.w(TAG, "@android:attr/listDivider was not set in the theme used for this "
                        + "DividerItemDecoration. Please set that attribute all call setDrawable()");
            }
            a.recycle();

            mLeftMargin = context.getResources().getDimensionPixelSize(R.dimen.schedule_item_minutes_left_margin);
        }

        @Override
        public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
            if (parent.getLayoutManager() == null || mDivider == null) {
                return;
            }

            canvas.save();
            final int left;
            final int right;

            if (parent.getClipToPadding()) {
                left = parent.getPaddingLeft() + mLeftMargin;
                right = parent.getWidth() - parent.getPaddingRight();
                canvas.clipRect(left, parent.getPaddingTop(), right,
                        parent.getHeight() - parent.getPaddingBottom());
            } else {
                left = mLeftMargin;
                right = parent.getWidth();
            }

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);

                Rect bounds = new Rect();
                parent.getDecoratedBoundsWithMargins(child, bounds);
                final int bottom = bounds.bottom + Math.round(child.getTranslationY());
                final int top = bottom - mDivider.getIntrinsicHeight();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
            canvas.restore();
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (mDivider == null) {
                outRect.set(0, 0, 0, 0);
                return;
            }

            int items = parent.getAdapter().getItemCount();
            int index = parent.getChildAdapterPosition(view);
            if (index != items - 1) {
                outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
            }
        }
    }

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
