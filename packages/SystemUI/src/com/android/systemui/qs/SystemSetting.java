package com.android.systemui.qs;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings.System;

public abstract class SystemSetting extends ContentObserver {
    private final Context mContext;
    private boolean mCurrentUserOnly;
    private final String mSettingName;

    public abstract void handleValueChanged(int i, boolean z);

    public SystemSetting(Context context, Handler handler, String settingName) {
        this(context, handler, settingName, false);
    }

    public SystemSetting(Context context, Handler handler, String settingName, boolean currentUserOnly) {
        super(handler);
        mContext = context;
        mSettingName = settingName;
        mCurrentUserOnly = currentUserOnly;
    }

    public int getValue() {
        return getValue(0);
    }

    public int getValue(int def) {
        if (mCurrentUserOnly) {
            return System.getIntForUser(mContext.getContentResolver(), mSettingName, def, -2);
        }
        return System.getInt(mContext.getContentResolver(), mSettingName, def);
    }

    public void setValue(int value) {
        if (mCurrentUserOnly) {
            System.putIntForUser(mContext.getContentResolver(), mSettingName, value, -2);
        } else {
            System.putInt(mContext.getContentResolver(), mSettingName, value);
        }
    }

    public void setListening(boolean listening) {
        if (!listening) {
            mContext.getContentResolver().unregisterContentObserver(this);
        } else if (mCurrentUserOnly) {
            mContext.getContentResolver().registerContentObserver(System.getUriFor(mSettingName), false, this, -2);
        } else {
            mContext.getContentResolver().registerContentObserver(System.getUriFor(mSettingName), false, this);
        }
    }

    public void onChange(boolean selfChange) {
        handleValueChanged(getValue(), selfChange);
    }
}
