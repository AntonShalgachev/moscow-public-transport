package com.shalgachev.moscowpublictransport.behaviors;

import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

public class TransitionTextViewBehavior extends TransitionViewBehavior<TextView> {
    private TextView mCollapsedView;
    private TextView mExpandedView;

    public TransitionTextViewBehavior(TextView collapsedView, TextView expandedView, Toolbar toolbar) {
        super(collapsedView, expandedView, toolbar);

        mCollapsedView = collapsedView;
        mExpandedView = expandedView;
    }

    @Override
    protected void updateViews(TextView child, float perc) {
        super.updateViews(child, perc);

        TextView fromView = mExpandedView;
        TextView toView = mCollapsedView;

        child.setTextSize(TypedValue.COMPLEX_UNIT_PX, lerp(fromView.getTextSize(), toView.getTextSize(), perc));


    }
}
