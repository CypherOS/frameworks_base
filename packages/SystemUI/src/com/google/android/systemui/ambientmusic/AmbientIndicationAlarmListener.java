package com.google.android.systemui.ambientmusic;

import android.app.AlarmManager;
import com.google.android.systemui.ambientmusic.AmbientIndicationService;

public class AmbientIndicationAlarmListener
implements AlarmManager.OnAlarmListener {
    private Object mContainer;

    private void hideContainer() {
        ((AmbientIndicationService)((Object)this.mContainer)).hideIndicationContainer();
    }

    public AmbientIndicationAlarmListener(Object object) {
        this.mContainer = object;
    }

    public void onAlarm() {
        this.hideContainer();
    }
}

