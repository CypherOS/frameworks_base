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

import com.google.android.systemui.ambientmusic.AmbientIndicationContainer;
import com.google.android.systemui.ambientmusic.AmbientIndicationAlarmListener;

public class AmbientIndicationService
extends BroadcastReceiver {
    private final AlarmManager mAlarmManager;
    private final AmbientIndicationContainer mAmbientIndicationContainer;
    private final KeyguardUpdateMonitorCallback mCallback;
    private final Context mContext;
    private final Handler mHandler;
    private final AlarmManager.OnAlarmListener mHideIndicationListener;
    private final WakeLock mWakeLock;

    public AmbientIndicationService(Context context, AmbientIndicationContainer ambientIndicationContainer) {
        this.mCallback = new KeyguardUpdateMonitorCallback(){

            @Override
            public void onUserSwitchComplete(int n) {
                AmbientIndicationService.this.onUserSwitched();
            }
        };
        this.mContext = context;
        this.mAmbientIndicationContainer = ambientIndicationContainer;
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mAlarmManager = (AlarmManager)context.getSystemService(AlarmManager.class);
        this.mWakeLock = this.createWakeLock(this.mContext, this.mHandler);
        this.mHideIndicationListener = new AmbientIndicationAlarmListener((Object)this);
        this.start();
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
        boolean bl;
        boolean bl2 = bl = true;
        if (this.getSendingUserId() == this.getCurrentUser()) return bl2;
        if (this.getSendingUserId() != -1) return false;
        return bl;
    }

    public void hideIndicationContainer() {
        this.mAmbientIndicationContainer.hideIndication();
    }

    public void onReceive(Context object, Intent intent) {
        if (!this.isForCurrentUser()) {
            return;
        }
        if (intent.getAction().equals("co.aoscp.lunasense.action.AMBIENT_PLAY_SHOW")) {
            CharSequence charSequence = (CharSequence)intent.getCharSequenceExtra("co.aoscp.lunasense.extra.TRACK_BY_ARTIST");
            //long l = Math.min(Math.max(intent.getLongExtra("com.google.android.ambientindication.extra.TTL_MILLIS", 180000L), 0L), 180000L);
            this.mAmbientIndicationContainer.setIndication(charSequence);
            //this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + l, "AmbientIndication", this.mHideIndicationListener, null);
            return;
        }
        //if (!object.equals("com.google.android.ambientindication.action.AMBIENT_INDICATION_HIDE")) return;
        //this.mAlarmManager.cancel(this.mHideIndicationListener);
        //this.mAmbientIndicationContainer.hideIndication();
    }

    @VisibleForTesting
    void onUserSwitched() {
        this.mAmbientIndicationContainer.hideIndication();
    }

    @VisibleForTesting
    void start() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("co.aoscp.lunasense.action.AMBIENT_PLAY_SHOW");
        //intentFilter.addAction("com.google.android.ambientindication.action.AMBIENT_INDICATION_HIDE");
        this.mContext.registerReceiverAsUser((BroadcastReceiver)this, UserHandle.ALL, intentFilter, null);
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mCallback);
    }

}

