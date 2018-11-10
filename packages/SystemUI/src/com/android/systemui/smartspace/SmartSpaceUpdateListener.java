package com.android.systemui.smartspace;

public interface SmartSpaceUpdateListener {
    void onGsaChanged();

    void onSensitiveModeChanged(boolean z);

    void onSmartSpaceUpdated(SmartSpaceData smartSpaceData);
}
