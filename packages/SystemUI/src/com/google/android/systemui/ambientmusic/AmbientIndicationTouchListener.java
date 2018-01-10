package com.google.android.systemui.ambientmusic;

import android.view.MotionEvent;
import android.view.View;
import com.google.android.systemui.ambientmusic.AmbientIndicationContainer;

public class AmbientIndicationTouchListener
implements View.OnTouchListener {
    private Object mContainer;

    private boolean touchEventStatus(View view, MotionEvent motionEvent) {
        return ((AmbientIndicationContainer)this.mContainer).getTouchEvent(view, motionEvent);
    }

    public AmbientIndicationTouchListener(Object object) {
        this.mContainer = object;
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        return this.touchEventStatus(view, motionEvent);
    }
}

