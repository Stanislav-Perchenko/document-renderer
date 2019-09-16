package com.webssa.guestbest.utils;

import android.content.res.Resources;
import android.util.TypedValue;

public final class ViewUtils {

    public static float dp2px(Resources res, int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
    }

    private ViewUtils() { }
}
