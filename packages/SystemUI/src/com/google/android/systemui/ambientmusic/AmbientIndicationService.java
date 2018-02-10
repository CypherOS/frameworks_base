/*
 * Copyright (C) 2017-2018 Google Inc.
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

package com.google.android.systemui.ambientmusic;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.SystemClock;
import android.os.UserHandle;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.util.wakelock.DelayedWakeLock;
import com.android.systemui.util.wakelock.WakeLock;

public class AmbientIndicationService extends BroadcastReceiver {
	
    private final AlarmManager mAlarmManager;
    private final AmbientIndicationContainer mAmbientIndicationContainer;
    private final KeyguardUpdateMonitorCallback mCallback;
    private final Context mContext;
    private final Handler mHandler;
    private final AlarmManager.OnAlarmListener mHideIndicationListener;
    private final WakeLock mWakeLock;

    public AmbientIndicationService(Context context, AmbientIndicationContainer ambientIndicationContainer) {
        mCallback = new KeyguardUpdateMonitorCallback(){

            @Override
            public void onUserSwitchComplete(int n) {
                AmbientIndicationService.onUserSwitched();
            }
        };
        mContext = context;
        mAmbientIndicationContainer = ambientIndicationContainer;
        mHandler = new Handler(Looper.getMainLooper());
        mAlarmManager = (AlarmManager)context.getSystemService(AlarmManager.class);
        mWakeLock = createWakeLock(mContext, mHandler);
        mHideIndicationListener = new AmbientIndicationAlarmListener((Object)this);
        start();
    }

    private boolean verifyAmbientApiVersion(Intent intent) {
        int n = intent.getIntExtra("com.google.android.ambientindication.extra.VERSION", 0);
        if (n != 1) {
            Log.e((String)"AmbientIndication", (String)("AmbientIndicationApi.EXTRA_VERSION is " + 1 + ", but received an intent with version " + n + ", dropping intent."));
            return false;
        }
        return true;
    }

    @VisibleForTesting
    WakeLock createWakeLock(Context context, Handler handler) {
        return new DelayedWakeLock(handler, WakeLock.createPartial(context, "AmbientIndication"));
    }

    @VisibleForTesting
    int getCurrentUser() {
        return KeyguardUpdateMonitor.getCurrentUser();
    }

    @VisibleForTesting
    boolean isForCurrentUser() {
        boolean enabled;
        boolean verified = enabled = true;
        if (getSendingUserId() == getCurrentUser()) return verified;
        if (getSendingUserId() != -1) return false;
        return enabled;
    }

    public void hideIndicationContainer() {
        mAmbientIndicationContainer.hideIndication();
    }

    public void onReceive(Context object, Intent intent) {
        if (!isForCurrentUser()) {
            return;
        }
        if (!verifyAmbientApiVersion(intent)) {
            return;
        }
        if (intent.getAction().equals("com.google.android.ambientindication.action.AMBIENT_INDICATION_SHOW")) {
            CharSequence charSequence = (CharSequence)intent.getCharSequenceExtra("com.google.android.ambientindication.extra.TEXT");
            PendingIntent pIntent = (PendingIntent)intent.getParcelableExtra("com.google.android.ambientindication.extra.OPEN_INTENT");
            long l = Math.min(Math.max(intent.getLongExtra("com.google.android.ambientindication.extra.TTL_MILLIS", 180000L), 0L), 180000L);
            mAmbientIndicationContainer.setIndication(charSequence, pIntent);
            mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + l, "AmbientIndication", mHideIndicationListener, null);
            return;
        }
        if (!object.equals("com.google.android.ambientindication.action.AMBIENT_INDICATION_HIDE")) return;
        mAlarmManager.cancel(mHideIndicationListener);
        mAmbientIndicationContainer.hideIndication();
    }

    @VisibleForTesting
    void onUserSwitched() {
        mAmbientIndicationContainer.hideIndication();
    }

    @VisibleForTesting
    void start() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.google.android.ambientindication.action.AMBIENT_INDICATION_SHOW");
        intentFilter.addAction("com.google.android.ambientindication.action.AMBIENT_INDICATION_HIDE");
        mContext.registerReceiverAsUser((BroadcastReceiver)this, UserHandle.ALL, intentFilter, "com.google.android.ambientindication.permission.AMBIENT_INDICATION", null);
        KeyguardUpdateMonitor.getInstance(mContext).registerCallback(mCallback);
    }

}

