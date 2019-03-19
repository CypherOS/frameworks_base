package com.android.systemui.doze.util;

import android.util.MathUtils;

public class AmbientBurnInHelper {

    public static final int getBurnInOffset(int offset, boolean isHoriz) {
        return (int) zigzag(((float) System.currentTimeMillis()) / 60000.0f, (float) offset, isHoriz ? 83.0f : 521.0f);
    }

    private static final float zigzag(float f, float f2, float f3) {
        float f4 = (float) 2;
        f = (f % f3) / (f3 / f4);
        if (f > ((float) 1)) {
            f = f4 - f;
        }
        return MathUtils.lerp(0.0f, f2, f);
    }
}
