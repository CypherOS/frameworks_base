package com.android.systemui.ambientplay;

import android.view.View;
import com.android.systemui.AutoReinflateContainer;
import com.android.systemui.ambientplay.AmbientIndicationContainer;

public class AmbientIndicationInflateListener implements AutoReinflateContainer.InflateListener {
    private Object mContainer;

    private void setAmbientIndicationView(View view) {
        ((AmbientIndicationContainer)mContainer).updateAmbientIndicationView(view);
    }

    public AmbientIndicationInflateListener(Object object) {
        mContainer = object;
    }

    @Override
    public void onInflated(View view) {
        setAmbientIndicationView(view);
    }
}