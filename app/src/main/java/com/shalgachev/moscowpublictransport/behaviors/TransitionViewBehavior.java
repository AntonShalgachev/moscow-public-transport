package com.shalgachev.moscowpublictransport.behaviors;

import android.content.res.Resources;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.shalgachev.moscowpublictransport.R;

public class TransitionViewBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {
    private static final float HEIGHT_OFFSET_DP = 28.0f;

    private static final float ANIMATION_FROM_PERCENT = 0.1f;
    private static final float ANIMATION_TO_PERCENT = 0.9f;

    private View mCollapsedView;
    private View mExpandedView;
    private Toolbar mToolbar;

    private boolean mInitComplete = false;

    private float mMaxScrollOffset;

    int mFromX;
    int mFromY;
    int mToX;
    int mToY;

    int mFromWidth;
    int mFromHeight;
    int mToWidth;
    int mToHeight;

    public TransitionViewBehavior(V collapsedView, V expandedView, Toolbar toolbar) {
        mCollapsedView = collapsedView;
        mExpandedView = expandedView;

        mToolbar = toolbar;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, V child, View dependency) {
        return dependency instanceof AppBarLayout && dependency.getId() == R.id.app_bar;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, V child, View dependency) {
        if (!mInitComplete) {
            init(dependency);
            mInitComplete = true;
        }

        float scroll = Math.abs(dependency.getY());
        float perc = scroll / mMaxScrollOffset;

        Log.d("Behavior", String.format("Old percent: %f", perc));

        float from = ANIMATION_FROM_PERCENT;
        float to = ANIMATION_TO_PERCENT;
        perc = (perc - from) / (to - from);

        if (perc > 1.0f)
            perc = 1.0f;
        if (perc < 0.0f)
            perc = 0.0f;

        Log.d("Behavior", String.format("New percent: %f", perc));

        updateViews(child, perc);

        return true;
    }

    protected void updateViews(V child, float perc)
    {
        View toView = mCollapsedView;
        int[] toPos = new int[2];
        toView.getLocationOnScreen(toPos);
        mToX = toPos[0];
        mToY = toPos[1];
        mToWidth = toView.getWidth();
        mToHeight = toView.getHeight();

        child.setX(lerp(mFromX, mToX, perc));
        child.setY(lerp(mFromY, mToY, perc));

        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        lp.width = lerp(mFromWidth, mToWidth, perc);
        lp.height = lerp(mFromHeight, mToHeight, perc);
        child.setLayoutParams(lp);
    }

    private void init(View dependency)
    {
        mMaxScrollOffset = dependency.getHeight()
                - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, HEIGHT_OFFSET_DP, Resources.getSystem().getDisplayMetrics())
                - mToolbar.getHeight();

        View fromView = mExpandedView;
        View toView = mCollapsedView;

        int[] fromPos = new int[2];
        fromView.getLocationOnScreen(fromPos);

        mFromX = fromPos[0];
        mFromY = fromPos[1];

        mFromWidth = fromView.getWidth();
        mFromHeight = fromView.getHeight();
    }

    int lerp(int from, int to, float t)
    {
        return (int)(from + (to - from) * t);
    }

    float lerp(float from, float to, float t)
    {
        return from + (to - from) * t;
    }
}
