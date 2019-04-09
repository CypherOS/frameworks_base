package com.android.systemui.privacy;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.android.systemui.R;

public enum PrivacyType {

    TYPE_CAMERA(R.string.privacy_type_camera, R.drawable.stat_sys_camera),
    TYPE_MICROPHONE(R.string.privacy_type_microphone, R.drawable.stat_sys_mic_none),
    TYPE_LOCATION(R.string.privacy_type_location, R.drawable.stat_sys_location);

    private final int iconId;
    private final int nameId;

    private PrivacyType(int i, int i2) {
        this.nameId = i;
        this.iconId = i2;
    }

    public String getName(Context context) {
        return context.getResources().getString(this.nameId);
    }

    public Drawable getIcon(Context context) {
        return context.getResources().getDrawable(this.iconId, context.getTheme());
    }
}
