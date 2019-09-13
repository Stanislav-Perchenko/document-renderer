/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webssa.library.mypicasso;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.widget.ImageView;

import com.webssa.guestbest.BuildConfig;

import static android.graphics.Color.WHITE;
import static com.webssa.library.mypicasso.Picasso.LoadedFrom.MEMORY;

final class PicassoDrawable extends BitmapDrawable {
    // Only accessed from main thread.
    private static final float FADE_DURATION = 260f; //ms

    /**
     * Create or update the drawable on the target {@link ImageView} to display the supplied bitmap
     * image.
     */
    static void setBitmap(ImageView target, Context context, Bitmap bitmap, Picasso.LoadedFrom loadedFrom, boolean noFade) {
        Drawable placeholder = target.getDrawable();
        if (placeholder instanceof AnimationDrawable) {
            ((AnimationDrawable) placeholder).stop();
        }
        PicassoDrawable drawable = new PicassoDrawable(context, bitmap, placeholder, loadedFrom, noFade);
        target.setImageDrawable(drawable);
    }

    /**
     * Create or update the drawable on the target {@link ImageView} to display the supplied
     * placeholder image.
     */
    static void setPlaceholder(ImageView target, Drawable placeholderDrawable) {
        target.setImageDrawable(placeholderDrawable);
        if (target.getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) target.getDrawable()).start();
        }
    }

    //private final boolean debugging;
    private final float density;
    private final Picasso.LoadedFrom loadedFrom;

    private Bitmap mBmp;
    Drawable placeholder;

    long startTimeMillis;
    boolean animating;
    int alpha = 0xFF;

    PicassoDrawable(Context context, Bitmap bitmap, Drawable placeholder, Picasso.LoadedFrom loadedFrom, boolean noFade) {
        super(context.getResources(), bitmap);
        this.mBmp = bitmap;
        this.density = context.getResources().getDisplayMetrics().density;
        this.loadedFrom = loadedFrom;
        boolean fade = loadedFrom != MEMORY && !noFade;
        if (fade) {
            this.placeholder = placeholder;
            animating = true;
            startTimeMillis = SystemClock.uptimeMillis();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (mBmp == null || mBmp.isRecycled()) return;
        if (!animating) {
            super.draw(canvas);
        } else {
            float normalized = (SystemClock.uptimeMillis() - startTimeMillis) / FADE_DURATION;
            if (normalized >= 1f) {
                animating = false;
                placeholder = null;
                super.draw(canvas);
            } else {
                if (placeholder != null) {
                    placeholder.draw(canvas);
                }
                int partialAlpha = (int) (alpha * normalized);
                super.setAlpha(partialAlpha);
                super.draw(canvas);
                super.setAlpha(alpha);
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
                    invalidateSelf();
                }
            }
        }

        if (BuildConfig.DEBUG) drawDebugIndicator(canvas);
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = alpha;
        if (placeholder != null) placeholder.setAlpha(alpha);
        super.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (placeholder != null) placeholder.setColorFilter(cf);
        super.setColorFilter(cf);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        if (placeholder != null) placeholder.setBounds(bounds);
        super.onBoundsChange(bounds);
    }


    private Paint mDebugPaint;
    {
        if (BuildConfig.DEBUG) {
            mDebugPaint = new Paint();
            mDebugPaint.setStyle(Paint.Style.FILL);
        }

    }
    private void drawDebugIndicator(Canvas canvas) {
        mDebugPaint.setColor(WHITE);
        Path path = getTrianglePath(new Point(0, 0), (int) (16 * density));
        canvas.drawPath(path, mDebugPaint);
        mDebugPaint.setColor(loadedFrom.debugColor);
        path = getTrianglePath(new Point(0, 0), (int) (15 * density));
        canvas.drawPath(path, mDebugPaint);
    }
    private static Path getTrianglePath(Point p1, int width) {
        Point p2 = new Point(p1.x + width, p1.y);
        Point p3 = new Point(p1.x, p1.y + width);
        Path path = new Path();
        path.moveTo(p1.x, p1.y);
        path.lineTo(p2.x, p2.y);
        path.lineTo(p3.x, p3.y);
        return path;
    }
}
