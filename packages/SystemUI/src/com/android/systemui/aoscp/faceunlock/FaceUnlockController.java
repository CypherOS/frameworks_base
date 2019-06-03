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
package com.android.systemui.aoscp.faceunlock;

import static com.android.systemui.statusbar.phone.FingerprintUnlockController.MODE_NONE;
import static com.android.systemui.statusbar.phone.FingerprintUnlockController.MODE_WAKE_AND_UNLOCK;
import static com.android.systemui.statusbar.phone.FingerprintUnlockController.MODE_UNLOCK;
import static com.android.systemui.statusbar.phone.FingerprintUnlockController.MODE_DISMISS_BOUNCER;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IPowerManager;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import android.view.IWindowManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManagerGlobal;

import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.ViewMediatorCallback;

import com.android.systemui.R;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.phone.FingerprintUnlockController;

public class FaceUnlockController extends KeyguardUpdateMonitorCallback {

    private static final boolean DEBUG = true;
	private final int MSG_START_FACEUNLOCK = 1;
    private final int MSG_STOP_FACEUNLOCK = 2;
    private final int MSG_UNLOCK = 3;
	private final int MSG_FAIL = 4;
    private final int MSG_NO_FACE = 5;
	private final int MSG_RESET_LOCKOUT = 6;
    private final int MSG_SKIP_BOUNCER = 7;
	private final int MSG_RESET_FACEUNLOCK_PENDING = 8;
    private final int MSG_CAMERA_ERROR = 9;
    private final int MSG_NO_PERMISSION = 10;
    private final int MSG_UPDATE_FACE_ADDED = 11;

    private boolean mBinding = false;
    private boolean mBindingSetting = false;
    private boolean mBouncer = false;
    private boolean mBoundToService = false;
	
	private static final String

