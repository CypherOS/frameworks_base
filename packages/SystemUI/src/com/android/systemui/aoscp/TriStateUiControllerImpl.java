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
 * limitations under the License.
 */

package com.android.systemui.aoscp;

import static android.view.Surface.ROTATION_90;
import static android.view.Surface.ROTATION_180;
import static android.view.Surface.ROTATION_270;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.hardware.display.DisplayManagerGlobal;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.aoscp.TriStateUiController;
import com.android.systemui.aoscp.TriStateUiController.UserActivityListener;
import com.android.systemui.plugins.VolumeDialogController;
import com.android.systemui.plugins.VolumeDialogController.Callbacks;
import com.android.systemui.plugins.VolumeDialogController.State;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.statusbar.policy.ZenModeController.Callback;
import com.android.systemui.util.OPUtils;
import com.android.systemui.util.ThemeColorUtils;

public class TriStateUiControllerImpl implements ConfigurationListener, TriStateUiController, Callback {

    private static boolean DEBUG = true;
    private static String TAG = "TriStateUiControllerImpl";

	private static final int MSG_DIALOG_SHOW = 1;
	private static final int MSG_DIALOG_DISMISS = 2;
	private static final int MSG_RESET_SCHEDULE = 3;
	private static final int MSG_STATE_CHANGE = 4;

    private static final int MODE_NORMAL = AudioManager.RINGER_MODE_NORMAL;
	private static final int MODE_SILENT = AudioManager.RINGER_MODE_SILENT;
	private static final int MODE_VIBRATE = AudioManager.RINGER_MODE_VIBRATE;
	
    private int mAccentColor = 0;
    private Context mContext;
    private final VolumeDialogController mController;
    private final Callbacks mControllerCallback = new Callbacks() {
		@Override
        public void onShowRequested(int reason) { }

        @Override
        public void onDismissRequested(int reason) { }

		@Override
        public void onScreenOff() { }

        @Override
        public void onStateChanged(State state) { }

        @Override
        public void onLayoutDirectionChanged(int layoutDirection) { }

        @Override
        public void onShowVibrateHint() { }

        @Override
        public void onShowSilentHint() { }

        @Override
        public void onShowSafetyWarning(int flags) { }

        @Override
        public void onAccessibilityModeChanged(Boolean showA11yStream) { }

        @Override
        public void onConnectedDeviceChanged(String deviceName) { }

        @Override
        public void onConfigurationChanged() {
            updateTheme(false);
            updateTriStateLayout();
        }

        @Override
        public void onPhoneStateChanged(int phoneState) { }
    };

    private int mDensity;
    private Dialog mDialog;
    private int mDialogPosition;
    private ViewGroup mDialogView;
    private final H mHandler = new H();
    private UserActivityListener mListener;
    private ZenModeController mZenModeController;
    OrientationEventListener mOrientationListener;
    private int mOrientationType = 0;
    private boolean mShowing = false;
    private int mThemeBgColor = 0;
    private int mThemeColorMode = 0;
    private int mThemeIconColor = 0;
    private int mThemeTextColor = 0;
    private ImageView mTriStateIcon;
    private TextView mTriStateText;
    private int mTriStateMode = -1;
    private Window mWindow;
    private LayoutParams mWindowLayoutParams;
    private int mWindowType;

