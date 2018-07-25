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

package com.android.server.policy;

import android.content.Context;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Vibrator;
import android.view.KeyEvent;

public class AlertSliderHandler {
    private static final String TAG = AlertSliderHandler.class.getSimpleName();
	
	private int mNormalKeycode;
    private int mVibrationKeycode;
    private int mSilenceKeycode;
	private boolean mSystemReady = false;

    private final Context mContext;
    private final AudioManager mAudioManager;
    private final Vibrator mVibrator;

    public AlertSliderHandler(Context context) {
        mContext = context;
        mAudioManager = mContext.getSystemService(AudioManager.class);
        mVibrator = mContext.getSystemService(Vibrator.class);
    }
	
	public void systemReady() {
		mSystemReady = true;
		getConfiguration();
	}

	private void getConfiguration() {
        final Resources resources = mContext.getResources();

        mNormalKeycode = resources.getInteger(R.integer.config_sliderNormalKeyCode);
        mVibrationKeycode = resources.getInteger(R.integer.config_sliderVibrationKeyCode);
        mSilenceKeycode = resources.getInteger(R.integer.config_sliderSilenceKeyCode);
    }

    public boolean handleKeyEvent(KeyEvent event) {
		if (!mSystemReady) return false;
        int scanCode = event.getScanCode();

        switch (scanCode) {
            case mNormalKeycode:
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_NORMAL);
                break;
            case mVibrationKeycode:
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_VIBRATE);
                break;
            case mSilenceKeycode:
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_SILENT);
                break;
            default:
                return event;
        }
        doHapticFeedback();

        return true;
    }

    private void doHapticFeedback() {
        if (mVibrator == null || !mVibrator.hasVibrator()) {
            return;
        }

        mVibrator.vibrate(50);
    }
}