package com.google.android.systemui.ambientmusic;

import android.view.View;
import com.android.systemui.AutoReinflateContainer;
import com.google.android.systemui.ambientmusic.AmbientIndicationContainer;

public class AmbientIndicationInflateListener
implements AutoReinflateContainer.InflateListener {
    private Object mContainer;

    private void setAmbientIndicationView(View view) {
        ((AmbientIndicationContainer)this.mContainer).updateAmbientIndicationView(view);
    }

    public AmbientIndicationInflateListener(Object object) {
        this.mContainer = object;
    }

    @Override
    public void onInflated(View view) {
        this.setAmbientIndicationView(view);
    }
}