    private final class H extends Handler {
        public H() {
            super(Looper.getMainLooper());
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DIALOG_SHOW:
                    TriStateUiControllerImpl.this.handleShow(msg.arg1);
                    return;
                case MSG_DIALOG_DISMISS:
                    TriStateUiControllerImpl.this.handleDismiss(msg.arg1);
                    return;
                case MSG_RESET_SCHEDULE:
                    TriStateUiControllerImpl.this.handleResetTimeout();
                    return;
                case MSG_STATE_CHANGE:
                    TriStateUiControllerImpl.this.handleStateChanged();
                    return;
                default:
                    return;
            }
        }
    }

    public TriStateUiControllerImpl(Context context) {
        mContext = context;
		mZenModeController = Dependency.get(ZenModeController.class);
        mOrientationListener = new OrientationEventListener(mContext, 3) {
			@Override
            public void onOrientationChanged(int orientation) {
                checkOrientationType();
            }
        };
        mController = (VolumeDialogController) Dependency.get(VolumeDialogController.class);
    }

    private void checkOrientationType() {
        Display display = DisplayManagerGlobal.getInstance().getRealDisplay(0);
        if (display != null) {
            int rotation = display.getRotation();
            if (rotation != mOrientationType) {
                mOrientationType = rotation;
                updateTriStateLayout();
            }
        }
    }

    public void init(int windowType, UserActivityListener listener) {
        mWindowType = windowType;
        mDensity = mContext.getResources().getConfiguration().densityDpi;
        mZenModeController.addCallback(this);
        mListener = listener;
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
        mController.addCallback(mControllerCallback, mHandler);
        initDialog();
    }

    public void destroy() {
        mZenModeController.removeCallback(this);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this);
        mController.removeCallback(mControllerCallback);
    }

    private void initDialog() {
        mDialog = new Dialog(mContext);
        mShowing = false;
        mWindow = mDialog.getWindow();
        mWindow.requestFeature(Window.FEATURE_NO_TITLE);
        mWindow.setBackgroundDrawable(new ColorDrawable(0));
        mWindow.clearFlags(Window.FLAG_DIM_BEHIND);
        mWindow.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        mDialog.setCanceledOnTouchOutside(false);
        mWindowLayoutParams = mWindow.getAttributes();
        mWindowLayoutParams.type = mWindowType;
        mWindowLayoutParams.format = -3;
        mWindowLayoutParams.setTitle(TriStateUiControllerImpl.class.getSimpleName());
        mWindowLayoutParams.gravity = 53;
        mWindowLayoutParams.y = mDialogPosition;
        mWindow.setAttributes(mWindowLayoutParams);
        mWindow.setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        mDialog.setContentView(R.layout.tri_state_dialog);
        mDialogView = (ViewGroup) mDialog.findViewById(R.id.tri_state_layout);
        mTriStateIcon = (ImageView) mDialog.findViewById(R.id.tri_state_icon);
        mTriStateText = (TextView) mDialog.findViewById(R.id.tri_state_text);
        updateTheme(true);
    }

    public void show(int reason) {
        mHandler.obtainMessage(MSG_DIALOG_SHOW, reason, 0).sendToTarget();
    }

    private void registerOrientationListener(boolean enable) {
        if (mOrientationListener.canDetectOrientation() && enable) {
            Log.v(TAG, "Can detect orientation");
            mOrientationListener.enable();
            return;
        }
        Log.v(TAG, "Cannot detect orientation");
        mOrientationListener.disable();
    }

    private void updateTriStateLayout() {
        if (mContext != null) {
            int iconId = 0;
            int textId = 0;
            int bg = 0;
            Resources res = mContext.getResources();
            if (res != null) {
                int positionY;
                int positionY2 = mWindowLayoutParams.y;
                int positionX = mWindowLayoutParams.x;
                int gravity = mWindowLayoutParams.gravity;
                switch (mTriStateMode) {
                    case MODE_SILENT:
                        iconId = R.drawable.op_ic_silence;
                        textId = R.string.volume_footer_slient;
                        break;
                    case MODE_VIBRATE:
                        iconId = R.drawable.op_ic_vibrate;
                        textId = R.string.volume_vibrate;
                        break;
                    case MODE_NORMAL:
                        iconId = R.drawable.op_ic_ring;
                        textId = R.string.volume_footer_ring;
                        break;
                }
                int volPanelPos = res.getInteger(R.integer.oneplus_config_threekey_type);
                boolean isRightTk = true;
                if (volPanelPos == 1) {
                    isRightTk = false;
                } else if (volPanelPos == 0) {
                    isRightTk = true;
                }
                switch (mOrientationType) {
                    case ROTATION_90:
                        if (isRightTk) {
                            gravity = 51;
                        } else {
                            gravity = 83;
                        }
                        positionY2 = res.getDimensionPixelSize(R.dimen.tri_state_up_dialog_position_deep_land);
                        if (isRightTk) {
                            positionY2 += res.getDimensionPixelSize(com.android.internal.R.dimen.status_bar_height);
                        }
                        if (mTriStateMode == MODE_SILENT) {
                            positionX = res.getDimensionPixelSize(R.dimen.tri_state_up_dialog_position_l);
                        } else if (mTriStateMode == MODE_VIBRATE) {
                            positionX = res.getDimensionPixelSize(R.dimen.tri_state_middle_dialog_position_l);
                        } else if (mTriStateMode == MODE_NORMAL) {
                            positionX = res.getDimensionPixelSize(R.dimen.tri_state_down_dialog_position_l);
                        }
                        bg = R.drawable.dialog_tri_state_middle_bg;
                        break;
                    case ROTATION_180:
                        if (isRightTk) {
                            gravity = 83;
                        } else {
                            gravity = 85;
                        }
                        positionX = res.getDimensionPixelSize(R.dimen.tri_state_up_dialog_position_deep);
                        if (mTriStateMode != MODE_SILENT) {
                            if (mTriStateMode != MODE_VIBRATE) {
                                if (mTriStateMode == MODE_NORMAL) {
                                    positionY = res.getDimensionPixelSize(R.dimen.tri_state_down_dialog_position) + res.getDimensionPixelSize(com.android.internal.R.dimen.status_bar_height);
                                }
                                bg = R.drawable.dialog_tri_state_middle_bg;
                                break;
                            }
                            positionY = res.getDimensionPixelSize(R.dimen.tri_state_middle_dialog_position) + res.getDimensionPixelSize(com.android.internal.R.dimen.status_bar_height);
                        } else {
                            positionY = res.getDimensionPixelSize(R.dimen.tri_state_up_dialog_position) + res.getDimensionPixelSize(com.android.internal.R.dimen.status_bar_height);
                        }
                        positionY2 = positionY;
                        bg = R.drawable.dialog_tri_state_middle_bg;
                    case ROTATION_270:
                        if (isRightTk) {
                            gravity = 85;
                        } else {
                            gravity = 53;
                        }
                        positionY2 = res.getDimensionPixelSize(R.dimen.tri_state_up_dialog_position_deep_land);
                        if (!isRightTk) {
                            positionY2 += res.getDimensionPixelSize(com.android.internal.R.dimen.status_bar_height);
                        }
                        if (mTriStateMode == MODE_SILENT) {
                            positionX = res.getDimensionPixelSize(R.dimen.tri_state_up_dialog_position_l);
                        } else if (mTriStateMode == MODE_VIBRATE) {
                            positionX = res.getDimensionPixelSize(R.dimen.tri_state_middle_dialog_position_l);
                        } else if (mTriStateMode == MODE_NORMAL) {
                            positionX = res.getDimensionPixelSize(R.dimen.tri_state_down_dialog_position_l);
                        }
                        bg = R.drawable.dialog_tri_state_middle_bg;
                        break;
                    default:
                        if (isRightTk) {
                            gravity = 53;
                        } else {
                            gravity = 51;
                        }
                        positionX = res.getDimensionPixelSize(R.dimen.tri_state_up_dialog_position_deep);
                        if (mTriStateMode != MODE_SILENT) {
                            if (mTriStateMode != MODE_VIBRATE) {
                                if (mTriStateMode == MODE_NORMAL) {
                                    positionY2 = res.getDimensionPixelSize(R.dimen.tri_state_down_dialog_position) + res.getDimensionPixelSize(com.android.internal.R.dimen.status_bar_height);
                                    bg = R.drawable.dialog_tri_state_down_bg;
                                    break;
                                }
                            }
                            positionY2 = res.getDimensionPixelSize(R.dimen.tri_state_middle_dialog_position) + res.getDimensionPixelSize(com.android.internal.R.dimen.status_bar_height);
                            bg = R.drawable.dialog_tri_state_middle_bg;
                            break;
                        }
                        positionY2 = res.getDimensionPixelSize(R.dimen.tri_state_up_dialog_position) + res.getDimensionPixelSize(com.android.internal.R.dimen.status_bar_height);
                        bg = R.drawable.dialog_tri_state_up_bg;
                        break;
                        break;
                }
                if (mTriStateMode != -1) {
                    if (mTriStateIcon != null) {
                        mTriStateIcon.setImageResource(iconId);
                    }
                    if (mTriStateText != null) {
                        String inputText = res.getString(textId);
                        if (inputText != null && mTriStateText.length() == inputText.length()) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(inputText);
                            sb.append(" ");
                            inputText = sb.toString();
                        }
                        mTriStateText.setText(inputText);
                    }
                    if (mDialogView != null) {
                        mDialogView.setBackgroundDrawable(res.getDrawable(bg));
                    }
                    mDialogPosition = positionY2;
                }
                positionY = res.getDimensionPixelSize(R.dimen.tri_state_dialog_padding);
                mWindowLayoutParams.gravity = gravity;
                mWindowLayoutParams.y = positionY2 - positionY;
                mWindowLayoutParams.x = positionX - positionY;
                mWindow.setAttributes(mWindowLayoutParams);
                handleResetTimeout();
            }
        }
    }

	@Override
    public void onZenChanged(int zen) {
        mHandler.obtainMessage(MSG_STATE_CHANGE, 0, 0).sendToTarget();
		if (mTriStateMode != -1) {
            show(0);
        }
    }

    private void handleShow(int reason) {
        if (DEBUG) {
            Log.d(TAG, "handleShow r=");
        }
        mHandler.removeMessages(MSG_DIALOG_SHOW);
        mHandler.removeMessages(MSG_DIALOG_DISMISS);
        handleResetTimeout();
        if (!mShowing) {
            updateTheme(false);
            registerOrientationListener(true);
            checkOrientationType();
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("handleShow mOrientationType=");
            stringBuilder.append(mOrientationType);
            Log.d(str, stringBuilder.toString());
            mShowing = true;
            mDialog.show();
            if (mListener != null) {
                mListener.onTriStateUserActivity();
            }
        }
    }

    private void handleDismiss(int reason) {
        if (DEBUG) {
            Log.d(TAG, "handleDismiss r=");
        }
        mHandler.removeMessages(MSG_DIALOG_SHOW);
        mHandler.removeMessages(MSG_DIALOG_DISMISS);
        if (mShowing) {
            registerOrientationListener(false);
            mShowing = false;
            mDialog.dismiss();
        }
    }

    private void handleStateChanged() {
		AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		int ringerMode = am.getRingerModeInternal();
        if (ringerMode != mTriStateMode) {
            mTriStateMode = ringerMode;
            updateTriStateLayout();
            if (mListener != null) {
                mListener.onTriStateUserActivity();
            }
        }
    }

    public void handleResetTimeout() {
        mHandler.removeMessages(MSG_DIALOG_DISMISS);
        int timeout = computeTimeout();
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_DIALOG_DISMISS, MSG_RESET_SCHEDULE, 0), (long) timeout);
        if (mListener != null) {
            mListener.onTriStateUserActivity();
        }
    }

    private int computeTimeout() {
        return 3000;
    }

    @Override
    public void onDensityOrFontScaleChanged() {
        handleDismiss(1);
        initDialog();
        updateTriStateLayout();
    }

    public void applyTheme() {
        Resources res = mContext.getResources();
        if (mThemeColorMode != 1) {
            mThemeIconColor = mAccentColor;
            mThemeTextColor = res.getColor(R.color.tri_state_dialog_text_color_primary);
            mThemeBgColor = res.getColor(R.color.tri_state_dialog_bg_color_steppers);
        } else {
            mThemeIconColor = mAccentColor;
            mThemeTextColor = res.getColor(R.color.tri_state_dialog_text_color_primary_dark);
            mThemeBgColor = res.getColor(R.color.tri_state_dialog_bg_color_steppers_dark);
        }
        mDialogView.setBackgroundTintList(ColorStateList.valueOf(mThemeBgColor));
        mTriStateText.setTextColor(mThemeTextColor);
        mTriStateIcon.setColorFilter(mThemeIconColor);
    }

    private void updateTheme(boolean force) {
        int theme = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.COLOR_MANAGER_THEME, 0);
        int accent = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.COLOR_MANAGER_ACCENT, 0);
        boolean change = (mThemeColorMode == theme && mAccentColor == accent) ? false : true;
        if (change || force) {
            mThemeColorMode = theme;
            mAccentColor = accent;
            applyTheme();
        }
    }
}
