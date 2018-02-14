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

import com.google.android.systemui.ambientmusic.AmbientIndicationAnimatorUpdateListener;
import com.google.android.systemui.ambientmusic.AmbientIndicationLayoutChangeListener;
import com.google.android.systemui.ambientmusic.AmbientIndicationTouchListener;
import com.google.android.systemui.ambientmusic.AmbientIndicationInflateListener;
import com.google.android.systemui.ambientmusic.AmbientIndicationActivationListener;
import com.google.android.systemui.ambientmusic.AmbientIndicationDoubleTapListener;

public class AmbientIndicationContainer
extends AutoReinflateContainer
implements DozeReceiver {
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
        if (this.mIntent != null) {
            this.mStatusBar.wakeUpIfDozing(SystemClock.uptimeMillis(), this.mAmbientIndication);
            this.mStatusBar.startPendingIntentDismissingKeyguard(this.mIntent);
            return true;
        }
        return false;
    }

    private void updateBottomPadding() {
        NotificationPanelView notificationPanelView = this.mStatusBar.getPanel();
        int padding = 0;
        if (this.mAmbientIndication.getVisibility() == View.VISIBLE) {
            padding = this.mStatusBar.getNotificationScrollLayout().getBottom() - this.getTop();
            Log.d(TAG, "Updated padding");
        }
        notificationPanelView.setAmbientIndicationBottomPadding(padding);
    }

    private void updateColors() {
        if (this.mTextColorAnimator != null && this.mTextColorAnimator.isRunning()) {
            this.mTextColorAnimator.cancel();
        }
        int defColor = this.mText.getTextColors().getDefaultColor();
        int textColor = this.mDozing ? Color.WHITE : this.mTextColor;
        if (defColor == textColor) {
            return;
        }
        this.mTextColorAnimator = ValueAnimator.ofArgb((int[])new int[]{defColor, textColor});
        this.mTextColorAnimator.setInterpolator((TimeInterpolator)Interpolators.LINEAR_OUT_SLOW_IN);
        this.mTextColorAnimator.setDuration(200L);
        this.mTextColorAnimator.addUpdateListener((ValueAnimator.AnimatorUpdateListener)new AmbientIndicationAnimatorUpdateListener(this));
        this.mTextColorAnimator.addListener((Animator.AnimatorListener)new AnimatorListenerAdapter(){

            public void onAnimationEnd(Animator animator2) {
                AmbientIndicationContainer.this.mTextColorAnimator = null;
            }
        });
        this.mTextColorAnimator.start();
        Log.d(TAG, "Updated colors");
    }

    public boolean getDoubleTap() {
        return this.onDoubleTap();
    }

    public void hideIndication() {
        this.setIndication(null);
    }

    public void initializeView(StatusBar statusBar) {
        this.mStatusBar = statusBar;
        this.addInflateListener(new AmbientIndicationInflateListener(this));
        this.addOnLayoutChangeListener((View.OnLayoutChangeListener)new AmbientIndicationLayoutChangeListener(this));
        Log.d(TAG, "Initialized view");
    }

    public void updateAmbientIndicationView(View view) {
        this.mAmbientIndication = this.findViewById(R.id.ambient_indication);
        this.mText = (TextView)this.findViewById(R.id.ambient_indication_text);
        this.mIcon = (ImageView)this.findViewById(R.id.ambient_indication_icon);
        this.mTextColor = this.mText.getCurrentTextColor();
        this.updateColors();
        this.setIndication(this.mIndication);
        this.mDoubleTapHelper = new DoubleTapHelper(this.mAmbientIndication, new AmbientIndicationActivationListener(this), new AmbientIndicationDoubleTapListener(this), null, null);
        this.mAmbientIndication.setOnTouchListener((View.OnTouchListener)new AmbientIndicationTouchListener(this));
        Log.d(TAG, "Updated view");
    }

    public void setActive(boolean bl) {
        if (bl) {
            this.mStatusBar.onActivated((View)this);
            Log.d(TAG, "Set active");
            return;
        }
        this.mStatusBar.onActivationReset((View)this);
        Log.d(TAG, "Set inactive");
    }

    boolean getTouchEvent(View view, MotionEvent motionEvent) {
        return this.mDoubleTapHelper.onTouchEvent(motionEvent);
    }

    public void updateAmbientIndicationBottomPadding() {
        this.updateBottomPadding();
    }

    public void updateAnimator(ValueAnimator valueAnimator) {
        int n = (Integer)valueAnimator.getAnimatedValue();
        this.mText.setTextColor(n);
        this.mIcon.setColorFilter(n);
    }

    @Override
    public void setDozing(boolean bl) {
        this.mDozing = bl;
        this.updateColors();
    }

    public void setIndication(CharSequence charSequence) {
        this.mText.setText(charSequence);
        this.mIndication = charSequence;
        View view = this.mAmbientIndication;
        view.setClickable(bl);
        bl = TextUtils.isEmpty((CharSequence)charSequence);
        if (bl)
            view.setVisibility(View.INVISIBLE);
        else
            view.setVisibility(View.VISIBLE);

        this.updateBottomPadding();
        Log.d(TAG, "Indication set");
    }

}