	private String mFaceUnlockComponent;
	private String mFaceUnlockComponentSetting;
	private String mFaceUnlockSettingIntent;
	

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
        public void onReceive(Context context, Intent intent) {
            if (mFaceUnlockSettingIntent.equals(intent.getAction())) {
                mHandler.removeMessages(MSG_UPDATE_FACE_ADDED);
                mHandler.sendEmptyMessage(MSG_UPDATE_FACE_ADDED);
                if (DEBUG) {
                    Log.d(TAG, "Update face added");
                }
            }
        }
    };

    private boolean mCameraLaunching = false;
    private ServiceConnection mConnection = new ServiceConnection() {
		@Override
        public void onServiceConnected(ComponentName className, IBinder iservice) {
            Log.d(TAG, "Connected to FaceUnlock service");
            mService = IFaceUnlockService.Stub.asInterface(iservice);
            mBinding = false;
            mBoundToService = true;
            tryAndStartFaceUnlock();
        }

        Override
        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "Disconnected from FaceUnlock service");
            mService = null;
            mBinding = false;
            mBoundToService = false;
        }
    };

    private Context mContext;
    private FingerprintUnlockController mFPC;
    private boolean mIsFaceUnlockActive = false;
    private HandlerThread mFaceUnlockThread;
    private int mFailedAttempts;
    private Handler mHandler;
    private KeyguardIndicationController mIndicator;
    private boolean mIsGoingToSleep = false;
    private boolean mIsKeyguardShowing = false;
    private boolean mIsScreenOffUnlock = false;
    private boolean mIsScreenTurnedOn = false;
    private boolean mIsScreenTurningOn = false;
    private boolean mIsSleep = false;
    private KeyguardViewMediator mKeyguardViewMediator;
    private ViewMediatorCallback mKeyguardViewMediatorCallback;

    private boolean mLockout = false;
    private boolean mNeedToPendingStopFaceUnlock = false;

    private final IFaceUnlock mFaceUnlockCallback = new IFaceUnlock.Stub() {
		Override
        public void onBeginRecognize(int faceId) {
            if (mIsFaceUnlockActive && DEBUG) {
                Log.d(TAG, "onBeginRecognize");
            }
        }

        Override
        public void onCompared(int faceId, int userId, int result, int compareTimeMillis, int score) {
        }

        Override
        public void onEndRecognize(int faceId, int userId, int result) {
            if (mIsFaceUnlockActive) {
                mHandler.removeMessages(MSG_RESET_FACEUNLOCK_PENDING);
                mNeedToPendingStopFaceUnlock = false;
                boolean allowed = mUpdateMonitor.isUnlockingWithFingerprintAllowed();
                StringBuilder sb = new StringBuilder();
                sb.append("onEndRecognize, result:");
                sb.append(result);
                sb.append(", keyguardShow:");
                sb.append(mIsKeyguardShowing);
                sb.append(", bouncer:");
                sb.append(mBouncer);
                sb.append(", allowed:");
                sb.append(allowed);
                sb.append(", isSleep:");
                sb.append(mIsSleep);
                sb.append(", simpin:");
                sb.append(mUpdateMonitor.isSimPinSecure());
                Log.d(TAG, sb.toString());
                mKeyguardViewMediator.userActivity();
                if (result == 0) {
                    if (!mUpdateMonitor.allowShowingLock() || !allowed || mIsSleep || mUpdateMonitor.isSimPinSecure()) {
                        Log.d(TAG, "not handle recognize");
                        mHandler.removeMessages(MSG_STOP_FACEUNLOCK);
                        mHandler.sendEmptyMessage(MSG_STOP_FACEUNLOCK);
                        return;
                    }
                    if (mUpdateMonitor.isAutoFaceUnlockEnabled() || mStatusBar.isBouncerShowing()) {
                        Log.d(TAG, "onEndRecognize, result ok to unlock");
                        mHandler.sendEmptyMessage(MSG_UNLOCK);
                    } else {
                        Log.d(TAG, "onEndRecognize, result ok to skip bouncer");
                        mHandler.sendEmptyMessage(MSG_SKIP_BOUNCER);
                    }
                } else if (result == 2) {
                    Log.d(TAG, "onEndRecognize: no face");
                    mHandler.sendEmptyMessage(MSG_NO_FACE);
                } else if (result == 3) {
                    Log.d(TAG, "onEndRecognize: camera error");
                    mHandler.sendEmptyMessage(MSG_CAMERA_ERROR);
                } else if (result == 4) {
                    Log.d(TAG, "onEndRecognize: no permission");
                    mHandler.sendEmptyMessage(MSG_NO_PERMISSION);
                } else {
                    sb = new StringBuilder();
                    sb.append("onEndRecognize: fail ");
                    sb.append(mFailedAttempts + 1);
                    sb.append(" times");
                    Log.d(TAG, sb.toString());
                    mHandler.sendEmptyMessage(MSG_FAIL);
                }
            }
        }
    };

    private boolean mPendingFaceUnlockWhenBouncer = false;
    private String mPendingLaunchCameraSource = null;
    private boolean mPendingStopFaceUnlock = false;
    private StatusBar mStatusBar;

    private IFaceUnlockService mService;
    private ServiceConnection mSettingConnection = new ServiceConnection() {
		@Override
        public void onServiceConnected(ComponentName className, IBinder iservice) {
            mSettingService = IFaceUnlockSettingService.Stub.asInterface(iservice);
            StringBuilder sb = new StringBuilder();
            sb.append("Connected to FaceSetting service, ");
            sb.append(mSettingService);
            Log.d(TAG, sb.toString());
            updateFaceAdded();
            mBindingSetting = false;
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "Disconnected from face unlock setting service");
            mSettingService = null;
            mUpdateMonitor.setIsFaceAdded(false);
            mBindingSetting = false;
        }
    };
    private IFaceUnlockSettingService mSettingService;
    long mSleepTime = 0;
    private boolean mStartFaceUnlockWhenScreenOn = false;
    private StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private StatusBarWindowManager mStatusBarWindowManager;
    private Handler mUIHandler;
    private KeyguardUpdateMonitor mUpdateMonitor;
    private IWindowManager mWM;

    private class FaceUnlockHandler extends Handler {
        FaceUnlockHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("handleMessage: what:");
                sb.append(msg.what);
                sb.append(", bound:");
                sb.append(mBoundToService);
                sb.append(", active:");
                sb.append(mIsFaceUnlockActive);
                Log.d(TAG, sb.toString());
            }
            switch (msg.what) {
                case MSG_START_FACEUNLOCK:
                    if (mBoundToService) {
                        handleStartFaceUnlock();
                        break;
                    }
                    return;
                case MSG_STOP_FACEUNLOCK:
                    if (mBoundToService) {
                        updateRecognizedState(0, -1);
                        handleStopFaceUnlock();
                        break;
                    }
                    return;
                case MSG_UNLOCK:
                    if (mIsFaceUnlockActive && mBoundToService) {
                        unlockKeyguard();
                        break;
                    }
                    return;
                case MSG_FAIL:
                    if (mIsFaceUnlockActive) {
                        handleRecognizeFail();
                        break;
                    }
                    return;
                case MSG_NO_FACE:
                    if (mIsFaceUnlockActive) {
                        playFaceUnlockIndicationTextAnim();
                        updateRecognizedState(6, -65536);
                        handleStopFaceUnlock();
                        break;
                    }
                    return;
                case MSG_RESET_LOCKOUT:
                    handleResetLockout();
                    break;
                case MSG_SKIP_BOUNCER:
                    if (mIsFaceUnlockActive && mBoundToService) {
                        handleSkipBouncer();
                        break;
                    }
                    return;
                case MSG_RESET_FACEUNLOCK_PENDING:
                    handleResetFaceUnlockPending();
                    break;
                case MSG_CAMERA_ERROR:
                    if (mIsFaceUnlockActive) {
                        playFaceUnlockIndicationTextAnim();
                        updateRecognizedState(8, -65536);
                        handleStopFaceUnlock();
                        break;
                    }
                    return;
                case MSG_NO_PERMISSION:
                    if (mIsFaceUnlockActive) {
                        playFaceUnlockIndicationTextAnim();
                        updateRecognizedState(9, -65536);
                        handleStopFaceUnlock();
                        break;
                    }
                    return;
                case MSG_UPDATE_FACE_ADDED:
                    updateFaceAdded();
                    break;
                default:
                    Log.e(TAG, "Unhandled message");
                    break;
            }
            if (DEBUG) {
                Log.d(TAG, "handleMessage: done");
            }
        }
    }

    public FaceUnlockController(Context context, KeyguardViewMediator keyguardViewMediator, StatusBar phoneStatusBar, StatusBarKeyguardViewManager statusBarKeyguardViewManager, StatusBarWindowManager statusBarWindowManager, FingerprintUnlockController fpc) {
        mContext = context;
        mUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
        mUpdateMonitor.registerCallback(this);
        mKeyguardViewMediator = keyguardViewMediator;
        mStatusBar = phoneStatusBar;
        mKeyguardViewMediatorCallback = keyguardViewMediator.getViewMediatorCallback();
        mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
        mStatusBarWindowManager = statusBarWindowManager;
        mFaceUnlockThread = new HandlerThread("FaceUnlockThread");
        mFaceUnlockThread.start();
        mHandler = new FaceUnlockHandler(mFaceUnlockThread.getLooper());
        mUIHandler = new Handler();
        mWM = WindowManagerGlobal.getWindowManagerService();
        mFPC = fpc;
	
		mFaceUnlockComponent = mContext.getResources().getString(R.config.config_faceUnlockComponent);
		mFaceUnlockComponentSetting = mContext.getResources().getString(R.config.config_faceUnlockComponentSetting);
		mFaceUnlockSettingIntent = mContext.getResources().getString(R.config.config_faceUnlockSettingIntent);

		if (isFaceUnlockSettingsAvailable()) {
			IntentFilter filter = new IntentFilter();
		    filter.addAction(mFaceUnlockSettingIntent);
            context.registerReceiver(mBroadcastReceiver, filter);
		}
    }

    private void updateRecognizedState(int type, int color) {
        if (!mLockout) {
            mUpdateMonitor.notifyFaceUnlockStateChanged(type);
            updateNotifyMessage(type, color);
            if (type == 1) {
                mLockout = true;
            }
        }
    }

    private void handleResetLockout() {
        mLockout = false;
        if (mBoundToService && canUseFaceUnlock()) {
            updateRecognizedState(5, -1);
        }
    }

    private void handleSkipBouncer() {
        if (DEBUG) {
            Log.d(TAG, "handleSkipBouncer");
        }
        mFailedAttempts = 0;
        updateRecognizedState(2, -1);
        handleStopFaceUnlock();
    }

    private void handleRecognizeFail() {
        int type = 1;
        mFailedAttempts++;
        boolean playFailAnimation = false;
        if (mFailedAttempts % 5 != 0) {
            type = 7;
        }
        if (mFailedAttempts < 3) {
            playFailAnimation = true;
        } else if (mStatusBar != null) {
            if (DEBUG) {
                Log.d(TAG, "enter Bouncer");
            }
            mUIHandler.post(new Runnable() {
                public void run() {
                    mStatusBarKeyguardViewManager.showBouncer(false);
                    mStatusBar.animateCollapsePanels(0, true, false, 1.3f);
                }
            });
        }
        if (playFailAnimation) {
            //playFaceUnlockIndicationTextAnim();
        }
        updateRecognizedState(type, -65536);
        handleStopFaceUnlock();
    }

    private void playFaceUnlockIndicationTextAnim() {
        if (mStatusBar != null && !mStatusBar.isBouncerShowing()) {
            mUIHandler.post(new Runnable() {
                public void run() {
                    //mStatusBar.startFacelockFailAnimation();
                }
            });
        }
    }

    @Override
    public void onPreStartedWakingUp() {
        StringBuilder sb = new StringBuilder();
        sb.append("onPreStartedWakingUp, bound:");
        sb.append(mBoundToService);
        sb.append(", pending:");
        sb.append(mPendingFaceUnlockWhenBouncer);
        Log.d(TAG, sb.toString());
        mIsSleep = false;
        if (mBoundToService && canUseFaceUnlock()) {
            if (mPendingFaceUnlockWhenBouncer) {
                updateRecognizedState(3, -1);
            }
            mHandler.removeMessages(MSG_STOP_FACEUNLOCK);
            mHandler.removeMessages(MSG_START_FACEUNLOCK);
            mHandler.sendEmptyMessage(MSG_START_FACEUNLOCK);
        }
    }

    @Override
    public void onStartedWakingUp() {
        StringBuilder sb = new StringBuilder();
        sb.append("onStartedWakingUp, bound:");
        sb.append(mBoundToService);
        sb.append(", lockout:");
        sb.append(mLockout);
        Log.d(TAG, sb.toString());
        mIsSleep = false;
        if (mBoundToService && canUseFaceUnlock()) {
            mHandler.removeMessages(MSG_STOP_FACEUNLOCK);
            mHandler.removeMessages(MSG_START_FACEUNLOCK);
            mHandler.sendEmptyMessage(MSG_START_FACEUNLOCK);
        }
    }

    @Override
    public void onScreenTurningOn() {
        if (DEBUG) {
            Log.d(TAG, "onScreenTurningOn");
        }
        mIsScreenTurningOn = true;
    }

    @Override
    public void onScreenTurnedOn() {
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("onScreenTurnedOn, ");
            sb.append(mStartFaceUnlockWhenScreenOn);
            sb.append(", ");
            sb.append(mIsSleep);
            Log.d(TAG, sb.toString());
        }
        mIsScreenTurnedOn = true;
        if (mStartFaceUnlockWhenScreenOn) {
            mStartFaceUnlockWhenScreenOn = false;
            if (canUseFaceUnlock()) {
                mIsSleep = false;
                if (mBoundToService) {
                    mHandler.removeMessages(MSG_STOP_FACEUNLOCK);
                    mHandler.removeMessages(MSG_START_FACEUNLOCK);
                    mHandler.sendEmptyMessage(MSG_START_FACEUNLOCK);
                }
            }
        }
    }

    @Override
    public void onScreenTurnedOff() {
        if (DEBUG) {
            Log.d(TAG, "onScreenTurnedOff");
        }
        mIsScreenTurnedOn = false;
        mIsScreenTurningOn = false;
    }

    @Override
    public void onPreStartedGoingToSleep() {
        if (DEBUG) {
            Log.d(TAG, "onPreStartedGoingToSleep");
        }
        mIsSleep = true;
    }

    @Override
    public void onStartedGoingToSleep(int why) {
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("onStartedGoingToSleep, ");
            sb.append(why);
            sb.append(", bound:");
            sb.append(mBoundToService);
            Log.d(TAG, sb.toString());
        }
        mIsGoingToSleep = true;
        mStartFaceUnlockWhenScreenOn = false;
        mCameraLaunching = false;
        mIsSleep = true;
        mHandler.removeMessages(MSG_STOP_FACEUNLOCK);
        mHandler.removeMessages(MSG_START_FACEUNLOCK);
        mHandler.sendEmptyMessage(MSG_STOP_FACEUNLOCK);
        mPendingFaceUnlockWhenBouncer = false;
        mSleepTime = SystemClock.uptimeMillis();
    }

    @Override
    public void onFinishedGoingToSleep(int why) {
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("onFinishedGoingToSleep, ");
            sb.append(why);
            Log.d(TAG, sb.toString());
        }
        mIsGoingToSleep = false;
    }

    @Override
    public void onDreamingStateChanged(boolean dreaming) {
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("onDreamingStateChanged, ");
            sb.append(dreaming);
            Log.d(TAG, sb.toString());
        }
    }

    @Override
    public void onKeyguardReset() {
        if (!mBoundToService) {
            return;
        }
        if (canUseFaceUnlock()) {
            if (mIsScreenTurnedOn && !mUpdateMonitor.isFaceUnlockRecognizing()) {
                if (DEBUG) {
                    Log.d(TAG, "onKeyguardReset to start");
                }
                mHandler.removeMessages(MSG_STOP_FACEUNLOCK);
                mHandler.removeMessages(MSG_START_FACEUNLOCK);
                mHandler.sendEmptyMessage(MSG_START_FACEUNLOCK);
            }
            return;
        }
        if (mIsFaceUnlockActive) {
            if (DEBUG) {
                Log.d(TAG, "onKeyguardReset to stop");
            }
            stopFaceUnlock();
        }
    }

    @Override
    public void onUserSwitchComplete(int userId) {
        if (userId != 0) {
            stopFaceUnlock();
        }
    }

    @Override
    public void onDeviceProvisioned() {
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("onDeviceProvisioned, bound:");
            sb.append(mBoundToService);
            Log.d(TAG, sb.toString());
        }
        if (!mBoundToService && isFaceUnlockAvailable()) {
            bindFaceUnlock();
        }
        mHandler.removeMessages(MSG_UPDATE_FACE_ADDED);
        mHandler.sendEmptyMessage(MSG_UPDATE_FACE_ADDED);
    }

    @Override
    public void onStrongAuthStateChanged(int userId) {
        if (!canUseFaceUnlock()) {
            if (mUpdateMonitor.isFaceUnlockAvailable() || mUpdateMonitor.isFaceUnlockRecognizing()) {
                Log.d(TAG, "onStrongAuthStateChanged to stop");
                stopFaceUnlock();
            }
        }
    }

    @Override
    public void onKeyguardVisibilityChanged(boolean showing) {
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("onKeyguardVisibilityChanged, show:");
            sb.append(showing);
            sb.append(", bound:");
            sb.append(mBoundToService);
            Log.d(TAG, sb.toString());
        }
        if (mIsKeyguardShowing != showing) {
            if (!mBoundToService && isFaceUnlockAvailable()) {
                bindFaceUnlock();
            }
            mHandler.removeMessages(MSG_UPDATE_FACE_ADDED);
            mHandler.sendEmptyMessage(MSG_UPDATE_FACE_ADDED);
            if (!showing) {
                mStartFaceUnlockWhenScreenOn = false;
                mCameraLaunching = false;
                mNeedToPendingStopFaceUnlock = false;
                mHandler.removeMessages(MSG_START_FACEUNLOCK);
                mHandler.sendEmptyMessage(MSG_STOP_FACEUNLOCK);
            } else if (!mIsKeyguardShowing && mBoundToService && canUseFaceUnlock()) {
                mHandler.removeMessages(MSG_STOP_FACEUNLOCK);
                mHandler.removeMessages(MSG_START_FACEUNLOCK);
                mHandler.sendEmptyMessage(MSG_START_FACEUNLOCK);
            }
            mIsKeyguardShowing = showing;
            if (!showing) {
                mPendingFaceUnlockWhenBouncer = false;
            }
        }
    }

    public void onKeyguardBouncerChanged(boolean isBouncer) {
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("onKeyguardBouncerChanged , bouncer:");
            sb.append(isBouncer);
            sb.append(", show:");
            sb.append(mIsKeyguardShowing);
            sb.append(", skip:");
            sb.append(mUpdateMonitor.canSkipBouncerByFaceUnlock());
            sb.append(", unlocking:");
            sb.append(mUpdateMonitor.isFaceUnlocking());
            Log.d(TAG, sb.toString());
        }
        mBouncer = isBouncer;
        if (mIsKeyguardShowing || !isBouncer) {
            if (mIsKeyguardShowing && isBouncer) {
                if (mUpdateMonitor.canSkipBouncerByFaceUnlock()) {
                    mFPC.startWakeAndUnlockForFace(MODE_DISMISS_BOUNCER);
                } else if (mUpdateMonitor.isFaceUnlocking() && mStatusBarWindowManager.isShowingLiveWallpaper(false)) {
                    Log.d(TAG, "just keyguardDone");
                    mKeyguardViewMediator.keyguardDone();
                }
            }
            return;
        }
        tryAndStartFaceUnlock();
    }

    @Override
    public void onClearFailedFaceUnlockAttempts() {
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("onClearFailedFaceUnlockAttempts, failed:");
            sb.append(mFailedAttempts);
            sb.append(", lockout:");
            sb.append(mLockout);
            Log.d(TAG, sb.toString());
        }
        mFailedAttempts = 0;
        mLockout = false;
    }

    @Override
    public void onSystemReady() {
        if (DEBUG) {
            Log.d(TAG, "onSystemReady");
        }
		if (isFaceUnlockAvailable()) {
			bindFaceUnlock();
			if (isFaceUnlockSettingsAvailable()) {
				bindFaceUnlockSetting();
			}
		}
    }

    @Override
    public void onPasswordLockout() {
        if (DEBUG) {
            Log.d(TAG, "onPasswordLockout");
        }
        stopFaceUnlock();
    }

    public boolean tryAndStartFaceUnlock() {
        StringBuilder sb = new StringBuilder();
        sb.append("tryAndStartFaceUnlock, bound:");
        sb.append(mBoundToService);
        Log.d(TAG, sb.toString());
        if (!canUseFaceUnlock()) {
            return false;
        }
        if (mBoundToService) {
            mHandler.removeMessages(MSG_STOP_FACEUNLOCK);
            mHandler.removeMessages(MSG_START_FACEUNLOCK);
            mHandler.sendEmptyMessage(MSG_START_FACEUNLOCK);
        }
        return true;
    }

    public void tryToStartFaceUnlockAfterScreenOn() {
        if (DEBUG) {
            Log.d(TAG, "tryToStartFaceUnlockAfterScreenOn");
        }
		mStartFaceUnlockWhenScreenOn = true;
        if (mBoundToService) {
            mPendingFaceUnlockWhenBouncer = true;
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    mPendingFaceUnlockWhenBouncer = false;
                    if (!tryAndStartFaceUnlock()) {
                        stopFaceUnlock();
                    }
                }
            }, 500);
        }
    }

    public boolean canUseFaceUnlock() {
        if (mCameraLaunching) {
            Log.d(TAG, "Cannot start while camera is in use");
            return false;
        } else if (!mUpdateMonitor.isFaceUnlockAllowed()) {
            if (DEBUG) {
                Log.d(TAG, "Face unlock is not allowed");
            }
            return false;
        } else if (!isFaceUnlockTimeout()) {
            return true;
        } else {
            Log.d(TAG, "Timeout, Face unlock is not allowed");
            return false;
        }
    }

    public boolean isFaceUnlockRunning() {
        return mIsFaceUnlockActive;
    }

    private void updateFaceAdded() {
        if (mSettingService == null) {
            mUpdateMonitor.setIsFaceAdded(false);
			if (isFaceUnlockSettingsAvailable()) {
				bindFaceUnlockSetting();
			}
            return;
        }
        boolean isAdded = true;
        int state = 1;
        try {
            state = mSettingService.checkState(0);
        } catch (Exception re) {
            StringBuilder sbFail = new StringBuilder();
            sbFail.append("updateFaceAdded fail: ");
            sbFail.append(re.getMessage());
            Log.d(TAG, sbFail.toString());
        }
        boolean preAdded = mUpdateMonitor.isFaceAdded();
        if (state != 0) {
            isAdded = false;
        }
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("isFaceAdded:");
            sb.append(isAdded);
            sb.append(", pre:");
            sb.append(preAdded);
            Log.d(TAG, sb.toString());
        }
        if (!(mUpdateMonitor.isFaceAdded() || !isAdded || mUpdateMonitor.isUnlockingWithFingerprintAllowed() || mStatusBarKeyguardViewManager.mBouncer == null)) {
            mUpdateMonitor.setIsFaceAdded(isAdded);
            mStatusBarKeyguardViewManager.mBouncer.updateBouncerPromptReason();
            Log.d(TAG, "Face is added and not allowed, update Prompt reason");
        }
        mUpdateMonitor.setIsFaceAdded(isAdded);
        if (isAdded != preAdded) {
            if (isAdded) {
                tryAndStartFaceUnlock();
            } else {
                stopFaceUnlock();
            }
        }
    }

    public boolean notifyCameraLaunch(boolean isCameraLaunching, String source) {
        if (mIsKeyguardShowing) {
            mCameraLaunching = isCameraLaunching;
        }
        boolean pending = false;
        StringBuilder sb = new StringBuilder();
        sb.append("notifyCameraLaunch, source:");
        sb.append(source);
        sb.append(", facelockActive:");
        sb.append(mIsFaceUnlockActive);
        sb.append(", keyguard:");
        sb.append(mIsKeyguardShowing);
        Log.d(TAG, sb.toString());
        if (mIsFaceUnlockActive) {
            if (source != null) {
                mPendingLaunchCameraSource = source;
                pending = true;
            }
            stopFaceUnlock();
        }
        return pending;
    }

    private void handleStartFaceUnlock() {
        StringBuilder sbFail;
        boolean cameraError = mUpdateMonitor.isCameraErrorState();
        StringBuilder sb = new StringBuilder();
        sb.append("Handle startFaceUnlock, active:");
        sb.append(mIsFaceUnlockActive);
        sb.append(", pendingStop:");
        sb.append(mPendingStopFaceUnlock);
        sb.append(", live wp:");
        sb.append(mStatusBarWindowManager.isShowingLiveWallpaper(false));
        sb.append(", cameraError:");
        sb.append(cameraError);
        sb.append(", showing:");
        sb.append(mIsKeyguardShowing);
        sb.append(", pending:");
        sb.append(mPendingFaceUnlockWhenBouncer);
        sb.append(", on:");
        sb.append(mIsScreenTurnedOn);
        Log.d(TAG, sb.toString());
        if (mService == null) {
            Log.d(TAG, "No service was found");
        } else if (cameraError) {
            Log.d(TAG, "An error occured with the camera");
        } else if (mPendingFaceUnlockWhenBouncer) {
            Log.d(TAG, "Pending in bouncer");
        } else if (mIsFaceUnlockActive) {
            mPendingStopFaceUnlock = false;
            updateRecognizedState(3, -1);
        } else if (mIsScreenTurnedOn || !mKeyguardViewMediator.isScreenOffAuthenticating()) {
            mStartFaceUnlockWhenScreenOn = false;
            updateRecognizedState(3, -1);
            mIsFaceUnlockActive = true;
            mNeedToPendingStopFaceUnlock = true;
            synchronized (this) {
                try {
                    mService.registerCallback(mFaceUnlockCallback);
                    mService.prepare();
                    mService.startFaceUnlock(0);
                } catch (RemoteException e) {
                    sbFail = new StringBuilder();
                    sbFail.append("FaceUnlock failed, ");
                    sbFail.append(e.getMessage());
                    Log.e(TAG, sbFail.toString());
                    mNeedToPendingStopFaceUnlock = false;
                    mHandler.sendEmptyMessage(MSG_FAIL);
                    return;
                } catch (NullPointerException e2) {
                    sbFail = new StringBuilder();
                    sbFail.append("FaceUnlock service is null, ");
                    sbFail.append(e2.getMessage());
                    Log.e(TAG, sbFail.toString());
                    mNeedToPendingStopFaceUnlock = false;
                    mHandler.sendEmptyMessage(MSG_FAIL);
                    return;
                }
            }
            mHandler.removeMessages(MSG_RESET_FACEUNLOCK_PENDING);
            mHandler.sendEmptyMessageDelayed(MSG_RESET_FACEUNLOCK_PENDING, 500);
        } else {
            mStartFaceUnlockWhenScreenOn = true;
            Log.d(TAG, "Pending start to screen on");
        }
    }

    public void resetFaceUnlockPending() {
        mNeedToPendingStopFaceUnlock = false;
        stopFaceUnlock();
    }

    private void handleResetFaceUnlockPending() {
        mNeedToPendingStopFaceUnlock = false;
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("handleResetFaceUnlockPending, ");
            sb.append(mPendingStopFaceUnlock);
            Log.d(TAG, sb.toString());
        }
        if (mPendingStopFaceUnlock) {
            handleStopFaceUnlock();
        }
    }

    private void stopFaceUnlock() {
        mHandler.removeMessages(MSG_STOP_FACEUNLOCK);
        mHandler.removeMessages(MSG_START_FACEUNLOCK);
        mHandler.sendEmptyMessage(MSG_STOP_FACEUNLOCK);
    }

    private void handleStopFaceUnlock() {
		StringBuilder sb;
        StringBuilder sbFail;
        if (!mIsFaceUnlockActive) {
            sb = new StringBuilder();
            sb.append("not stop facelock, active:");
            sb.append(mIsFaceUnlockActive);
            Log.d(TAG, sb.toString());
        } else if (mNeedToPendingStopFaceUnlock) {
            mPendingStopFaceUnlock = true;
            if (DEBUG) {
                Log.d(TAG, "pending stop facelock");
            }
        } else {
            sb = new StringBuilder();
            sb.append("handle stopFaceUnlock, pending camera:");
            sb.append(mPendingLaunchCameraSource);
            Log.d(TAG, sb.toString());
            mHandler.removeMessages(MSG_RESET_FACEUNLOCK_PENDING);
            mPendingStopFaceUnlock = false;
            mIsFaceUnlockActive = false;
            synchronized (this) {
                try {
                    mService.unregisterCallback(mFaceUnlockCallback);
                    mService.stopFaceUnlock(0);
                    mService.release();
                } catch (RemoteException e) {
                    sbFail = new StringBuilder();
                    sbFail.append("stopFaceUnlock fail, ");
                    sbFail.append(e.getMessage());
                    Log.e(TAG, sbFail.toString());
                } catch (NullPointerException e2) {
                    sbFail = new StringBuilder();
                    sbFail.append("stopFaceUnlock mService null, ");
                    sbFail.append(e2.getMessage());
                    Log.e(TAG, sbFail.toString());
                }
            }
            if (mPendingLaunchCameraSource != null) {
                final String source = mPendingLaunchCameraSource;
                mUIHandler.post(new Runnable() {
                    public void run() {
                        launchCamera(source);
                    }
                });
                mPendingLaunchCameraSource = null;
            }
        }
    }

    private void unlockKeyguard() {
        boolean isLiveWallpaperShowing = mStatusBarWindowManager.isShowingLiveWallpaper(false);
        boolean isBouncerShowing = mStatusBar.isBouncerShowing();
        boolean interActive = mUpdateMonitor.isDeviceInteractive();
        StringBuilder sb = new StringBuilder();
        sb.append("unlockKeyguard, bouncer:");
        sb.append(isBouncerShowing);
        sb.append(", live wp:");
        sb.append(isLiveWallpaperShowing);
        sb.append(", interactive = ");
        sb.append(interActive);
        sb.append(", offUnlock:");
        sb.append(mIsScreenOffUnlock);
        Log.d(TAG, sb.toString());
        mFailedAttempts = 0;
        mUpdateMonitor.onFaceUnlocking(true);
        mUpdateMonitor.notifyFaceUnlockStateChanged(4);
        mUIHandler.post(new Runnable() {
            public void run() {
                int mode;
                if (mIsScreenOffUnlock && !isLiveWallpaperShowing) {
                    mode = MODE_WAKE_AND_UNLOCK;
                } else if (isBouncerShowing) {
                    mKeyguardViewMediator.onWakeAndUnlocking(false);
                    mode = MODE_NONE;
                    if (mStatusBar != null) {
                        mStatusBar.forceHideBouncer();
                    }
                } else if (isLiveWallpaperShowing || !mUpdateMonitor.isDeviceInteractive()) {
                    mode = MODE_UNLOCK;
                } else {
                    mKeyguardViewMediator.onWakeAndUnlocking(false);
                    mode = MODE_NONE;
                }
                mUpdateMonitor.resetFPTimeout();
                mFPC.startWakeAndUnlockForFace(mode);
            }
        });
        mUpdateMonitor.notifyFaceUnlockStateChanged(0);
        stopFaceUnlock();
    }

    public boolean isFaceUnlockTimeout() {
        return mLockout || !mUpdateMonitor.isUnlockingWithFingerprintAllowed();
    }

    private void bindFaceUnlock() {
        if (!mBinding) {
            Intent component = new Intent();
			component.setComponent(new ComponentName(ComponentName.unflattenFromString(mFaceUnlockComponent)));
            try {
                if (mContext.bindServiceAsUser(component, mConnection, 1, UserHandle.OWNER)) {
                    Log.d(TAG, "Binding ok");
                    mBinding = true;
                } else {
                    Log.d(TAG, "Binding fail");
                }
            } catch (Exception e) {
                StringBuilder sb = new StringBuilder();
                sb.append("bindFaceUnlock fail, ");
                sb.append(e.getMessage());
                Log.e(TAG, sb.toString());
            }
        }
    }

    private void bindFaceUnlockSetting() {
        if (mBindingSetting) {
            Log.d(TAG, "return Binding");
            return;
        }
        Intent component = new Intent();
		component.setComponent(new ComponentName(ComponentName.unflattenFromString(mFaceUnlockComponentSetting)));
        try {
            if (mContext.bindServiceAsUser(component, mSettingConnection, 1, UserHandle.OWNER)) {
                Log.d(TAG, "Binding setting ok");
                mBindingSetting = true;
            } else {
                Log.d(TAG, "Binding setting fail");
            }
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("bind setting fail, ");
            sb.append(e.getMessage());
            Log.e(TAG, sb.toString());
        }
    }

    private void updateNotifyMessage(int type, int color) {
        int msgId = mUpdateMonitor.getFaceUnlockNotifyMsgId(type);
        mUIHandler.post(new Runnable() {
            public void run() {
                LockIcon icon = null;
                if (!(mStatusBar == null || mStatusBar.getKeyguardBottomAreaView() == null)) {
                    icon = mStatusBar.getKeyguardBottomAreaView().getLockIcon();
                }
                if (icon != null) {
                    if (mIsGoingToSleep && type == 0) {
                        icon.setFaceUnlockRunning(type, false);
                    } else {
                        icon.setFaceUnlockRunning(type, true);
                    }
                }
                if (mIndicator != null) {
                    if (type == 3) {
                        mIndicator.showTransientIndication(" ", color);
                    } else if (type == 2) {
                        mIndicator.showTransientIndication(null);
                    } else {
                        if (msgId > 0) {
                            if (mUpdateMonitor.isFaceUnlockAvailable()) {
                                mIndicator.showTransientIndication(null);
                            } else {
                                mIndicator.showTransientIndication(mContext.getString(msgId), -1);
                            }
                        }
                    }
                }
            }
        });
    }

    public void setKeyguardIndicationController(KeyguardIndicationController indicationController) {
        mIndicator = indicationController;
    }

    private void launchCamera(String source) {
        if (mStatusBar != null) {
            mStatusBar.getKeyguardBottomAreaView().launchCamera(source);
        }
    }

    public boolean isScreenOffUnlock() {
        return mIsScreenOffUnlock;
    }

	private boolean isFaceUnlockAvailable() {
		return !TextUtils.isEmpty(mFaceUnlockComponent);
	}

	private boolean isFaceUnlockSettingsAvailable() {
		return isFaceUnlockAvailable()
		        && !TextUtils.isEmpty(mFaceUnlockComponentSetting)
		        && !TextUtils.isEmpty(mFaceUnlockSettingIntent);
	}
}
