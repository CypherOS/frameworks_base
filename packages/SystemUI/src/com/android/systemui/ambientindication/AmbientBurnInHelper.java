/*
 * Copyright (C) 2019 CypherOS
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
 * limitations under the License
 */

package com.android.systemui.ambientindication;

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
