/*
 * Copyright (C) 2019 CypherOS
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
 * limitations under the License
 */

package com.android.systemui.ambientindication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.MathUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.widget.TextView;

import com.android.internal.annotations.VisibleForTesting;

import com.android.systemui.AutoReinflateContainer;
import com.android.systemui.AutoReinflateContainer.InflateListener;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.doze.DozeReceiver;
import com.android.systemui.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.StatusBarStateController.StateListener;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.util.wakelock.DelayedWakeLock;
import com.android.systemui.util.wakelock.WakeLock;

public class AmbientIndicationContainer extends AutoReinflateContainer implements DozeReceiver, OnClickListener, 
    InflateListener, OnLayoutChangeListener, StateListener, AnimatorUpdateListener {

    private View mAmbientIndication;
    private int mAmbientIndicationIconSize;
    private Drawable mAmbientMusicAnimation;
    private PendingIntent mAmbientMusicIntent;
    private CharSequence mAmbientMusicText;
    private int mBurnInPreventionOffset;
    private float mDozeAmount;
    private boolean mDozing;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Rect mIconBounds = new Rect();
    private boolean mIsPublicMode;
    private boolean mNotificationsHidden;
    private StatusBar mStatusBar;
    private TextView mText;
    private int mTextColor;
    private ValueAnimator mTextColorAnimator;
    private final WakeLock mWakeLock = createWakeLock(mContext, mHandler);

    public AmbientIndicationContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @VisibleForTesting
    WakeLock createWakeLock(Context context, Handler handler) {
        return new DelayedWakeLock(handler, WakeLock.createPartial(context, "AmbientIndication"));
    }

    public void initializeView(StatusBar statusBar) {
        mStatusBar = statusBar;
        addInflateListener(this);
        addOnLayoutChangeListener(this);
		AmbientStateController.addListener(this);
    }

	@Override
	public void onInflated(View view) {
        updateView(view);
    }

	@Override
	public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft,
            int oldTop, int oldRight, int oldBottom) {
        updateBottomPadding();
    }

    public void updateView(View view) {
        mAmbientIndication = findViewById(R.id.ambient_indication);
        mText = (TextView) findViewById(R.id.ambient_indication_text);
        mAmbientMusicAnimation = getResources().getDrawable(R.drawable.audioanim_animation, mContext.getTheme());
        mTextColor = mText.getCurrentTextColor();
        mText.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        mAmbientIndicationIconSize = getResources().getDimensionPixelSize(R.dimen.ambient_indication_icon_size);
        mBurnInPreventionOffset = getResources().getDimensionPixelSize(R.dimen.default_burn_in_prevention_offset);
        updateColors();
        updatePill();
        mText.setOnClickListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((StatusBarStateController) Dependency.get(StatusBarStateController.class)).addCallback((StateListener) this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((StatusBarStateController) Dependency.get(StatusBarStateController.class)).removeCallback((StateListener) this);
    }

    public void setAmbientMusic(CharSequence charSequence, PendingIntent pendingIntent) {
        mAmbientMusicText = charSequence;
        mAmbientMusicIntent = pendingIntent;
        updatePill();
    }

    private void updatePill() {
        int visibility = View.VISIBLE;
        mText.setText(mAmbientMusicText);
		mText.setClickable(mAmbientMusicIntent != null);
        mAmbientIndication.setContentDescription(mAmbientMusicText);
        if (mAmbientMusicAnimation != null) {
            mIconBounds.set(0, 0, mAmbientMusicAnimation.getIntrinsicWidth(), mAmbientMusicAnimation.getIntrinsicHeight());
            MathUtils.fitRect(mIconBounds, mAmbientIndicationIconSize);
            mAmbientMusicAnimation.setBounds(mIconBounds);
        }
        Drawable rtlDrawable = isLayoutRtl() ? null : mAmbientMusicAnimation;
        mText.setCompoundDrawables(rtlDrawable, null, rtlDrawable == null ? mAmbientMusicAnimation : null, null);
		boolean hasNoMusic = (TextUtils.isEmpty(mAmbientMusicText) || mNotificationsHidden);
        if (hasNoMusic) {
            visibility = View.INVISIBLE;
        }
        mAmbientIndication.setVisibility(visibility);
        if (hasNoMusic) {
            mText.animate().cancel();
            if (mAmbientMusicAnimation instanceof AnimatedVectorDrawable) {
                ((AnimatedVectorDrawable) mAmbientMusicAnimation).reset();
            }
            mHandler.post(mWakeLock.wrap(
			        new Runnable() {
						@Override
						public void run() {
							// no op
						}}));
        } else if (mAmbientIndication.getVisibility() != View.VISIBLE) {
            mWakeLock.acquire();
            if (mAmbientMusicAnimation instanceof AnimatedVectorDrawable) {
                ((AnimatedVectorDrawable) mAmbientMusicAnimation).start();
            }
            mText.setTranslationY((float) (mText.getHeight() / 2));
            mText.setAlpha(0.0f);
            mText.animate().withLayer().alpha(1.0f).translationY(0.0f).setStartDelay(150).setDuration(100).setListener(
			        new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animator) {
							mWakeLock.release();
						}
					}).setInterpolator(Interpolators.DECELERATE_QUINT).start();
        } else {
            mHandler.post(mWakeLock.wrap(
			        new Runnable() {
						@Override
						public void run() {
							// no op
						}}));
        }
        updateBottomPadding();
    }

    public void setNotificationsHidden(boolean isHidden) {
        mNotificationsHidden = isHidden;
        updatePill();
    }

    private void updateBottomPadding() {
        mStatusBar.getPanel().setAmbientIndicationBottomPadding(mAmbientIndication.getVisibility() == View.VISIBLE 
		        ? mStatusBar.getNotificationScrollLayout().getBottom() - getTop() : View.VISIBLE);
    }

    public void hideAmbientMusic() {
        setAmbientMusic(null, null);
    }

    @Override
    public void onClick(View view) {
        if (mAmbientMusicIntent != null) {
            mStatusBar.wakeUpIfDozing(SystemClock.uptimeMillis(), mAmbientIndication);
            mStatusBar.startPendingIntentDismissingKeyguard(mAmbientMusicIntent);
        }
    }

    @Override
    public void onDozingChanged(boolean isDozing) {
        mDozing = isDozing;
        mText.setEnabled(isDozing);
        updateColors();
        updateBurnInOffsets();
    }

	@Override
    public void onDozeAmountChanged(float oldAmmount, float newAmmount) {
        mDozeAmount = newAmmount;
        updateBurnInOffsets();
    }

    private void updateBurnInOffsets() {
        int burnInOffset = AmbientBurnInHelper.getBurnInOffset(mBurnInPreventionOffset * 2, true);
        float offsetAmmountX = (float) (burnInOffset - mBurnInPreventionOffset);
        float offsetAmmountY = (float) (AmbientBurnInHelper.getBurnInOffset(mBurnInPreventionOffset * 2, false) - mBurnInPreventionOffset);
        mAmbientIndication.setTranslationX(offsetAmmountX * mDozeAmount);
        mAmbientIndication.setTranslationY(offsetAmmountY * mDozeAmount);
    }

    private void updateColors() {
        if (mTextColorAnimator != null && mTextColorAnimator.isRunning()) {
            mTextColorAnimator.cancel();
        }
        if (mText.getTextColors().getDefaultColor() != (mDozing ? -1 : mTextColor)) {
            mTextColorAnimator = ValueAnimator.ofArgb(new int[]{mText.getTextColors().getDefaultColor(), mDozing ? -1 : mTextColor});
            mTextColorAnimator.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
            mTextColorAnimator.setDuration(500);
            mTextColorAnimator.addUpdateListener(this);
            mTextColorAnimator.addListener(
			        new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animator) {
							mTextColorAnimator = null;
						}
					});
            mTextColorAnimator.start();
        }
    }

	@Override
	public void onAnimationUpdate(ValueAnimator valueAnimator) {
        updateTint(valueAnimator);
    }

    public void updateTint(ValueAnimator valueAnimator) {
        int newValue = ((Integer) valueAnimator.getAnimatedValue()).intValue();
        mText.setTextColor(newValue);
        mText.setCompoundDrawableTintList(ColorStateList.valueOf(newValue));
    }
}
