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
 * limitations under the License.
 */

package com.android.systemui.aoscp.privacy;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.android.systemui.R;

public enum PrivacyType {

    TYPE_CAMERA(R.string.privacy_type_camera, R.drawable.stat_sys_camera),
    TYPE_MICROPHONE(R.string.privacy_type_microphone, R.drawable.stat_sys_mic_none),
    TYPE_LOCATION(R.string.privacy_type_location, R.drawable.stat_sys_location);

    private final int iconId;
    private final int nameId;

    private PrivacyType(int nameId, int iconId) {
        this.nameId = nameId;
        this.iconId = iconId;
    }

    public String getName(Context context) {
        return context.getResources().getString(this.nameId);
    }

    public Drawable getIcon(Context context) {
        return context.getResources().getDrawable(this.iconId, context.getTheme());
    }
}