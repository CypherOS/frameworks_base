/*
 * Copyright (C) 2016, ParanoidAndroid Project
 * Copyright (C) 2018, CypherOS
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

package com.android.server.policy;

import android.app.KeyguardManager;
import android.app.StatusBarManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraManager.TorchCallback;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.power.V1_0.PowerHint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.input.InputManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.session.MediaSessionLegacyHelper;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.HandlerThread;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.PowerManagerInternal;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.WindowManagerGlobal;

import com.android.server.LocalServices;
import com.android.server.statusbar.StatusBarManagerInternal;

import com.android.internal.R;
import com.android.internal.policy.IKeyguardDismissCallback;

import java.lang.IllegalArgumentException;

public class FingerprintKeyHandler {

    private static final String TAG = FingerprintKeyHandler.class.getSimpleName();

    private static final boolean DEBUG = true;

    private static final int MAX_SUPPORTED_FINGERPRINT_GESTURES = 15;
    private static final int FINGERPRINT_GESTURES_DEFAULT = 0;

    // Dummy camera id for CameraManager.
    private static final String DUMMY_CAMERA_ID = "";

    // Vibration attributes.
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .build();

    // Supported actions.
    private static final int DISABLED = 0;
    private static final int TORCH = 1;
    private static final int AIRPLANE = 2;
    private static final int MUSIC_PLAY_PAUSE = 3;
    private static final int MUSIC_NEXT = 4;
    private static final int MUSIC_PREVIOUS = 5;
    private static final int SCREENSHOT = 6;
    private static final int ASSISTANT = 7;

    private Context mContext;
    private String mCameraId;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private CameraManager mCameraManager;
    private AudioManager mAudioManager;
    private TelecomManager mTelecomManager;
    private StatusBarManagerInternal mStatusBarManagerInternal;
    private KeyguardManager mKeyguardManager;
    private Vibrator mVibrator;
    private boolean mTorchEnabled;
    private boolean mSystemReady = false;

    private int mDoubleTapKeyCode;
    private int mLongPressKeyCode;
    private int mSwipeUpKeyCode;
    private int mSwipeDownKeyCode;
    private int mSwipeLeftKeyCode;
    private int mSwipeRightKeyCode;

    private int mDoubleTapGesture;
    private int mLongPressGesture;
    private int mSwipeUpGesture;
    private int mSwipeDownGesture;
    private int mSwipeLeftGesture;
    private int mSwipeRightGesture;
  
    private IKeyguardDismissCallback mCallback;

    private long[] mVibePattern;

    private boolean mFPGesturesEnabled;

    private SparseIntArray mGestures = new SparseIntArray(MAX_SUPPORTED_FINGERPRINT_GESTURES);

    private ContentObserver mObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange) {
            onConfigurationChanged();
        }
    };

    public FingerprintKeyHandler(Context context) {
        mContext = context;
        mHandlerThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    public void systemReady() {
        mSystemReady = true;

        // Init configurations
        getConfiguration();

        // Init Managers
        getAudioManager();
        getTelecomManager();
        getVibrator();
        getStatusBarService();
        getCameraManager();
        getKeyguardManager();

        // Get camera id
        prepareCameraId();

        registerTorchCallback();
        registerObservers();
    }

    private void getConfiguration() {
        final Resources resources = mContext.getResources();

        // Fingerprint Gesture Keycodes
        mDoubleTapKeyCode = resources.getInteger(R.integer.config_fpDoubleTapKeyCode);
        mLongPressKeyCode = resources.getInteger(R.integer.config_fpLongpressKeyCode);
        mSwipeUpKeyCode = resources.getInteger(R.integer.config_fpSwipeUpKeyCode);
        mSwipeDownKeyCode = resources.getInteger(R.integer.config_fpSwipeDownKeyCode);
        mSwipeLeftKeyCode = resources.getInteger(R.integer.config_fpSwipeLeftKeyCode);
        mSwipeRightKeyCode = resources.getInteger(R.integer.config_fpSwipeRightKeyCode);

        mGestures.clear();
        mGestures.put(mDoubleTapKeyCode, mDoubleTapGesture);
        mGestures.put(mLongPressKeyCode, mLongPressGesture);
        mGestures.put(mSwipeUpKeyCode, mSwipeUpGesture);
        mGestures.put(mSwipeDownKeyCode, mSwipeDownGesture);
        mGestures.put(mSwipeLeftKeyCode, mSwipeLeftGesture);
        mGestures.put(mSwipeRightKeyCode, mSwipeRightGesture);

        onConfigurationChanged();
    }

    private void onConfigurationChanged() {
        boolean gesturesEnabled = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.FINGERPRINT_GESTURES_ENABLED, FINGERPRINT_GESTURES_DEFAULT) != 0;
        if (gesturesEnabled != mFPGesturesEnabled) {
            mFPGesturesEnabled = gesturesEnabled;
        }

        int doubleTapGesture = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.FINGERPRINT_GESTURES_DOUBLE_TAP, mContext.getResources()
                        .getInteger(com.android.internal.R.integer.config_fpDoubleTapDefault));
        if (doubleTapGesture != mDoubleTapGesture) {
            mDoubleTapGesture = doubleTapGesture;
            mGestures.put(mDoubleTapKeyCode, mDoubleTapGesture);
        }

        int longPressGesture = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.FINGERPRINT_GESTURES_LONGPRESS, mContext.getResources()
                        .getInteger(com.android.internal.R.integer.config_fpLongpressDefault));
        if (longPressGesture != mLongPressGesture) {
            mLongPressGesture = longPressGesture;
            mGestures.put(mLongPressKeyCode, mLongPressGesture);
        }

        int swipeUpGesture = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.FINGERPRINT_GESTURES_SWIPE_UP, mContext.getResources()
                        .getInteger(com.android.internal.R.integer.config_fpSwipeUpDefault));
        if (swipeUpGesture != mSwipeUpGesture) {
            mSwipeUpGesture = swipeUpGesture;
            mGestures.put(mSwipeUpKeyCode, mSwipeUpGesture);
        }

        int swipeDownGesture = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.FINGERPRINT_GESTURES_SWIPE_DOWN, mContext.getResources()
                        .getInteger(com.android.internal.R.integer.config_fpSwipeDownDefault));
        if (swipeDownGesture != mSwipeDownGesture) {
            mSwipeDownGesture = swipeDownGesture;
            mGestures.put(mSwipeDownKeyCode, mSwipeDownGesture);
        }

        int swipeLeftGesture = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.FINGERPRINT_GESTURES_SWIPE_LEFT, mContext.getResources()
                        .getInteger(com.android.internal.R.integer.config_fpSwipeLeftDefault));
        if (swipeLeftGesture != mSwipeLeftGesture) {
            mSwipeLeftGesture = swipeLeftGesture;
            mGestures.put(mSwipeLeftKeyCode, mSwipeLeftGesture);
        }

        int swipeRightGesture = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.FINGERPRINT_GESTURES_SWIPE_RIGHT, mContext.getResources()
                        .getInteger(com.android.internal.R.integer.config_fpSwipeRightDefault));
        if (swipeRightGesture != mSwipeRightGesture) {
            mSwipeRightGesture = swipeRightGesture;
            mGestures.put(mSwipeRightKeyCode, mSwipeRightGesture);
        }
    }

    private void getAudioManager() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        }
    }

    private void getTelecomManager() {
        if (mTelecomManager == null) {
            mTelecomManager = TelecomManager.from(mContext);
        }
    }

    private void getVibrator() {
        if (mVibrator == null) {
            mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            mVibePattern = getLongIntArray(mContext.getResources(),
                    R.array.config_longPressVibePattern);
            if (!mVibrator.hasVibrator()) {
                mVibrator = null;
            }
        }
    }

    private void getStatusBarService() {
        if (mStatusBarManagerInternal == null) {
            mStatusBarManagerInternal = LocalServices.getService(StatusBarManagerInternal.class);
        }
    }

    private void getKeyguardManager() {
        if (mKeyguardManager == null) {
            mKeyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        }
    }

    private void getCameraManager() {
        if (mCameraManager == null) {
            mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        }
    }

    private void prepareCameraId() {
        String cameraId = DUMMY_CAMERA_ID;
        try {
            cameraId = getCameraId();
        } catch (Throwable e) {
            Log.e(TAG, "Couldn't initialize.", e);
            return;
        } finally {
            mCameraId = cameraId;
        }
    }

    private void registerTorchCallback() {
        if (mCameraManager != null) {
            mCameraManager.registerTorchCallback(mTorchCallback, mHandler);
        }
    }

    private void registerObservers() {
        final ContentResolver resolver = mContext.getContentResolver();
        resolver.registerContentObserver(Settings.System.getUriFor(
                Settings.System.FINGERPRINT_GESTURES_ENABLED),
                false, mObserver, UserHandle.USER_ALL);
        resolver.registerContentObserver(Settings.System.getUriFor(
                Settings.System.FINGERPRINT_GESTURES_DOUBLE_TAP),
                false, mObserver, UserHandle.USER_ALL);
        resolver.registerContentObserver(Settings.System.getUriFor(
                Settings.System.FINGERPRINT_GESTURES_LONGPRESS),
                false, mObserver, UserHandle.USER_ALL);
        resolver.registerContentObserver(Settings.System.getUriFor(
                Settings.System.FINGERPRINT_GESTURES_SWIPE_UP),
                false, mObserver, UserHandle.USER_ALL);
        resolver.registerContentObserver(Settings.System.getUriFor(
                Settings.System.FINGERPRINT_GESTURES_SWIPE_DOWN),
                false, mObserver, UserHandle.USER_ALL);
        resolver.registerContentObserver(Settings.System.getUriFor(
                Settings.System.FINGERPRINT_GESTURES_SWIPE_LEFT),
                false, mObserver, UserHandle.USER_ALL);
        resolver.registerContentObserver(Settings.System.getUriFor(
                Settings.System.FINGERPRINT_GESTURES_SWIPE_RIGHT),
                false, mObserver, UserHandle.USER_ALL);
    }

    private void handleGesture(int gesture) {
        if (DEBUG) {
            Log.w(TAG, "handleCodeBehavior: gesture = " + gesture);
        }
        doHapticFeedback(true);
        boolean handled = false;
        switch(gesture) {
            case TORCH:
                handled = setTorchMode(!mTorchEnabled);
                break;
            case AIRPLANE:
                doHapticFeedback(true);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        boolean enabled = Settings.Global.getInt(mContext.getContentResolver(),
                                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
                        Settings.Global.putInt(mContext.getContentResolver(),
                                Settings.Global.AIRPLANE_MODE_ON, !enabled ? 1 : 0);
                        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
                        intent.putExtra("state", enabled);
                        mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                    }
                });
                break;
            case MUSIC_PLAY_PAUSE:
                handled = dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                break;
            case MUSIC_PREVIOUS:
                handled = isMusicActive() && dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                break;
            case MUSIC_NEXT:
                handled = isMusicActive() && dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_NEXT);
                break;
            case SCREENSHOT:
                triggerVirtualKeypress(mHandler, KeyEvent.KEYCODE_SYSRQ);
                break;
            case ASSISTANT:
                triggerVirtualKeypress(mHandler, KeyEvent.KEYCODE_ASSIST);
                break;
            default:
                handled = false;
                break;
        }

        if (!handled) {
            doHapticFeedback(false);
        }
    }

    public boolean handleKeyEvent(KeyEvent event) {
        if (DEBUG) {
            Log.w(TAG, "handleKeyEvent(): event.toString(): " + event.toString());
        }

        if (!mSystemReady || !mFPGesturesEnabled || isDisabledByPhoneState()) {
            return false;
        }

        int action = event.getAction();
        int scanCode = event.getScanCode();
        int repeatCount = event.getRepeatCount();

        if (scanCode <= 0) {
            if (DEBUG) {
                Log.w(TAG, "handleKeyEvent(): scanCode is invalid, returning." );
            }
            return false;
        }

        if (action != KeyEvent.ACTION_UP || repeatCount != 0) {
            if (DEBUG) {
                Log.w(TAG, "handleKeyEvent(): action != ACTION_UP || repeatCount != 0, returning.");
            }
            return false;
        }

        boolean isKeySupportedAndEnabled = mGestures.get(scanCode) > 0;

        if (DEBUG) {
            Log.w(TAG, "handleKeyEvent(): isKeySupportedAndEnabled = " + isKeySupportedAndEnabled);
        }

        if (isKeySupportedAndEnabled) {
            handleGesture(mGestures.get(scanCode));
        }

        return isKeySupportedAndEnabled;
    }

    private void triggerVirtualKeypress(Handler handler, int keyCode) {
        final InputManager im = InputManager.getInstance();
        long now = SystemClock.uptimeMillis();

        final KeyEvent downEvent = new KeyEvent(now, now, KeyEvent.ACTION_DOWN,
                keyCode, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                KeyEvent.FLAG_FROM_SYSTEM, InputDevice.SOURCE_CLASS_BUTTON);
        final KeyEvent upEvent = KeyEvent.changeAction(downEvent,
                KeyEvent.ACTION_UP);

        // add a small delay to make sure everything behind got focus
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                im.injectInputEvent(downEvent, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
            }
        }, 10);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                im.injectInputEvent(upEvent, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
            }
        }, 20);
    }

    private boolean dispatchMediaKeyWithWakeLockToMediaSession(int keycode) {
        MediaSessionLegacyHelper helper = MediaSessionLegacyHelper.getHelper(mContext);
        if (helper != null) {
            KeyEvent event = new KeyEvent(SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN, keycode, 0);
            helper.sendMediaButtonEvent(event, true);
            event = KeyEvent.changeAction(event, KeyEvent.ACTION_UP);
            helper.sendMediaButtonEvent(event, true);
            return true;
        } else {
            if (DEBUG) {
                Log.w(TAG, "Unable to send media key event");
            }
            return false;
        }
    }

    private void doHapticFeedback(boolean success) {
        final boolean hapticsEnabled = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.HAPTIC_FEEDBACK_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
        if (hapticsEnabled && mVibrator != null) {
            if (success) {
                mVibrator.vibrate(mVibePattern, -1, VIBRATION_ATTRIBUTES);
            } else {
                mVibrator.vibrate(350L, VIBRATION_ATTRIBUTES);
            }
        }
    }

    private String getCameraId() throws CameraAccessException {
        String[] ids = mCameraManager.getCameraIdList();
        if (ids != null && ids.length > 0) {
            for (String id : ids) {
                CameraCharacteristics c = mCameraManager.getCameraCharacteristics(id);
                Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
                if (flashAvailable != null && flashAvailable
                        && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    return id;
                }
            }
        }
        return DUMMY_CAMERA_ID;
    }

    private boolean setTorchMode(boolean enabled) {
        try {
            mCameraManager.setTorchMode(mCameraId, enabled);
        } catch (CameraAccessException e) {
            return false;
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    private TorchCallback mTorchCallback = new TorchCallback() {
        @Override
        public void onTorchModeChanged(String cameraId, boolean enabled) {
            if (!TextUtils.isEmpty(mCameraId)) {
                if (mCameraId.equals(cameraId)) {
                    mTorchEnabled = enabled;
                }
            } else {
                mTorchEnabled = enabled;
            }
        }

        @Override
        public void onTorchModeUnavailable(String cameraId) {
            if (!TextUtils.isEmpty(mCameraId)) {
                if (mCameraId.equals(cameraId)) {
                    mTorchEnabled = false;
                }
            } else {
                mTorchEnabled = false;
            }
        }
    };

    private boolean isMusicActive() {
        if (mAudioManager != null) {
            return mAudioManager.isMusicActive();
        }
        return false;
    }

    private void dismissKeyguard(IKeyguardDismissCallback callback) {
        mCallback = callback;
        try {
            WindowManagerGlobal.getWindowManagerService().dismissKeyguard(callback);
        } catch (RemoteException e) {
            if (DEBUG) {
                Log.w(TAG, "WindowManagerGlobal.getWindowManagerService() instance not alive");
            }
        }
    }

    private boolean isKeyguardShowing() {
        return mKeyguardManager.isKeyguardLocked();
    }

    private long[] getLongIntArray(Resources r, int resid) {
        int[] ar = r.getIntArray(resid);
        if (ar == null) {
            return null;
        }
        long[] out = new long[ar.length];
        for (int i = 0; i < ar.length; i++) {
            out[i] = ar[i];
        }
        return out;
    }

    private boolean isDisabledByPhoneState() {
        if (mTelecomManager != null) {
            return mTelecomManager.isInCall() || mTelecomManager.isRinging();
        }
        return false;
    }
}
