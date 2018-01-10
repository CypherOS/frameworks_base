package com.google.android.systemui.ambientmusic;

import android.animation.ValueAnimator;
import com.google.android.systemui.ambientmusic.AmbientIndicationContainer;

public class AmbientIndicationAnimatorUpdateListener
implements ValueAnimator.AnimatorUpdateListener {
    private Object mContainer;

    private void updateAmbientIndicationAnimator(ValueAnimator valueAnimator) {
        ((AmbientIndicationContainer)this.mContainer).updateAnimator(valueAnimator);
    }

    public AmbientIndicationAnimatorUpdateListener(Object object) {
        this.mContainer = object;
    }

    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        this.updateAmbientIndicationAnimator(valueAnimator);
    }
}

