package com.vanvatcorporation.vanvatsach.impl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;

import com.vanvatcorporation.vanvatsach.animations.ProgressBarAnimation;

public class ProgressBarImpl extends ProgressBar {

    public ProgressBarImpl(Context context) {
        super(context);
    }

    public ProgressBarImpl(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProgressBarImpl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ProgressBarImpl(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setProgressAnimate(int progress) {

        ProgressBarAnimation anim = new ProgressBarAnimation(this, getProgress(), progress);
        anim.setDuration(500);
        anim.setInterpolator(new DecelerateInterpolator());
        startAnimation(anim);
    }
}
