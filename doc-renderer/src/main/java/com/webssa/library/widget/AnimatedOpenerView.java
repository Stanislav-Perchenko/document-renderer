package com.webssa.library.widget;

import android.animation.Animator;
import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.webssa.guestbest.R;

/**
 * Created by stanislav.perchenko on 1/31/2018.
 */

public class AnimatedOpenerView extends LinearLayout {

    public enum State {
        OPENED, CLOSED, OPENING, CLOSING
    }

    public interface Callback {
        void onAnimationStart(float openPart);
        void onAnimationUpdate(float openPart);
        void onAnimationEnd(boolean isOpened);
    }

    public static Builder builder(Context ctx) {
        return new Builder(ctx);
    }

    public static class Builder {
        private AnimatedOpenerView instance;
        private Builder(Context ctx){
            instance = new AnimatedOpenerView(ctx, false);
        }

        public Builder setTitle(String title) {
            instance.argTitle = title;
            return this;
        }

        public Builder setPaddingVertical(int padding) {
            instance.argPaddingVertical = padding;
            return this;
        }

        public Builder setPaddingHorizontal(int padding) {
            instance.argPaddingHoriz = padding;
            return this;
        }

        public Builder setTextToMarkSpace(int space) {
            instance.argTextToMarkSpace = space;
            return this;
        }

        public Builder setAngleOpened(int angle) {
            instance.argAngleOpened = angle;
            return this;
        }

        public Builder setAngleClosed(int angle) {
            instance.argAngleClosed = angle;
            return this;
        }

        public Builder setInitialOpened(boolean opened) {
            instance.argIsInitialOpen = opened;
            return this;
        }

        public Builder setDuration(int duration) {
            instance.argDuration = duration;
            return this;
        }

        public Builder setFPS(int fps) {
            instance.argFPS = fps;
            return this;
        }

        public Builder setIsFullViewClickable(boolean isFullViewClickable) {
            instance.argIsFullViewClickable = isFullViewClickable;
            return this;
        }

        public AnimatedOpenerView build() {
            instance.init();
            return instance;
        }
    }


    private String argTitle = "";
    private int argAngleOpened = 0;
    private int argAngleClosed = 180;
    private boolean argIsInitialOpen;
    private int argDuration = 300;
    private int argFPS = 40;
    private Integer argPaddingVertical;
    private Integer argPaddingHoriz;
    private Integer argTextToMarkSpace;
    private boolean argIsFullViewClickable;


    public AnimatedOpenerView(Context context, boolean initHere) {
        super(context, null, 0);
        if (initHere) init();
    }

