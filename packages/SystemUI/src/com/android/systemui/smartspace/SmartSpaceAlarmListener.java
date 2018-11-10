package com.android.systemui.smartspace;

import android.app.AlarmManager.OnAlarmListener;

public final class SmartSpaceAlarmListener implements OnAlarmListener {

    private final SmartSpaceController mController;

    public SmartSpaceAlarmListener(SmartSpaceController smartSpaceController) {
        mController = smartSpaceController;
    }

    public final void onAlarm() {
        mController.onExpire(false);
    }
}
