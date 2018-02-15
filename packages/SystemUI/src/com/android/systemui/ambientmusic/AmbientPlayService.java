/*
 * Copyright (c) 2018 CypherOS
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

package com.android.systemui.ambientmusic;

import android.app.Service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.util.Log;

import com.android.systemui.R;
import com.android.systemui.ambientmusic.lunasense.AmbientPlayRecognition;
import com.android.systemui.ambientmusic.lunasense.AmbientPlayRecognition.Results;

import java.lang.CharSequence;

public class AmbientPlayService extends Service implements AmbientPlayRecognition.Callback {

    private static final String TAG = "AmbientPlayService";
	
    public static Context mContext;

	private AmbientIndicationContainer mIndicationContainer;
    private AmbientPlayRecognition mRecognition;
    private AmbientPlayRecognition.Results mResult;
    private Handler mHandler;
    private HandlerThread mHandlerThread;

    public AmbientPlayService(Context context) {
       mContext = context;
    }

    public static void start(Context context) {
        start(context, null);
    }

    private static void start(Context context, String action) {
        Intent intent = new Intent(context, AmbientPlayService.class);
        intent.setAction(action);
        context.startService(intent);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        Log.v(TAG, "Service is bound");
        return null;
    }

    public void onCreate() {
        super.onCreate();

        Log.v(TAG, "Service is starting....");

        mHandlerThread = new HandlerThread("Luna Ambient Services");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        startListening();
    }

    @Override
    public void onDestroy() {
        mHandlerThread.quitSafely();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        return START_STICKY;
    }
  
    private Runnable mStartRecognition = new Runnable() {
        @Override
        public void run() {
            Log.v(TAG, "Will start listening again in 10 seconds");
            startListening();
        }
    };

    private Runnable mStopRecognition = new Runnable() {
        @Override
        public void run() {
            mRecognition.stopRecording();
        }
    };

    private void startListening() {
        mRecognition = new AmbientPlayRecognition(AmbientPlayService.this);
        mRecognition.startRecording();
        // If no match is found in 19 seconds, stop listening(The buffer has a max size of 20)
        mHandler.postDelayed(mStopRecognition, 19000);
    }

    @Override
    public void onResult(AmbientPlayRecognition.Results result) {
        mResult = result;
		
        // Check if there's a match before continuing
        if (result.TrackName != null && result.ArtistName != null) {
			((AmbientIndicationContainer) mIndicationContainer).setIndication(result.TrackName, result.ArtistName);
			Log.d(TAG, "Setting indication for track info");
			
			// Then post the notification
            showSongNotification(result.TrackName, result.ArtistName);
        }
        mHandler.removeCallbacks(mStartRecognition);
        mHandler.removeCallbacks(mStopRecognition);
        mHandler.postDelayed(mStartRecognition, 10000); // 10 seconds, change to 60 when finished debugging

    }

    @Override
    public void onNoMatch() {
        mHandler.removeCallbacks(mStartRecognition);
        mHandler.removeCallbacks(mStopRecognition);
        mHandler.postDelayed(mStartRecognition, 10000); // 10 seconds, change to 60 when finished debugging
    }

    @Override
    public void onAudioLevel(final float level) {
        // no op
    }

    @Override
    public void onError() {
        mHandler.removeCallbacks(mStartRecognition);
        mHandler.removeCallbacks(mStopRecognition);
        mHandler.postDelayed(mStartRecognition, 10000); // 10 seconds, change to 60 when finished debugging
    }

    private void showSongNotification(String trackName, String artistName) {
        Notification.Builder mBuilder =
                new Notification.Builder(mContext, "music_recognized_channel");
        final Bundle extras = Bundle.forPair(Notification.EXTRA_SUBSTITUTE_APP_NAME,
                mContext.getResources().getString(R.string.ambient_play_notification_title));
        mBuilder.setSmallIcon(R.drawable.ic_music_note_24dp);
        mBuilder.setContentText(String.format(mContext.getResources().getString(R.string.ambient_play_track_information),
                                              trackName, artistName));
        mBuilder.setColor(mContext.getResources().getColor(com.android.internal.R.color.system_notification_accent_color));
        mBuilder.setAutoCancel(false);
        mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        mBuilder.setLocalOnly(true);
        mBuilder.setShowWhen(true);
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setTicker(String.format(mContext.getResources().getString(R.string.ambient_play_track_information),
                                         trackName, artistName));
        mBuilder.setExtras(extras);

        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("music_recognized_channel",
                    mContext.getResources().getString(R.string.ambient_play_recognition_channel),
                    NotificationManager.IMPORTANCE_MIN);
            mNotificationManager.createNotificationChannel(channel);
        }

        mNotificationManager.notify(122306791, mBuilder.build());
    }
}