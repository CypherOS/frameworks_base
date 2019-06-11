package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.SysUIToast;
import com.android.systemui.plugins.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.SystemSetting;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.qs.tileimpl.QSTileImpl.ResourceIcon;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NetworkController.SignalCallback;
import com.android.systemui.statusbar.policy.NetworkControllerImpl;

import java.lang.reflect.Method;

import org.codeaurora.internal.IExtTelephony;
import org.codeaurora.internal.IExtTelephony.Stub;

public class DataSwitchTile extends QSTileImpl<BooleanState> {

    private boolean mCanSwitch = true;
    protected final NetworkController mController;
    private IExtTelephony mExtTelephony = null;
    private MyCallStateListener mPhoneStateListener;
    private boolean mRegistered = false;
    protected final DataSwitchSignalCallback mSignalCallback = new DataSwitchSignalCallback();
    private int mSimCount = 0;
    BroadcastReceiver mSimReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "mSimReceiver:onReceive");
            refreshState();
        }
    };
    private SubscriptionManager mSubscriptionManager;
    private TelephonyManager mTelephonyManager;

    class MyCallStateListener extends PhoneStateListener {
        MyCallStateListener() {
        }

        public void onCallStateChanged(int state, String arg1) {
            mCanSwitch = mTelephonyManager.getCallState() == 0;
            refreshState();
        }
    }

    protected final class DataSwitchSignalCallback implements SignalCallback {
        protected DataSwitchSignalCallback() {
        }
    }

    public DataSwitchTile(QSHost host) {
        super(host);
        mSubscriptionManager = SubscriptionManager.from(host.getContext());
        mTelephonyManager = (TelephonyManager) mContext.getSystemService("phone");
        mPhoneStateListener = new MyCallStateListener();
        mController = (NetworkController) Dependency.get(NetworkController.class);
        refreshState();
    }

    public boolean isAvailable() {
        int count = TelephonyManager.getDefault().getPhoneCount();
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("phoneCount: ");
        stringBuilder.append(count);
        Log.d(str, stringBuilder.toString());
        return count >= 2;
    }

    public BooleanState newTileState() {
        return new BooleanState();
    }

    public void handleSetListening(boolean listening) {
        if (listening) {
            if (!mRegistered) {
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.SIM_STATE_CHANGED");
                mContext.registerReceiver(mSimReceiver, filter);
                mTelephonyManager.listen(mPhoneStateListener, 32);
                mController.addCallback(mSignalCallback);
                mRegistered = true;
            }
            refreshState();
        } else if (mRegistered) {
            mContext.unregisterReceiver(mSimReceiver);
            mTelephonyManager.listen(mPhoneStateListener, 0);
            mController.removeCallback(mSignalCallback);
            mRegistered = false;
        }
    }

    private void updateSimCount() {
        String simState = SystemProperties.get("gsm.sim.state");
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DataSwitchTile:updateSimCount:simState=");
        stringBuilder.append(simState);
        Log.d(str, stringBuilder.toString());
        int i = 0;
        mSimCount = 0;
        try {
            String[] sims = TextUtils.split(simState, ",");
            while (i < sims.length) {
                if (!(sims[i].isEmpty() || sims[i].equalsIgnoreCase("ABSENT"))) {
                    if (!sims[i].equalsIgnoreCase("NOT_READY")) {
                        mSimCount++;
                    }
                }
                i++;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error to parse sim state");
        }
        str = TAG;
        stringBuilder = new StringBuilder();
        stringBuilder.append("DataSwitchTile:updateSimCount:mSimCount=");
        stringBuilder.append(mSimCount);
        Log.d(str, stringBuilder.toString());
    }

    private void setDefaultDataSimIndex(int phoneId) {
        try {
            if (mExtTelephony == null) {
                mExtTelephony = Stub.asInterface(ServiceManager.getService("extphone"));
            }
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("oemDdsSwitch:phoneId=");
            stringBuilder.append(phoneId);
            Log.d(str, stringBuilder.toString());
            Method method = mExtTelephony.getClass().getDeclaredMethod("oemDdsSwitch", new Class[]{Integer.TYPE});
            method.setAccessible(true);
            method.invoke(mExtTelephony, new Object[]{Integer.valueOf(phoneId)});
        } catch (Exception e) {
            Log.d(TAG, "setDefaultDataSimId", e);
            Log.d(TAG, "clear ext telephony service ref");
            mExtTelephony = null;
        }
    }

    public void handleClick() {
        if (!mCanSwitch) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Call state=");
            stringBuilder.append(mTelephonyManager.getCallState());
            Log.d(str, stringBuilder.toString());
        } else if (mSimCount == 0) {
            Log.d(TAG, "handleClick:no sim card");
            SysUIToast.makeText(mContext, mContext.getString(R.string.quick_settings_data_switch_toast_0), 1).show();
        } else if (mSimCount == 1) {
            Log.d(TAG, "handleClick:only one sim card");
            SysUIToast.makeText(mContext, mContext.getString(R.string.quick_settings_data_switch_toast_1), 1).show();
        } else {
            AsyncTask.execute(() -> {
                setDefaultDataSimIndex(1 - mSubscriptionManager.getDefaultDataPhoneId());
                refreshState();
            });
        }
    }

    public Intent getLongClickIntent() {
        return new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
    }

    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_data_switch_label);
    }

    /* Access modifiers changed, original: protected */
    public void handleUpdateState(BooleanState state, Object arg) {
        boolean value;
        if (arg == null) {
            boolean value2;
            int defaultPhoneId = mSubscriptionManager.getDefaultDataPhoneId();
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("default data phone id=");
            stringBuilder.append(defaultPhoneId);
            Log.d(str, stringBuilder.toString());
            if (defaultPhoneId == 0) {
                value2 = true;
            } else {
                value2 = false;
            }
            value = value2;
        } else {
            value = ((Boolean) arg).booleanValue();
        }
        updateSimCount();
        int i = mSimCount;
        int i2 = R.drawable.ic_qs_data_switch_1_disable;
        switch (i) {
            case 1:
                if (!value) {
                    i2 = R.drawable.ic_qs_data_switch_2_disable;
                }
                state.icon = ResourceIcon.get(i2);
                state.value = false;
                break;
            case 2:
                state.icon = ResourceIcon.get(value ? R.drawable.ic_qs_data_switch_1 : R.drawable.ic_qs_data_switch_2);
                state.value = true;
                break;
            default:
                state.icon = ResourceIcon.get(R.drawable.ic_qs_data_switch_1_disable);
                state.value = false;
                break;
        }
        if (value) {
            state.contentDescription = mContext.getString(R.string.accessibility_quick_settings_data_switch_changed_1);
        } else {
            state.contentDescription = mContext.getString(R.string.accessibility_quick_settings_data_switch_changed_2);
        }
        state.label = mContext.getString(R.string.quick_settings_data_switch_label);
    }

    public int getMetricsCategory() {
        return 9999;
    }

    /* Access modifiers changed, original: protected */
    public String composeChangeAnnouncement() {
        if (((BooleanState) mState).value) {
            return mContext.getString(R.string.accessibility_quick_settings_data_switch_changed_1);
        }
        return mContext.getString(R.string.accessibility_quick_settings_data_switch_changed_2);
    }
}
