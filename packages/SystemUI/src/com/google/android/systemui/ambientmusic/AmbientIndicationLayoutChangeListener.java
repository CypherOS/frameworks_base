package com.google.android.systemui.ambientmusic;

import android.view.View;
import com.google.android.systemui.ambientmusic.AmbientIndicationContainer;

public class AmbientIndicationLayoutChangeListener implements View.OnLayoutChangeListener {
    private Object mContainer;

    private void updateContainerBottomPadding() {
        ((AmbientIndicationContainer)this.mContainer).updateAmbientIndicationBottomPadding();
    }

    public AmbientIndicationLayoutChangeListener(Object object) {
        this.mContainer = object;
    }

    public void onLayoutChange(View view, int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8) {
        this.updateContainerBottomPadding();
    }
}

