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

import static android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
import static android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_GENERIC;

import android.accessibilityservice.AccessibilityServiceInfo;
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
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserManager;
import android.service.dreams.DreamService;
import android.service.dreams.IDreamManager;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager.AccessibilityServicesStateChangeListener;
import android.widget.ImageButton;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.ScreenshotHelper;

import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.plugins.GlobalActions.GlobalActionsManager;
import com.android.systemui.statusbar.policy.AccessibilityManagerWrapper;
import com.android.systemui.volume.Events;
import com.android.systemui.volume.SystemUIInterpolators;

import java.util.List;

public class GlobalActionsDialogLight implements GlobalActionsLight {

    private static final String TAG = "GlobalActionsDialogLight";
	
	static public final String SYSTEM_DIALOG_REASON_DREAM = "dream";

	private final Accessibility mAccessibility = new Accessibility();
	private final AccessibilityManagerWrapper mAccessibilityMgr;
	private DialogLight mDialog;
	private final GlobalActionsLightController mController;
	private GlobalActionsManager mWindowManagerFuncs;
	private final IDreamManager mDreamManager;
	private ImageButton mMainActionIcon;
	private ImageButton mMainContentPrimary;
	private ImageButton mMainContentSecondary;
	private final KeyguardManager mKeyguard;
	private final ScreenshotHelper mScreenshotHelper;
	private ViewGroup mDialogView;
	private ViewGroup mMainAction;
	private ViewGroup mMainContent;
	private Window mWindow;
	private final Context mContext;
	private final GAL mHandler = new GAL();

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
		mAccessibilityMgr = Dependency.get(AccessibilityManagerWrapper.class);
		mController = Dependency.get(GlobalActionsLightController.class);
		mDreamManager = IDreamManager.Stub.asInterface(
                ServiceManager.getService(DreamService.DREAM_SERVICE));
		mScreenshotHelper = new ScreenshotHelper(context);
    }

	public void createDialog(boolean keyguardShowing, boolean isDeviceProvisioned) {
        mKeyguardShowing = keyguardShowing;
        mDeviceProvisioned = isDeviceProvisioned;
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
            // Show delayed, so that the dismiss of the previous dialog completes
            mHandler.sendEmptyMessage(GAL.MSG_SHOW);
        } else {
            handleShow();
        }
		mDialog.setOnDismissListener(this);
    }

	private void awakenIfNecessary() {
        if (mDreamManager != null) {
            try {
                if (mDreamManager.isDreaming()) {
                    mDreamManager.awaken();
                }
            } catch (RemoteException e) {
                // we tried
            }
        }
    }

	private void handleShow() {
		mHandler.removeMessages(GAL.MSG_SHOW);
        mHandler.removeMessages(GAL.MSG_DISMISS);
        mShowing = true;

		awakenIfNecessary();
		rescheduleTimeout();
		initDialog();
		mAccessibility.init();
		mController.addCallback(mControllerCallbackH, mHandler);
		mController.notifyVisible(true);
		mWindowManagerFuncs.onGlobalActionsShown();
    }

	@Override
    public void onGalDestroy() {
        mAccessibility.destroy();
        mController.removeCallback(mControllerCallbackH);
        mHandler.removeCallbacksAndMessages(null);
    }

    public void initDialog() {
		mDialog = new DialogLight(mContext);
		
		mHovering = false;
        mShowing = false;
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

		mDialog.setCanceledOnTouchOutside(false);
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
            rescheduleTimeout();
            return true;
        });

		mMainAction = mDialog.findViewById(R.id.main_action);
        mMainActionIcon = mMainAction.findViewById(R.id.main_action_icon);
		mMainActionIcon.setImageResource(com.android.internal.R.drawable.ic_lock_power_off);
		
		mMainContent = mDialog.findViewById(R.id.main_content);
		mMainContentPrimary = mMainContent.findViewById(R.id.main_content_primary);
		mMainContentPrimary.setImageResource(com.android.internal.R.drawable.ic_restart);
		mMainContentSecondary = mMainContent.findViewById(R.id.main_content_secondary);
		mMainContentSecondary.setImageResource(com.android.internal.R.drawable.ic_screenshot);
		
		initMainAction();
		initContentPrimary();
		initContentSecondary();
		mDialog.show();
    }

	protected void rescheduleTimeout() {
        mHandler.removeMessages(GAL.MSG_DISMISS);
        final int timeout = computeTimeout();
        mHandler.sendMessageDelayed(mHandler
                .obtainMessage(GAL.MSG_DISMISS, Events.DISMISS_REASON_TIMEOUT, 0), timeout);
		mController.userActivity();
    }

	private int computeTimeout() {
        if (mHovering) return 16000;
        return 3000;
    }

	public void initMainAction() {
        mMainActionIcon.setOnClickListener(v -> {
			mDialog.dismiss();
            mWindowManagerFuncs.shutdown(false /* confirm */);
        });
		mMainActionIcon.setOnLongClickListener(v -> {
            UserManager um = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
			mDialog.dismiss();
            if (!um.hasUserRestriction(UserManager.DISALLOW_SAFE_BOOT)) {
                mWindowManagerFuncs.rebootSafeMode(true);
                return true;
            }
            return false;
        });
    }

	public void initContentPrimary() {
        mMainContentPrimary.setOnClickListener(v -> {
			mDialog.dismiss();
            mWindowManagerFuncs.reboot(false /* confirm */);
        });
    }

	public void initContentSecondary() {
		mMainContentSecondary.setOnClickListener(v -> {
			mDialog.dismiss();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScreenshotHelper.takeScreenshot(1, true, true, mHandler);
                    MetricsLogger.action(mContext,
                            MetricsEvent.ACTION_SCREENSHOT_POWER_MENU);
                }
            }, 500);
		});
    }

	private boolean isLandscape() {
        return mContext.getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE;
    }
	
	CharSequence composeWindowTitle() {
        return mContext.getString(R.string.global_actions);
    }
	
	private final GlobalActionsLightController.Callbacks mControllerCallbackH
            = new GlobalActionsLightController.Callbacks() {
        @Override
        public void onShowRequested() {
            handleShow();
        }

        @Override
        public void onDismissRequested() {
            mDialog.dismiss();
        }

        @Override
        public void onScreenOff() {
            mDialog.dismiss();
        }

        @Override
        public void onLayoutDirectionChanged(int layoutDirection) {
            mDialogView.setLayoutDirection(layoutDirection);
        }

        @Override
        public void onConfigurationChanged() {
            mDialog.dismiss();
            initDialog();
        }
    };

	private final class GAL extends Handler {
        private static final int MSG_SHOW = 1;
        private static final int MSG_DISMISS = 2;
        private static final int MSG_RESCHEDULE_TIMEOUT = 3;

        public GAL() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SHOW:
				    handleShow();
					break;
				case MSG_DISMISS:
                    if (mDialog != null) {
                        if (SYSTEM_DIALOG_REASON_DREAM.equals(msg.obj)) {
                            mDialog.dismissImmediately();
                        } else {
                            mDialog.dismiss();
                        }
                        mDialog = null;
                    }
                    break;
                case MSG_RESCHEDULE_TIMEOUT:
				    rescheduleTimeout();
					break;
            }
        }
    }

	private final class DialogLight extends Dialog implements DialogInterface {
        public DialogLight(Context context) {
            super(context, com.android.systemui.R.style.qs_theme);
			mDialogView.setOutsideTouchListener(view -> dismiss());
        }

        @Override
        protected void onStart() {
            super.setCanceledOnTouchOutside(true);
            super.onStart();
        }
		
		@Override
        public void dismiss() {
			mHandler.removeMessages(GAL.MSG_DISMISS);
			mHandler.removeMessages(GAL.MSG_SHOW);
			mDialogView.animate().cancel();
			mShowing = false;

			mDialogView.setTranslationX(0);
			mDialogView.setAlpha(1);
			ViewPropertyAnimator animator = mDialogView.animate()
                    .alpha(0)
                    .setDuration(250)
					.withEndAction(() -> super.dismiss())
                    .setInterpolator(new SystemUIInterpolators.LogAccelerateInterpolator());
            if (!isLandscape()) animator.translationX(mDialogView.getWidth() / 2);
            animator.start();

		    Events.writeEvent(mContext, Events.EVENT_DISMISS_DIALOG, reason);
		    mController.notifyVisible(false);
        }
		
		void dismissImmediately() {
            super.dismiss();
        }
    }
	
	private final class Accessibility extends AccessibilityDelegate {
        private boolean mFeedbackEnabled;

        public void init() {
            mDialogView.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    updateFeedbackEnabled();
                }
            });
            mDialogView.setAccessibilityDelegate(this);
            mAccessibilityMgr.addCallback(mListener);
            updateFeedbackEnabled();
        }

        public void destroy() {
            mAccessibilityMgr.removeCallback(mListener);
        }

        @Override
        public boolean dispatchPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
            event.getText().add(composeWindowTitle());
            return true;
        }

        @Override
        public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child,
                AccessibilityEvent event) {
            rescheduleTimeout();
            return super.onRequestSendAccessibilityEvent(host, child, event);
        }

        private void updateFeedbackEnabled() {
            mFeedbackEnabled = computeFeedbackEnabled();
        }

        private boolean computeFeedbackEnabled() {
            final List<AccessibilityServiceInfo> services =
                    mAccessibilityMgr.getEnabledAccessibilityServiceList(FEEDBACK_ALL_MASK);
            for (AccessibilityServiceInfo asi : services) {
                if (asi.feedbackType != 0 && asi.feedbackType != FEEDBACK_GENERIC) {
                    return true;
                }
            }
            return false;
        }

        private final AccessibilityServicesStateChangeListener mListener =
                enabled -> updateFeedbackEnabled();
    }
}
