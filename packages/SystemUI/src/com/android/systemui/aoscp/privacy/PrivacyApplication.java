/*
 * Copyright (C) 2019 CypherOS
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

package com.android.systemui.aoscp.privacy;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.util.Log;

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
		try {
            ApplicationInfo appInfo = mContext.getPackageManager().getApplicationInfo(getPackageName(), 0);
            return appInfo;
        } catch (NameNotFoundException e) {
            Log.d("PrivacyApplication", "Application not found");
			return null;
        }
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
				if (this.mPackageName.equals(privacyApp.mPackageName)) {
                    if ((mUid == privacyApp.mUid) && this.mContext.equals(privacyApp.mContext)) {
						return true;
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