    public AnimatedOpenerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimatedOpenerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) extractCustomAttrs(attrs, defStyleAttr, 0);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AnimatedOpenerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (attrs != null) extractCustomAttrs(attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void extractCustomAttrs(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        Resources res = getContext().getResources();
        TypedArray a = res.obtainAttributes(attrs, R.styleable.AnimatedOpenerView);
        try {
            argTitle    = a.getString(R.styleable.AnimatedOpenerView_openerTitle);
            argAngleOpened = a.getInt(R.styleable.AnimatedOpenerView_angleOpened, 0);
            argAngleClosed = a.getInt(R.styleable.AnimatedOpenerView_angleClosed, 180);
            argIsInitialOpen = a.getBoolean(R.styleable.AnimatedOpenerView_isOpened, false);
            argDuration = a.getInt(R.styleable.AnimatedOpenerView_anim_duration, argDuration);
            argFPS = a.getInt(R.styleable.AnimatedOpenerView_anim_fps, argFPS);
        } finally {
            a.recycle();
        }
    }


    private void init() {
        setOrientation(HORIZONTAL);
        LayoutInflater.from(getContext()).inflate(R.layout.view_animated_opener, this, true);
        vRotation = findViewById(R.id.action_open_close);
        (vTitle = findViewById(android.R.id.title)).setText(argTitle);
        if (argIsInitialOpen) {
            state = State.OPENED;
            openPart = 1f;
        } else {
            state = State.CLOSED;
            openPart = 0;
        }

        final OnClickListener cl = v -> {
            boolean needNotify = false;
            switch (state) {
                case OPENED:
                    needNotify = true;
                case OPENING:
                    close();
                    break;
                case CLOSED:
                    needNotify = true;
                case CLOSING:
                    open();
                    break;
            }
            if (callback != null) callback.onAnimationStart(openPart);
        };

        if (argIsFullViewClickable) super.setOnClickListener(cl);
        else vRotation.setOnClickListener(cl);

        if (argPaddingVertical != null) {
            this.setPadding(0, argPaddingVertical, 0, argPaddingVertical);
        }
        if (argPaddingHoriz != null) {
            ((MarginLayoutParams) vTitle.getLayoutParams()).leftMargin = argPaddingHoriz;
            ((MarginLayoutParams) vRotation.getLayoutParams()).rightMargin = argPaddingHoriz;
        }
        if (argTextToMarkSpace != null) {
            ((MarginLayoutParams) vRotation.getLayoutParams()).leftMargin = argTextToMarkSpace;
        }

        valueAnimator = createAnimatorEngine();
        valueAnimator.addUpdateListener((ValueAnimator animation) -> onAnimationUpdated(((Float) animation.getAnimatedValue()).floatValue()));
        valueAnimator.addListener(mAnimListener);

        updateViewRotation();
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        if (argIsFullViewClickable) {
            throw new RuntimeException("Not supported in this mode");
        } else {
            super.setOnClickListener(l);
        }
    }

    private ValueAnimator createAnimatorEngine() {
        ValueAnimator va = new ValueAnimator();
        va.setInterpolator(new AccelerateDecelerateInterpolator());
        va.setEvaluator(new FloatEvaluator());
        va.setRepeatCount(0);
        va.setRepeatMode(ValueAnimator.RESTART);
        va.setDuration(argDuration);
        va.setFrameDelay(Math.round(1000.0 / argFPS));
        va.setStartDelay(0);
        //if (updateListener != null) va.addUpdateListener(updateListener);
        return va;
    }






    private State state;
    private float openPart;

    private ValueAnimator valueAnimator;

    private Callback callback;
    private TextView vTitle;
    private View vRotation;


    public TextView getTitleTextView() {
        return vTitle;
    }

    public void setCallback(@Nullable Callback callback) {
        this.callback = callback;
    }

    public State getState() {
        return state;
    }

    public float getOpenPart() {
        return openPart;
    }

    public boolean isOpened() {
        return (state == State.OPENED) || (state == State.OPENING);
    }

    private void open() {
        valueAnimator.cancel();
        valueAnimator.setFloatValues(openPart, 1f);
        valueAnimator.setDuration(Math.round(argDuration * (1f - openPart)));
        state = State.OPENING;
        valueAnimator.start();
    }

    private void close() {
        valueAnimator.cancel();
        valueAnimator.setFloatValues(openPart, 0f);
        valueAnimator.setDuration(Math.round(argDuration * openPart));
        state = State.CLOSING;
        valueAnimator.start();
    }


    private void onAnimationUpdated(float value) {
        openPart = value;
        updateViewRotation();
        if (callback != null) callback.onAnimationUpdate(value);
    }

    private final Animator.AnimatorListener mAnimListener = new Animator.AnimatorListener() {
        private boolean lastAnimCancelled;


        @Override
        public void onAnimationStart(Animator animation) {
            lastAnimCancelled = false;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (lastAnimCancelled) {
                lastAnimCancelled = true;
                return;
            }
            if (state == State.OPENING) {
                state = State.OPENED;
                openPart = 1f;
            } else if (state == State.CLOSING) {
                state = State.CLOSED;
                openPart = 0;
            }
            updateViewRotation();
            if (callback != null) callback.onAnimationEnd(state == State.OPENED);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            lastAnimCancelled = true;
        }

        @Override
        public void onAnimationRepeat(Animator animation) {}
    };

    private void updateViewRotation() {
        float r = argAngleOpened + (argAngleOpened + argAngleClosed)*openPart;
        vRotation.setRotation(r);
    }
}

