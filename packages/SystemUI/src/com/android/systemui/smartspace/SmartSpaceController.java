package com.android.systemui.smartspace;

import android.app.AlarmManager;
import android.app.AlarmManager.OnAlarmListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.util.KeyValueListParser;
import android.util.Log;
import com.android.systemui.Dumpable;
import com.android.systemui.smartspace.nano.SmartspaceProto.CardWrapper;
import com.android.systemui.util.Assert;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class SmartSpaceController implements Dumpable {

    static final boolean DEBUG = Log.isLoggable("SmartSpaceController", 3);
    private static SmartSpaceController sInstance;
    private final AlarmManager mAlarmManager;
    private boolean mAlarmRegistered;
    private final Context mAppContext;
    private final Handler mBackgroundHandler;
    private final Context mContext;
    private int mCurrentUserId;
    private final SmartSpaceData mData;
    private final OnAlarmListener mExpireAlarmAction = new SmartSpaceAlarmListener(this);
    private SmartSpaceUpdateListener mListener;
    private boolean mSmartSpaceEnabledBroadcastSent;
    private final ProtoStore mStore;
    private final Handler mUiHandler;

    class SmartSpaceReceiver extends BroadcastReceiver {
        SmartSpaceReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            SmartSpaceController.onGsaChanged();
        }
    }

    private class UserSwitchReceiver extends BroadcastReceiver {
        private UserSwitchReceiver() {
        }

        UserSwitchReceiver(SmartSpaceController x0, SmartSpaceReceiver receiver) {
            this();
        }

        public void onReceive(Context context, Intent intent) {
            if (SmartSpaceController.DEBUG) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Switching user: ");
                stringBuilder.append(intent.getAction());
                stringBuilder.append(" uid: ");
                stringBuilder.append(UserHandle.myUserId());
                Log.d("SmartSpaceController", stringBuilder.toString());
            }
            if (intent.getAction().equals("android.intent.action.USER_SWITCHED")) {
                SmartSpaceController.mCurrentUserId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                SmartSpaceController.mData.clear();
                SmartSpaceController.onExpire(true);
            }
            SmartSpaceController.onExpire(true);
        }
    }

    public static SmartSpaceController get(Context context) {
        if (sInstance == null) {
            if (DEBUG) {
                Log.d("SmartSpaceController", "controller created");
            }
            sInstance = new SmartSpaceController(context.getApplicationContext());
        }
        return sInstance;
    }

    private SmartSpaceController(Context context) {
        mContext = context;
        mUiHandler = new Handler(Looper.getMainLooper());
        mStore = new ProtoStore(mContext);
        HandlerThread loaderThread = new HandlerThread("smartspace-background");
        loaderThread.start();
        mBackgroundHandler = new Handler(loaderThread.getLooper());
        mCurrentUserId = UserHandle.myUserId();
        mAppContext = context;
        mAlarmManager = (AlarmManager) context.getSystemService(AlarmManager.class);
        mData = new SmartSpaceData();
        if (!isSmartSpaceDisabledByExperiments()) {
            reloadData();
            onGsaChanged();
            context.registerReceiver(new SmartSpaceReceiver(), GSAIntents.getGsaPackageFilter("android.intent.action.PACKAGE_ADDED", "android.intent.action.PACKAGE_CHANGED", "android.intent.action.PACKAGE_REMOVED", "android.intent.action.PACKAGE_DATA_CLEARED"));
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.USER_SWITCHED");
            filter.addAction("android.intent.action.USER_UNLOCKED");
            context.registerReceiver(new UserSwitchReceiver(this, null), filter);
            context.registerReceiver(new SmartSpaceBroadcastReceiver(this), new IntentFilter("com.google.android.apps.nexuslauncher.UPDATE_SMARTSPACE"));
        }
    }

    private SmartSpaceCard loadSmartSpaceData(boolean primary) {
        CardWrapper output = new CardWrapper();
        ProtoStore protoStore = mStore;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("smartspace_");
        stringBuilder.append(mCurrentUserId);
        stringBuilder.append("_");
        stringBuilder.append(primary);
        if (protoStore.load(stringBuilder.toString(), output)) {
            return SmartSpaceCard.fromWrapper(mContext, output, primary ^ 1);
        }
        return null;
    }

    public void onNewCard(NewCardInfo card) {
        StringBuilder stringBuilder;
        if (DEBUG) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("onNewCard: ");
            stringBuilder.append(card);
            Log.d("SmartSpaceController", stringBuilder.toString());
        }
        if (card != null) {
            if (card.getUserId() != mCurrentUserId) {
                if (DEBUG) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Ignore card that belongs to another user target: ");
                    stringBuilder.append(mCurrentUserId);
                    stringBuilder.append(" current: ");
                    stringBuilder.append(mCurrentUserId);
                    Log.d("SmartSpaceController", stringBuilder.toString());
                }
                return;
            }
            mBackgroundHandler.post(new SmartSpaceCardInfoListener(this, card));
        }
    }

    public static void onNewCard(SmartSpaceController smartSpaceController, NewCardInfo card) {
        CardWrapper message = card.toWrapper(smartSpaceController.mContext);
        ProtoStore protoStore = smartSpaceController.mStore;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("smartspace_");
        stringBuilder.append(smartSpaceController.mCurrentUserId);
        stringBuilder.append("_");
        stringBuilder.append(card.isPrimary());
        protoStore.store(message, stringBuilder.toString());
        smartSpaceController.mUiHandler.post(new SmartSpaceCardListener(smartSpaceController, card, card.shouldDiscard() ? null : SmartSpaceCard.fromWrapper(smartSpaceController.mContext, message, card.isPrimary())));
    }

    public static void onNewCard(SmartSpaceController smartSpaceController, NewCardInfo card, SmartSpaceCard smartSpaceCard) {
        if (card.isPrimary()) {
            smartSpaceController.mData.mCurrentCard = smartSpaceCard;
        } else {
            smartSpaceController.mData.mWeatherCard = smartSpaceCard;
        }
        smartSpaceController.mData.handleExpire();
        smartSpaceController.update();
    }

    private void update() {
        Assert.isMainThread();
        if (DEBUG) {
            Log.d("SmartSpaceController", "update");
        }
        if (mAlarmRegistered) {
            mAlarmManager.cancel(mExpireAlarmAction);
            mAlarmRegistered = false;
        }
        long expiresMillis = mData.getExpiresAtMillis();
        if (expiresMillis > 0) {
            mAlarmManager.set(0, expiresMillis, "SmartSpace", mExpireAlarmAction, mUiHandler);
            mAlarmRegistered = true;
        }
        if (mListener != null) {
            if (DEBUG) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("notify listener data=");
                stringBuilder.append(mData);
                Log.d("SmartSpaceController", stringBuilder.toString());
            }
            mListener.onSmartSpaceUpdated(mData);
        }
    }

    private void onExpire(boolean forceExpire) {
        Assert.isMainThread();
        mAlarmRegistered = false;
        if (mData.handleExpire() || forceExpire) {
            update();
            if (UserHandle.myUserId() == 0) {
                if (DEBUG) {
                    Log.d("SmartSpaceController", "onExpire - sent");
                }
                mAppContext.sendBroadcast(new Intent("com.google.android.systemui.smartspace.EXPIRE_EVENT").setPackage("com.google.android.googlequicksearchbox").addFlags(268435456));
            }
        } else if (DEBUG) {
            Log.d("SmartSpaceController", "onExpire - cancelled");
        }
    }

    public void setListener(SmartSpaceUpdateListener listener) {
        Assert.isMainThread();
        mListener = listener;
        if (mData != null && mListener != null) {
            mListener.onSmartSpaceUpdated(mData);
        }
    }

    public void setHideSensitiveData(boolean hidePrivateData) {
        mListener.onSensitiveModeChanged(hidePrivateData);
    }

    private void onGsaChanged() {
        if (DEBUG) {
            Log.d("SmartSpaceController", "onGsaChanged");
        }
        if (UserHandle.myUserId() == 0) {
            mAppContext.sendBroadcast(new Intent("com.google.android.systemui.smartspace.ENABLE_UPDATE").setPackage("com.google.android.googlequicksearchbox").addFlags(268435456));
            mSmartSpaceEnabledBroadcastSent = true;
        }
        if (mListener != null) {
            mListener.onGsaChanged();
        }
    }

    public void reloadData() {
        mData.mCurrentCard = loadSmartSpaceData(true);
        mData.mWeatherCard = loadSmartSpaceData(false);
        update();
    }

    private boolean isSmartSpaceDisabledByExperiments() {
        boolean smartSpaceEnabled = true;
        String value = Global.getString(mContext.getContentResolver(), "always_on_display_constants");
        KeyValueListParser parser = new KeyValueListParser(',');
        try {
            parser.setString(value);
            smartSpaceEnabled = parser.getBoolean("smart_space_enabled", true);
        } catch (IllegalArgumentException e) {
            Log.e("SmartSpaceController", "Bad AOD constants");
        }
        if (smartSpaceEnabled) {
            return false;
        }
        return true;
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        writer.println();
        writer.println("SmartspaceController");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("  initial broadcast: ");
        stringBuilder.append(mSmartSpaceEnabledBroadcastSent);
        writer.println(stringBuilder.toString());
        stringBuilder = new StringBuilder();
        stringBuilder.append("  weather ");
        stringBuilder.append(mData.mWeatherCard);
        writer.println(stringBuilder.toString());
        stringBuilder = new StringBuilder();
        stringBuilder.append("  current ");
        stringBuilder.append(mData.mCurrentCard);
        writer.println(stringBuilder.toString());
        writer.println("serialized:");
        stringBuilder = new StringBuilder();
        stringBuilder.append("  weather ");
        stringBuilder.append(loadSmartSpaceData(false));
        writer.println(stringBuilder.toString());
        stringBuilder = new StringBuilder();
        stringBuilder.append("  current ");
        stringBuilder.append(loadSmartSpaceData(true));
        writer.println(stringBuilder.toString());
        stringBuilder = new StringBuilder();
        stringBuilder.append("disabled by experiment: ");
        stringBuilder.append(isSmartSpaceDisabledByExperiments());
        writer.println(stringBuilder.toString());
    }
}
