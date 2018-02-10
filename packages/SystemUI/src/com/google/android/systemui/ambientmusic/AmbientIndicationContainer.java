/*
 * Copyright (C) 2017-2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.systemui.ambientmusic;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import com.android.systemui.AutoReinflateContainer;
import com.android.systemui.Interpolators;
import com.android.systemui.doze.DozeReceiver;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.DoubleTapHelper;
import com.android.systemui.statusbar.phone.NotificationPanelView;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;

public class AmbientIndicationContainer extends AutoReinflateContainer implements DozeReceiver {
	
    private View mAmbientIndication;
    private DoubleTapHelper mDoubleTapHelper;
    private boolean mDozing;
    private ImageView mIcon;
    private CharSequence mIndication;
    private PendingIntent mIntent;
    private StatusBar mStatusBar;
    private TextView mText;
    private int mTextColor;
    private ValueAnimator mTextColorAnimator;
    private Context mContext;
    private final String TAG = "AmbientIndicationContainer";

    public AmbientIndicationContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mContext = context;
    }

    private boolean onDoubleTap() {
        if (mIntent != null) {
            mStatusBar.wakeUpIfDozing(SystemClock.uptimeMillis(), mAmbientIndication);
            mStatusBar.startPendingIntentDismissingKeyguard(mIntent);
            return true;
        }
        return false;
    }

    private void updateBottomPadding() {
        NotificationPanelView notificationPanelView = mStatusBar.getPanel();
        int padding = 0;
        if (mAmbientIndication.getVisibility() == View.VISIBLE) {
            padding = mStatusBar.getNotificationScrollLayout().getBottom() - getTop();
            Log.d(TAG, "Updated padding");
        }
        notificationPanelView.setAmbientIndicationBottomPadding(padding);
    }

    private void updateColors() {
        if (mTextColorAnimator != null && mTextColorAnimator.isRunning()) {
            mTextColorAnimator.cancel();
        }
        int defColor = mText.getTextColors().getDefaultColor();
        int textColor = mDozing ? Color.WHITE : mTextColor;
        if (defColor == textColor) {
            return;
        }
        mTextColorAnimator = ValueAnimator.ofArgb((int[])new int[]{defColor, textColor});
        mTextColorAnimator.setInterpolator((TimeInterpolator)Interpolators.LINEAR_OUT_SLOW_IN);
        mTextColorAnimator.setDuration(200L);
        mTextColorAnimator.addUpdateListener((ValueAnimator.AnimatorUpdateListener)new AmbientIndicationAnimatorUpdateListener(this));
        mTextColorAnimator.addListener((Animator.AnimatorListener)new AnimatorListenerAdapter(){

            public void onAnimationEnd(Animator animator2) {
                AmbientIndicationContainer.mTextColorAnimator = null;
            }
        });
        mTextColorAnimator.start();
        Log.d(TAG, "Updated colors");
    }

    public boolean getDoubleTap() {
        return onDoubleTap();
    }

    public void hideIndication() {
        setIndication(null, null);
    }

    public void initializeView(StatusBar statusBar) {
        mStatusBar = statusBar;
        addInflateListener(new AmbientIndicationInflateListener(this));
        addOnLayoutChangeListener((View.OnLayoutChangeListener)new AmbientIndicationLayoutChangeListener(this));
        Log.d(TAG, "Initialized view");
    }

    public void updateAmbientIndicationView(View view) {
        mAmbientIndication = findViewById(R.id.ambient_indication);
        mText = (TextView)findViewById(R.id.ambient_indication_text);
        mIcon = (ImageView)findViewById(R.id.ambient_indication_icon);
        mTextColor = mText.getCurrentTextColor();
        updateColors();
        setIndication(mIndication, mIntent);
        mDoubleTapHelper = new DoubleTapHelper(mAmbientIndication, new AmbientIndicationActivationListener(this), new AmbientIndicationDoubleTapListener(this), null, null);
        mAmbientIndication.setOnTouchListener((View.OnTouchListener)new AmbientIndicationTouchListener(this));
        Log.d(TAG, "Updated view");
    }

    public void setActive(boolean enabled) {
        if (enabled) {
            mStatusBar.onActivated((View)this);
            Log.d(TAG, "Set active");
            return;
        }
        mStatusBar.onActivationReset((View)this);
        Log.d(TAG, "Set inactive");
    }

    boolean getTouchEvent(View view, MotionEvent motionEvent) {
        return mDoubleTapHelper.onTouchEvent(motionEvent);
    }

    public void updateAmbientIndicationBottomPadding() {
        updateBottomPadding();
    }

    public void updateAnimator(ValueAnimator valueAnimator) {
        int n = (Integer)valueAnimator.getAnimatedValue();
        mText.setTextColor(n);
        mIcon.setColorFilter(n);
    }

    @Override
    public void setDozing(boolean enabled) {
        mDozing = enabled;
        updateColors();
    }

    public void setIndication(CharSequence charSequence, PendingIntent pendingIntent) {
        mText.setText(charSequence);
        mIndication = charSequence;
        mIntent = pendingIntent;
        View view = mAmbientIndication;
        boolean enabled = pendingIntent != null;
        view.setClickable(enabled);
        enabled = TextUtils.isEmpty((CharSequence)charSequence);
        if (enabled)
            view.setVisibility(View.INVISIBLE);
        else
            view.setVisibility(View.VISIBLE);

        updateBottomPadding();
        Log.d(TAG, "Indication set");
    }

}

