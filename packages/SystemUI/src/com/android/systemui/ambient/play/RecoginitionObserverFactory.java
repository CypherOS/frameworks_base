/*
 * Copyright (C) 2014 Fastboot Mobile, LLC.
 * Copyright (C) 2018 CypherOS
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses>.
 */
package com.android.systemui.ambient.play;

import android.ambient.AmbientIndicationManager;
import android.ambient.play.RecoginitionObserver;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class helping audio fingerprinting for recognition.
 * This is a factory class that extends the primary recognition class.
 */
public class RecoginitionObserverFactory extends RecoginitionObserver {
	
	public RecoginitionObserver(Context context) {
        return RecoginitionObserver(context);
    }

    @Override
    public void startRecording() {
        mBufferIndex = 0;
		if (!mRecognitionEnabled) return;
		if (mManager.isCharging()) {
			Log.d(TAG, "Cannot observe while charging, aborting..");
			return;
		}
        try {
            mRecorder.startRecording();
            mRecThread = new RecorderThread();
            mRecThread.start();
        } catch (IllegalStateException e) {
            Log.d(TAG, "Cannot start recording for recognition", e);
			mManager.dispatchRecognitionError();
        }
    }

    @Override
    public void stopRecording() {
        if (mRecThread != null && mRecThread.isAlive()) {
            Log.d(TAG, "Interrupting recorder thread");
            mRecThread.interrupt();
        }

        if (mRecorder != null) {
            Log.d(TAG, "Stopping recorder");
            mRecorder.stop();
            mRecorder = null;
        }
    }
}
