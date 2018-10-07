/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.android.systemui.aoscp.globalactions;

import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.android.systemui.R;
import com.android.systemui.plugins.GlobalActions.GlobalActionsManager;
import com.android.systemui.volume.Events;

class GlobalActionsDialogLight {

    private static final String TAG = "GlobalActionsDialogLight";

	private DialogLight mDialog;
	private GlobalActionsManager mWindowManagerFuncs;
	private ImageButton mMainActionIcon;
	private final KeyguardManager mKeyguard;
	private ViewGroup mDialogView;
	private ViewGroup mMainAction;
	private Window mWindow;
	private final Context mContext;
	private final H mHandler = new H();

	private boolean mShowing;
	private boolean mDeviceProvisioned = false;
	private boolean mHovering = false;
    private boolean mKeyguardShowing = false;

	private Runnable mEmulatePress = new Runnable() {
        @Override
        public void run() {
            mMainActionIcon.setPressed(true);
            mMainActionIcon.postOnAnimationDelayed(mEmulateUnpress, 200);
        }
    };

	private Runnable mEmulateUnpress = new Runnable() {
        @Override
        public void run() {
            mMainActionIcon.setPressed(false);
        }
    };

    public GlobalActionsDialogLight(Context context, GlobalActionsManager windowManagerFuncs) {
        mContext = new ContextThemeWrapper(context, com.android.systemui.R.style.qs_theme);
        mWindowManagerFuncs = windowManagerFuncs;
		mKeyguard = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
    }

    public void initDialog(boolean keyguardShowing, boolean isDeviceProvisioned) {
        mKeyguardShowing = keyguardShowing;
        mDeviceProvisioned = isDeviceProvisioned;
		mDialog = new DialogLight(mContext);
		
		mWindow = mDialog.getWindow();
        mWindow.requestFeature(Window.FEATURE_NO_TITLE);
        mWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mWindow.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND
                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR);
        mWindow.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        mWindow.setType(WindowManager.LayoutParams.TYPE_VOLUME_OVERLAY);
        mWindow.setWindowAnimations(com.android.internal.R.style.Animation_Toast);
        final WindowManager.LayoutParams lp = mWindow.getAttributes();
        lp.format = PixelFormat.TRANSLUCENT;
		lp.setTitle(GlobalActionsDialogLight.class.getSimpleName());
		lp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        lp.windowAnimations = -1;
        mWindow.setAttributes(lp);
        mWindow.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		mDialog.setCanceledOnTouchOutside(true);
        mDialog.setContentView(R.layout.global_actions_light);
		mDialog.setOnShowListener(dialog -> {
            if (!isLandscape()) mDialogView.setTranslationX(mDialogView.getWidth() / 2);
            mDialogView.setAlpha(0);
            mDialogView.animate()
                    .alpha(1)
                    .translationX(0)
                    .setDuration(300)
                    .setInterpolator(new SystemUIInterpolators.LogDecelerateInterpolator())
                    .withEndAction(() -> {
                        mMainActionIcon.postOnAnimationDelayed(mEmulatePress, 1500);
                    })
                    .start();
        });
		mDialogView = mDialog.findViewById(R.id.global_light_dialog);
        mDialogView.setOnHoverListener((v, event) -> {
            int action = event.getActionMasked();
            mHovering = (action == MotionEvent.ACTION_HOVER_ENTER)
                    || (action == MotionEvent.ACTION_HOVER_MOVE);
            rescheduleTimeoutH();
            return true;
        });

		mMainAction = mDialog.findViewById(R.id.main_action);
        mMainActionIcon = mMainAction.findViewById(R.id.main_action_icon);
		mMainActionIcon.setImageResource(R.drawable.ic_lock_power_off);
		
		initMainActionH();
    }

	protected void rescheduleTimeoutH() {
        mHandler.removeMessages(H.DISMISS);
        final int timeout = computeTimeoutH();
        mHandler.sendMessageDelayed(mHandler
                .obtainMessage(H.DISMISS, Events.DISMISS_REASON_TIMEOUT, 0), timeout);
    }
	
	private int computeTimeoutH() {
        if (mHovering) return 16000;
        return 3000;
    }
	
	public void initMainActionH() {
        mMainActionIcon.setOnClickListener(v -> {
            mWindowManagerFuncs.shutdown(false /* confirm */);
        });
    }
	
	private void showH(int reason) {
        mHandler.removeMessages(H.SHOW);
        mHandler.removeMessages(H.DISMISS);
        rescheduleTimeoutH();
        mShowing = true;

        mDialog.show();
        Events.writeEvent(mContext, Events.EVENT_SHOW_DIALOG, reason, mKeyguard.isKeyguardLocked());
    }
	
	protected void dismissH(int reason) {
        mHandler.removeMessages(H.DISMISS);
        mHandler.removeMessages(H.SHOW);
        mDialogView.animate().cancel();
        mShowing = false;

        mDialogView.setTranslationX(0);
        mDialogView.setAlpha(1);
        ViewPropertyAnimator animator = mDialogView.animate()
                .alpha(0)
                .setDuration(250)
                .setInterpolator(new SystemUIInterpolators.LogAccelerateInterpolator())
                .withEndAction(() -> mHandler.postDelayed(() -> {
                    mDialog.dismiss();
                }, 50));
        if (!isLandscape()) animator.translationX(mDialogView.getWidth() / 2);
        animator.start();

        Events.writeEvent(mContext, Events.EVENT_DISMISS_DIALOG, reason);
    }

	private boolean isLandscape() {
        return mContext.getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE;
    }
	
	private final class H extends Handler {
        private static final int SHOW = 1;
        private static final int DISMISS = 2;
        private static final int RESCHEDULE_TIMEOUT = 3;

        public H() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW: showH(msg.arg1); break;
                case DISMISS: dismissH(msg.arg1); break;
                case RESCHEDULE_TIMEOUT: rescheduleTimeoutH(); break;
            }
        }
    }

	private final class DialogLight extends Dialog implements DialogInterface {
        public DialogLight(Context context) {
            super(context, com.android.systemui.R.style.qs_theme);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            rescheduleTimeoutH();
            return super.dispatchTouchEvent(ev);
        }

        @Override
        protected void onStart() {
            super.setCanceledOnTouchOutside(true);
            super.onStart();
        }

        @Override
        protected void onStop() {
            super.onStop();
            mHandler.sendEmptyMessage(H.RECHECK_ALL);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (isShowing()) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    dismissH(Events.DISMISS_REASON_TOUCH_OUTSIDE);
                    return true;
                }
            }
            return false;
        }
    }
}
