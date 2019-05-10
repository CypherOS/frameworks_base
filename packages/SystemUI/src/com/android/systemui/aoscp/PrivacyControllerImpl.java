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

package com.android.systemui.aoscp;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.android.systemui.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages active operations and presents them to the user
 */
public class PrivacyControllerImpl implements PrivacyController, AppOpsManager.OnOpActiveChangedListener {

    private static final String TAG = "PrivacyControllerImpl";

    private static final int[] PRIVACY_OPS = new int[] {AppOpsManager.OP_CAMERA,
            AppOpsManager.OP_RECORD_AUDIO,
            AppOpsManager.OP_COARSE_LOCATION,
			AppOpsManager.OP_FINE_LOCATION,
			AppOpsManager.OP_MONITOR_LOCATION,
			AppOpsManager.OP_MONITOR_HIGH_POWER_LOCATION};

    private ArrayList<Callback> mCallbacks = new ArrayList<Callback>();

    private AppOpsManager mAppOpsManager;
    private Set<String> mActiveOps = new HashSet<>();
    private List<AppOpsManager.PackageOps> mPackages;
    private Handler mHandler = new Handler();
    private Object mLock = new Object();
    private int mAppOpCode;
    private String mAppOpPkg;

    public PrivacyControllerImpl(Context context) {
        mAppOpsManager = context.getSystemService(AppOpsManager.class);
        mPackages = mAppOpsManager.getPackagesForOps(PRIVACY_OPS);
    }

    @Override
    public void addCallback(Callback cb) {
        synchronized (mCallbacks) {
            if (mCallbacks.isEmpty()) {
                mAppOpsManager.startWatchingActive(PRIVACY_OPS, this);

                synchronized (mActiveOps) {
                    mActiveOps.clear();
                    initActiveOps();
                }
            }
            mCallbacks.add(cb);
            cb.onPrivacyChanged(mActiveOps.size(), mAppOpCode, mAppOpPkg);
        }
    }

    @Override
    public void removeCallback(Callback cb) {
        synchronized (mCallbacks) {
            mCallbacks.remove(cb);
            if (mCallbacks.isEmpty()) {
                mAppOpsManager.stopWatchingActive(this);
            }
        }
    }

    @Override
    public int getActiveOps() {
        synchronized (mActiveOps) {
            return mActiveOps.size();
        }
    }

    @Override
    public int getActiveOpCode() {
        synchronized (mLock) {
            return mAppOpCode;
        }
    }

    @Override
    public boolean isActiveOp(int op) {
        for (AppOpsManager.PackageOps ops : mPackages) {
            return mAppOpsManager.isOperationActive(op,
                    ops.getUid(), ops.getPackageName());
        }
        return false;
    }

    private void initActiveOps() {
        if (mPackages != null) {
            for (AppOpsManager.PackageOps ops : mPackages) {
                if (isActiveOp(AppOpsManager.OP_CAMERA)) {
                    mAppOpCode = AppOpsManager.OP_CAMERA;
                    mActiveOps.add(ops.getPackageName());
                } else if (isActiveOp(AppOpsManager.OP_RECORD_AUDIO)) {
                    mAppOpCode = AppOpsManager.OP_RECORD_AUDIO;
                    mActiveOps.add(ops.getPackageName());
                } else if (isActiveOp(AppOpsManager.OP_COARSE_LOCATION)) {
                    mAppOpCode = AppOpsManager.OP_COARSE_LOCATION;
                    mActiveOps.add(ops.getPackageName());
                } else if (isActiveOp(AppOpsManager.OP_FINE_LOCATION)) {
                    mAppOpCode = AppOpsManager.OP_FINE_LOCATION;
                    mActiveOps.add(ops.getPackageName());
				} else if (isActiveOp(AppOpsManager.OP_MONITOR_LOCATION)) {
                    mAppOpCode = AppOpsManager.OP_MONITOR_LOCATION;
                    mActiveOps.add(ops.getPackageName());
				} else if (isActiveOp(AppOpsManager.OP_MONITOR_HIGH_POWER_LOCATION)) {
                    mAppOpCode = AppOpsManager.OP_MONITOR_HIGH_POWER_LOCATION;
                    mActiveOps.add(ops.getPackageName());
                }
            }
        }
    }

    private void notifySubscribers(int activeOps, int opCode, String opPackage) {
        synchronized (mCallbacks) {
            for (Callback cb : mCallbacks) {
                cb.onPrivacyChanged(activeOps, opCode, opPackage);
            }
        }
    }

    @Override
    public void onOpActiveChanged(int op, int uid, String packageName, boolean active) {
        Log.d(TAG, "Microphone operation changed for " + packageName + " to " + active);
        mAppOpPkg = packageName;
        mAppOpCode = op;
        int oldOps, newOps;
        synchronized (mActiveOps) {
            oldOps = mActiveOps.size();
            if (active) {
                mActiveOps.add(packageName);
            } else {
                mActiveOps.remove(packageName);
            }
            newOps = mActiveOps.size();
        }

        if (oldOps != newOps) {
            mHandler.post(() -> notifySubscribers(newOps, mAppOpCode, mAppOpPkg));
        }
    }
}