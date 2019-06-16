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

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.android.systemui.R;

public class LiveOp {

    private int op;
    private String packageName;
    private String state;
    private long timeStarted;
    private int uid;

    public LiveOp(int op, int uid, String packageName, long timeStarted) {
        this.op = op;
        this.uid = uid;
        this.packageName = packageName;
        this.timeStarted = timeStarted;
        StringBuilder sb = new StringBuilder();
        sb.append("LiveOp(");
        sb.append("Op code=");
        sb.append(op);
        String str2 = ", ";
        sb.append(str2);
        sb.append("UID=");
        sb.append(uid);
        sb.append(str2);
        sb.append("Package name=");
        sb.append(packageName);
        sb.append(")");
        this.state = sb.toString();
    }

    public int getOp() {
        return this.op;
    }

    public int getUid() {
        return this.uid;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public String getApplicationName(Context context) {
        final PackageManager pm = context.getPackageManager();
        ApplicationInfo info;
        try {
            info = pm.getApplicationInfo(this.packageName, 0);
        } catch (NameNotFoundException e) {
            info = null;
        }
        if (info != null) {
            String appName = pm.getApplicationLabel(info).toString();
            return appName;
        }
        return null;
    }

    public Drawable getIcon(Context context) {
        Drawable icon = null;
        if (this.op == AppOpsManager.OP_CAMERA) {
            icon = context.getResources().getDrawable(R.drawable.stat_sys_camera);
        } else if (this.op == AppOpsManager.OP_RECORD_AUDIO) {
            icon = context.getResources().getDrawable(R.drawable.stat_sys_mic_none);
        } else if (this.op == AppOpsManager.OP_COARSE_LOCATION || this.op == AppOpsManager.OP_FINE_LOCATION 
                || this.op == AppOpsManager.OP_MONITOR_LOCATION || this.op == AppOpsManager.OP_MONITOR_HIGH_POWER_LOCATION) {
            icon = context.getResources().getDrawable(R.drawable.stat_sys_location);
        }
        return icon;
    }

    public Drawable getAppIcon(Context context) {
        final PackageManager pm = context.getPackageManager();
        ApplicationInfo info;
        try {
            info = pm.getApplicationInfo(this.packageName, 0);
        } catch (NameNotFoundException e) {
            info = null;
        }
        if (info != null) {
            Drawable appIcon = context.getPackageManager().getApplicationIcon(info);
            if (appIcon != null) {
                return appIcon;
            }
        }
        return context.getDrawable(com.android.internal.R.drawable.sym_def_app_icon);
    }

    public String getOpName(Context context) {
        String opName = null;
        if (this.op == AppOpsManager.OP_CAMERA) {
            opName = context.getResources().getString(R.string.op_type_camera);
        } else if (this.op == AppOpsManager.OP_RECORD_AUDIO) {
            opName = context.getResources().getString(R.string.op_type_microphone);
        } else if (this.op == AppOpsManager.OP_COARSE_LOCATION || this.op == AppOpsManager.OP_FINE_LOCATION 
                || this.op == AppOpsManager.OP_MONITOR_LOCATION || this.op == AppOpsManager.OP_MONITOR_HIGH_POWER_LOCATION) {
            opName = context.getResources().getString(R.string.op_type_location);
        }
        return opName;
    }

    public String toString() {
        return this.state;
    }
}
