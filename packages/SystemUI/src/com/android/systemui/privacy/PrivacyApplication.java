package com.android.systemui.privacy;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;

import kotlin.Lazy;
import kotlin.LazyKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.PropertyReference1Impl;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KProperty;

public final class PrivacyApplication implements Comparable<PrivacyApplication> {

    private Context mContext;
    private String mPackageName;
    private int mUid;

    public PrivacyApplication(String packageName, int uid, Context context) {
        mPackageName = packageName;
        mUid = uid;
        mContext = context;
    }

	public ApplicationInfo getApplicationInfo() {
        return mContext.getPackageManager().getApplicationInfo(getPackageName(), 0);
    }

    public String getApplicationName() {
		if (getApplicationInfo() != null) {
			CharSequence appLabel = mContext.getPackageManager().getApplicationLabel(getApplicationInfo());
			if (appLabel != null) {
				String label = (String) appLabel;
				if (label != null) {
                    return label;
                }
			}
		}
		return getPackageName();
    }

    public Drawable getIcon() {
        if (getApplicationInfo() != null) {
			Drawable appIcon = mContext.getPackageManager().getApplicationIcon(getApplicationInfo());
			if (appIcon != null) {
                return appIcon;
            }
        }
        return mContext.getDrawable(com.android.internal.R.drawable.sym_def_app_icon);
    }

    public int hashCode() {
        int hashCode = (((mPackageName != null ? mPackageName.hashCode() : 0) * 31) + Integer.hashCode(mUid)) * 31;
		int extra = mContext != null ? mContext.hashCode() : 0;
        return hashCode + extra;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("PrivacyApplication(packageName=");
        stringBuilder.append(mPackageName);
        stringBuilder.append(", uid=");
        stringBuilder.append(mUid);
        stringBuilder.append(", context=");
        stringBuilder.append(mContext);
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    public Context getContext() {
        return mContext;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public int getUid() {
        return mUid;
    }

	public boolean equals(Object obj) {
        if (this != obj) {
            if (obj instanceof PrivacyApplication) {
                PrivacyApplication privacyApp = (PrivacyApplication) obj;
                if (Intrinsics.areEqual(mPackageName, privacyApp.mPackageName)) {
                    if ((mUid == privacyApp.mUid) && Intrinsics.areEqual(mContext, privacyApp.mContext)) {
                    }
                }
            }
            return false;
        }
        return true;
    }

    public int compareTo(PrivacyApplication privacyApp) {
        return getApplicationName().compareTo(privacyApp.getApplicationName());
    }
}
