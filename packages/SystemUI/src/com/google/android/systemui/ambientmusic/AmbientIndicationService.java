package com.google.android.systemui.ambientmusic;

import android.app.AlarmManager;
import android.app.NotificationManager;
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
import android.service.notification.StatusBarNotification;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.util.wakelock.DelayedWakeLock;
import com.android.systemui.util.wakelock.WakeLock;

import com.google.android.systemui.ambientmusic.AmbientIndicationContainer;
import com.google.android.systemui.ambientmusic.AmbientIndicationAlarmListener;

public class AmbientIndicationService extends BroadcastReceiver {
	
    private final AlarmManager mAlarmManager;
    private final AmbientIndicationContainer mAmbientIndicationContainer;
    private final KeyguardUpdateMonitorCallback mCallback;
    private final Context mContext;
    private final Handler mHandler;
    private final AlarmManager.OnAlarmListener mHideIndicationListener;
    private final WakeLock mWakeLock;
	
	private NotificationManager mNotificationManager;
	
	public AmbientIndicationService get(Context context, AmbientIndicationContainer ambientIndicationContainer) {
		return new AmbientIndicationService(context, ambientIndicationContainer);
	}

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
        if (getSendingUserId() == getCurrentUser()) return bl2;
        if (getSendingUserId() != -1) return false;
        return bl;
    }

    public void hideIndicationContainer() {
        mAmbientIndicationContainer.hideIndication();
    }

    public void onReceive(Context object, Intent intent) {
        if (!isForCurrentUser()) {
            return;
        }
        if (intent.getAction().equals("co.aoscp.lunasense.action.AMBIENT_PLAY_SHOW")) {
            CharSequence charSequence = (CharSequence)intent.getCharSequenceExtra("co.aoscp.lunasense.extra.TRACK_BY_ARTIST");
            //long l = Math.min(Math.max(intent.getLongExtra("com.google.android.ambientindication.extra.TTL_MILLIS", 180000L), 0L), 180000L);
            mAmbientIndicationContainer.setIndication(charSequence);
            //this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + l, "AmbientIndication", this.mHideIndicationListener, null);
            return;
        }
		StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();
		for (StatusBarNotification notification : notifications) {
			if (notification.getId() != 122306791) {
				mAlarmManager.cancel(mHideIndicationListener);
				mAmbientIndicationContainer.hideIndication();
			}
		}
    }

    @VisibleForTesting
    void onUserSwitched() {
        mAmbientIndicationContainer.hideIndication();
    }

    @VisibleForTesting
    void start() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("co.aoscp.lunasense.action.AMBIENT_PLAY_SHOW");
        //intentFilter.addAction("com.google.android.ambientindication.action.AMBIENT_INDICATION_HIDE");
        mContext.registerReceiverAsUser((BroadcastReceiver)this, UserHandle.ALL, intentFilter, "co.aoscp.lunasense.permission.AMBIENT_PLAY", null);
        KeyguardUpdateMonitor.getInstance(mContext).registerCallback(mCallback);
    }

}

