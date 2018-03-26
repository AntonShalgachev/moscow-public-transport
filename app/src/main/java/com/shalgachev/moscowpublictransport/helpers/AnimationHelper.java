package com.shalgachev.moscowpublictransport.helpers;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

/**
 * Created by anton on 3/26/2018.
 */

public class AnimationHelper {
    public static ValueAnimator animateAlpha(final View view, float from, float to) {
        ValueAnimator anim = ValueAnimator.ofFloat(from, to);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setAlpha((float)animation.getAnimatedValue());
            }
        });

        anim.setInterpolator(new DecelerateInterpolator());

        return anim;
    }
    public static ValueAnimator animateTextColor(final TextView view, int from, int to) {
        ValueAnimator anim = ValueAnimator.ofObject(new ArgbEvaluator(), from, to);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                view.setTextColor((Integer)animator.getAnimatedValue());
            }
        });

        anim.setInterpolator(new DecelerateInterpolator());

        return anim;
    }
}
