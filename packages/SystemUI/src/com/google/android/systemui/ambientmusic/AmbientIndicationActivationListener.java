package com.google.android.systemui.ambientmusic;

import com.android.systemui.statusbar.phone.DoubleTapHelper;
import com.google.android.systemui.ambientmusic.AmbientIndicationContainer;

public class AmbientIndicationActivationListener
implements DoubleTapHelper.ActivationListener {
    private Object mContainer;

    private void updateActive(boolean bl) {
        ((AmbientIndicationContainer)this.mContainer).setActive(bl);
    }

    public AmbientIndicationActivationListener(Object object) {
        this.mContainer = object;
    }

    @Override
    public void onActiveChanged(boolean bl) {
        this.updateActive(bl);
    }
}

