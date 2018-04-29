/*
 * Copyright (C) 2018 CypherOS
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
package com.android.settingslib.aoscp;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.StringRes;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.settingslib.R;
import com.android.settingslib.Utils;

public class FooterConfirm extends FooterConfirmLayout {

    private long mDuration = 5000;

    private boolean mHasAction = false;
    private boolean mShowAnimated = true;
    private boolean mDismissAnimated = true;
    private boolean mIsReplacePending = false;
    private boolean mIsShowingByReplace = false;
    private boolean mIsShowing = false;
    private boolean mIsDismissing = false;

    private ImageView mIcon;
    private CharSequence mMessage;
    private TextView mFooterMessage;
    private CharSequence mActionTitle;
    private Button mFooterAction;

    private Context mContext;

    private int mOffset;
    private onActionClickListener mActionClickListener;
    private boolean mActionClicked;
    private onEventListener mEventListener;
    private Rect mWindowInsets = new Rect();
    private Rect mDisplayFrame = new Rect();
    private Point mDisplaySize = new Point();
    private Point mRealDisplaySize = new Point();
    private Activity mTargetActivity;
    private Runnable mDismissRunnable = new Runnable() {
        @Override
        public void run() {
            dismiss();
        }
    };
    private Runnable mRefreshLayoutParamsMarginsRunnable = new Runnable() {
        @Override
        public void run() {
            refreshLayoutParamsMargins();
        }
    };

    private FooterConfirm(Context context) {
        super(context);
        mContext = context;
    }

    public static FooterConfirm with(Context context) {
        return new FooterConfirm(context);
    }

    /**
     * Sets the confirmation message as a String
     *
     * @param message
     * @return
     */
    public FooterConfirm setMessage(String message) {
        mMessage = message;
        if (mFooterMessage != null) {
            mFooterMessage.setText(mMessage);
        }
        return this;
    }

    /**
     * Sets the confirmation message as a CharSequence
     *
     * @param message
     * @return
     */
    public FooterConfirm setMessage(CharSequence message) {
        mMessage = message;
        if (mFooterMessage != null) {
            mFooterMessage.setText(mMessage);
        }
        return this;
    }

    /**
     * Whether the {@link FooterConfirm} has an action
     *
     * @param hasAction
     * @return
     */
    public FooterConfirm setAction(boolean hasAction) {
        mHasAction = hasAction;
        return this;
    }

    /**
     * Sets the action name to be displayed, if any. Note that if this is not set, the action
     * button will not be displayed
     *
     * @param actionTitle
     * @return
     */
    public FooterConfirm setActionTitle(CharSequence actionTitle) {
        mActionTitle = actionTitle;
        if (mFooterAction != null) {
            mFooterAction.setText(mActionTitle);
        }
        return this;
    }

    /**
     * Sets the action name to be displayed, if any. Note that if this is not set, the action
     * button will not be displayed
     *
     * @param resId
     * @return
     */
    public FooterConfirm setActionTitle(@StringRes int resId) {
        return setActionTitle(mContext.getResources().getString(resId));
    }

    /**
     * Sets the listener to be called when the {@link FooterConfirm} action is selected.
     * @param listener
     * @return
     */
    public FooterConfirm setActionListener(onActionClickListener listener) {
        mActionClickListener = listener;
        return this;
    }

    /**
     * Sets the listener to be called when the {@link FooterConfirm} is dismissed.
     *
     * @param listener
     * @return
     */
    public FooterConfirm setEventListener(onEventListener listener) {
        mEventListener = listener;
        return this;
    }

    /**
     * Sets on/off show animation for this {@link FooterConfirm}
     *
     * @param withAnimation
     * @return
     */
    public FooterConfirm setShowAnimation(boolean withAnimation) {
        mShowAnimated = withAnimation;
        return this;
    }

    /**
     * Sets on/off dismiss animation for this {@link FooterConfirm}
     *
     * @param withAnimation
     * @return
     */
    public FooterConfirm setDismissAnimation(boolean withAnimation) {
        mDismissAnimated = withAnimation;
        return this;
    }

    /**
     * Sets the duration of this {@link FooterConfirm}
     *
     * @param duration
     * @return
     */
    public FooterConfirm setDuration(long duration) {
        mDuration = duration;
        return this;
    }

    private static MarginLayoutParams createMarginLayoutParams(ViewGroup viewGroup, int width, int height, int gravity) {
        if (viewGroup instanceof FrameLayout) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
            params.gravity = gravity;
            return params;
        } else if (viewGroup instanceof RelativeLayout) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            return params;
        } else if (viewGroup instanceof LinearLayout) {
            LinearLayout.LayoutParams params = new LayoutParams(width, height);
            params.gravity = gravity;
            return params;
        } else {
            throw new IllegalStateException("Requires FrameLayout or RelativeLayout for the parent of FooterConfirm");
        }
    }

    private MarginLayoutParams init(Context context, Activity targetActivity, ViewGroup parent) {
        FooterConfirmLayout view = (FooterConfirmLayout) LayoutInflater.from(context)
                .inflate(R.layout.footer_confirmation, this, true);
        view.setOrientation(LinearLayout.VERTICAL);

        Resources res = getResources();
        mOffset = res.getDimensionPixelOffset(R.dimen.footer_confirm_offset);
        float scale = res.getDisplayMetrics().density;

        MarginLayoutParams params;
        params = createMarginLayoutParams(
                parent, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
        view.setBackgroundColor(Utils.getColorAttr(context, android.R.attr.colorPrimary));

        mIcon = (ImageView) view.findViewById(R.id.icon);

        mFooterMessage = (TextView) view.findViewById(R.id.message);
        mFooterMessage.setText(mMessage);

        mFooterAction = (Button) view.findViewById(R.id.action);
        if (mHasAction) {
            if (!TextUtils.isEmpty(mActionTitle)) {
                requestLayout();
                mFooterAction.setVisibility(View.VISIBLE);
                mFooterAction.setText(mActionTitle);
                mFooterAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mActionClickListener != null) {
                            if (!mIsDismissing && !mActionClicked) {
                                mActionClickListener.onActionClicked(FooterConfirm.this);
                                mActionClicked = true;
                            }
                        }
                        dismiss();
                    }
                });
            } else {
                mFooterAction.setVisibility(View.GONE);
            }
        }

        return params;
    }


    private void updateWindowInsets(Activity targetActivity, Rect outInsets) {
        outInsets.left = outInsets.top = outInsets.right = outInsets.bottom = 0;

        if (targetActivity == null) {
            return;
        }

        ViewGroup decorView = (ViewGroup) targetActivity.getWindow().getDecorView();
        Display display = targetActivity.getWindowManager().getDefaultDisplay();

        boolean isTranslucent = isNavigationBarTranslucent(targetActivity);
        boolean isHidden = isNavigationBarHidden(decorView);

        Rect dispFrame = mDisplayFrame;
        Point realDispSize = mRealDisplaySize;
        Point dispSize = mDisplaySize;

        decorView.getWindowVisibleDisplayFrame(dispFrame);

        display.getRealSize(realDispSize);
        display.getSize(dispSize);

        if (dispSize.x < realDispSize.x) {
            // navigation bar is placed on right side of the screen
            if (isTranslucent || isHidden) {
                int navBarWidth = realDispSize.x - dispSize.x;
                int overlapWidth = realDispSize.x - dispFrame.right;
                outInsets.right = Math.max(Math.min(navBarWidth, overlapWidth), 0);
            }
        } else if (dispSize.y < realDispSize.y) {
            // navigation bar is placed on bottom side of the screen

            if (isTranslucent || isHidden) {
                int navBarHeight = realDispSize.y - dispSize.y;
                int overlapHeight = realDispSize.y - dispFrame.bottom;
                outInsets.bottom = Math.max(Math.min(navBarHeight, overlapHeight), 0);
            }
        }
    }

    private static int dpToPx(int dp, float scale) {
        return (int) (dp * scale + 0.5f);
    }

    public void showByReplace(Activity targetActivity) {
        mIsShowingByReplace = true;
        show(targetActivity);
    }

    public void showByReplace(ViewGroup parent) {
        mIsShowingByReplace = true;
        show(parent);
    }

    /**
     * Displays the {@link FooterConfirm} at the bottom of the
     * {@link android.app.Activity} provided.
     *
     * @param targetActivity
     */
    public void show(Activity targetActivity) {
        ViewGroup root = (ViewGroup) targetActivity.findViewById(android.R.id.content);
        MarginLayoutParams params = init(targetActivity, targetActivity, root);
        updateLayoutParamsMargins(targetActivity, params);
        showInternal(targetActivity, params, root);
    }

    /**
     * Displays the {@link FooterConfirm} at the bottom of the
     * {@link android.view.ViewGroup} provided.
     *
     * @param parent
     */
    public void show(ViewGroup parent) {
        MarginLayoutParams params = init(parent.getContext(), null, parent);
        updateLayoutParamsMargins(null, params);
        showInternal(null, params, parent);
    }

    private void showInternal(Activity targetActivity, MarginLayoutParams params, ViewGroup parent) {
        parent.removeView(this);

        for (int i = 0; i < parent.getChildCount(); i++) {
            View otherChild = parent.getChildAt(i);
            float elvation = otherChild.getElevation();
            if (elvation > getElevation()) {
                setElevation(elvation);
            }
        }
        parent.addView(this, params);

        bringToFront();

        mIsShowing = true;
        mTargetActivity = targetActivity;

        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                if (mEventListener != null) {
                    if (mIsShowingByReplace) {
                        mEventListener.onShowByReplace(FooterConfirm.this);
                    } else {
                        mEventListener.onShow(FooterConfirm.this);
                    }
                    if (!mShowAnimated) {
                        mEventListener.onShown(FooterConfirm.this);
                        mIsShowingByReplace = false;
                    }
                }
                return true;
            }
        });

        if (!mShowAnimated) {
            if (shouldStartTimer()) {
                startTimer();
            }
            return;
        }

        Animation slideIn = AnimationUtils.loadAnimation(mContext, R.anim.footer_confirm_bottom_in);
        slideIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mEventListener != null) {
                    mEventListener.onShown(FooterConfirm.this);
                    mIsShowingByReplace = false;
                }

                focusForAccessibility(mFooterMessage);

                post(new Runnable() {
                    @Override
                    public void run() {
                        getDuration();
                        if (shouldStartTimer()) {
                            startTimer();
                        }
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        startAnimation(slideIn);
    }

    private void focusForAccessibility(View view) {
        final AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_VIEW_FOCUSED);

        AccessibilityEventCompat.asRecord(event).setSource(view);
        try {
            view.sendAccessibilityEventUnchecked(event);
        } catch (IllegalStateException e) {
        }
    }

    private boolean shouldStartTimer() {
        return !isIndefiniteDuration();
    }

    private boolean isIndefiniteDuration() {
        return getDuration() == -1;
    }

    private boolean isNavigationBarHidden(ViewGroup root) {
        int viewFlags = root.getWindowSystemUiVisibility();
        return (viewFlags & View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) ==
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
    }

    private boolean isNavigationBarTranslucent(Activity targetActivity) {
        int flags = targetActivity.getWindow().getAttributes().flags;
        return (flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION) != 0;
    }

    private void startTimer() {
        postDelayed(mDismissRunnable, getDuration());
    }

    private void startTimer(long duration) {
        postDelayed(mDismissRunnable, duration);
    }

    public void dismissByReplace() {
        mIsReplacePending = true;
        dismiss();
    }

    public void dismiss() {
        dismiss(mDismissAnimated);
    }

    private void dismiss(boolean animate) {
        if (mIsDismissing) {
            return;
        }

        mIsDismissing = true;

        if (mEventListener != null && mIsShowing) {
            if (mIsReplacePending) {
                mEventListener.onDismissByReplace(FooterConfirm.this);
            } else {
                mEventListener.onDismiss(FooterConfirm.this);
            }
        }

        if (!animate) {
            finish();
            return;
        }

        final Animation slideOut = AnimationUtils.loadAnimation(mContext, R.anim.footer_confirm_bottom_out);
        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        startAnimation(slideOut);
    }

    private void finish() {
        clearAnimation();
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            parent.removeView(this);
        }
        if (mEventListener != null && mIsShowing) {
            mEventListener.onDismissed(this);
        }
        mIsShowing = false;
        mIsDismissing = false;
        mIsReplacePending = false;
        mTargetActivity = null;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        
        mIsShowing = false;

        if (mDismissRunnable != null) {
            removeCallbacks(mDismissRunnable);
        }
        if (mRefreshLayoutParamsMarginsRunnable != null) {
            removeCallbacks(mRefreshLayoutParamsMarginsRunnable);
        }
    }

    void dispatchOnWindowSystemUiVisibilityChangedCompat(int visible) {
        onWindowSystemUiVisibilityChangedCompat(visible);
    }

    protected void onWindowSystemUiVisibilityChangedCompat(int visible) {
        if (mRefreshLayoutParamsMarginsRunnable != null) {
            post(mRefreshLayoutParamsMarginsRunnable);
        }
    }

    protected void refreshLayoutParamsMargins() {
        if (mIsDismissing) {
            return;
        }

        ViewGroup parent = (ViewGroup) getParent();
        if (parent == null) {
            return;
        }

        MarginLayoutParams params = (MarginLayoutParams) getLayoutParams();

        updateLayoutParamsMargins(mTargetActivity, params);

        setLayoutParams(params);
    }

    protected void updateLayoutParamsMargins(Activity targetActivity, MarginLayoutParams params) {
        params.topMargin = 0;
        params.rightMargin = 0;
        params.leftMargin = 0;
        params.bottomMargin = 0;

        updateWindowInsets(targetActivity, mWindowInsets);

        params.rightMargin += mWindowInsets.right;
        params.bottomMargin += mWindowInsets.bottom;
    }

    public CharSequence getActionTitle() {
        return mActionTitle;
    }

    public CharSequence getMessage() {
        return mMessage;
    }

    public long getDuration() {
        return mDuration;
    }

    /**
     * @return whether the action button has been clicked.
     */
    public boolean isActionClicked() {
        return mActionClicked;
    }

    /**
     * @return the pixel offset of this {@link FooterConfirm} from the left and
     * bottom of the {@link android.app.Activity}.
     */
    public int getOffset() {
        return mOffset;
    }

    /**
     * @return true only if both dismiss and show animations are enabled
     */
    public boolean isAnimated() {
        return mShowAnimated && mDismissAnimated;
    }

    public boolean isDismissAnimated() {
        return mDismissAnimated;
    }

    public boolean isShowAnimated() {
        return mShowAnimated;
    }

    /**
     * @return true if this {@link FooterConfirm} is currently showing
     */
    public boolean isShowing() {
        return mIsShowing;
    }

    /**
     * @return true if this {@link FooterConfirm} is dismissing.
     */
    public boolean isDimissing() {
        return mIsDismissing;
    }

    /**
     * @return false if this {@link FooterConfirm} has been dismissed
     */
    public boolean isDismissed() {
        return !mIsShowing;
    }

    public interface onActionClickListener {
        
        void onActionClicked(FooterConfirm footerConfirm);

    }

    public interface onEventListener {

        /**
         * Called when a {@link FooterConfirm} is about to enter the screen
         *
         * @param footerConfirm the {@link FooterConfirm} that's being shown
         */
        public void onShow(FooterConfirm footerConfirm);

        /**
         * Called when a {@link FooterConfirm} is about to enter the screen while
         * a {@link FooterConfirm} is about to exit the screen by replacement.
         *
         * @param footerConfirm the {@link FooterConfirm} that's being shown
         */
        public void onShowByReplace(FooterConfirm footerConfirm);

        /**
         * Called when a {@link FooterConfirm} is fully shown
         *
         * @param footerConfirm the {@link FooterConfirm} that's being shown
         */
        public void onShown(FooterConfirm footerConfirm);

        /**
         * Called when a {@link FooterConfirm} is about to exit the screen
         *
         * @param footerConfirm the {@link FooterConfirm} that's being dismissed
         */
        public void onDismiss(FooterConfirm footerConfirm);

        /**
         * Called when a {@link FooterConfirm} is about to exit the screen
         * when a new {@link FooterConfirm} is about to enter the screen.
         *
         * @param footerConfirm the {@link FooterConfirm} that's being dismissed
         */
        public void onDismissByReplace(FooterConfirm footerConfirm);

        /**
         * Called when a {@link FooterConfirm} had just been dismissed
         *
         * @param footerConfirm the {@link FooterConfirm} that's being dismissed
         */
        public void onDismissed(FooterConfirm footerConfirm);
    }
}