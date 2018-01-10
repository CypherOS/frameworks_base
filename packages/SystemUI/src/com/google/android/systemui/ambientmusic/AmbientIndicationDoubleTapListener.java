package com.google.android.systemui.ambientmusic;

import com.android.systemui.statusbar.phone.DoubleTapHelper;
import com.google.android.systemui.ambientmusic.AmbientIndicationContainer;

public class AmbientIndicationDoubleTapListener
implements DoubleTapHelper.DoubleTapListener {
    private Object mContainer;

    private boolean isDoubleTap() {
        return ((AmbientIndicationContainer)this.mContainer).getDoubleTap();
    }

    public AmbientIndicationDoubleTapListener(Object object) {
        this.mContainer = object;
    }

    @Override
    public boolean onDoubleTap() {
        return this.isDoubleTap();
    }
}

