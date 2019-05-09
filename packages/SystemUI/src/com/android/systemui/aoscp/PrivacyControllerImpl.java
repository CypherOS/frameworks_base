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
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    private static final int[] PRIVACY_OPS = new int[] {AppOpsManager.OP_CAMERA,
            AppOpsManager.OP_RECORD_AUDIO,
            AppOpsManager.OP_COARSE_LOCATION,
			AppOpsManager.OP_FINE_LOCATION};

    private ArrayList<Callback> mCallbacks = new ArrayList<Callback>();

    private AppOpsManager mAppOpsManager;
    private Set<String> mActiveOps = new HashSet<>();
    private Handler mHandler = new Handler();
    private int mAppOpCode;
    private String mAppOpPkg;

    public PrivacyControllerImpl(Context context) {
        mAppOpsManager = context.getSystemService(AppOpsManager.class);
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

    private void initActiveOps() {
        List<AppOpsManager.PackageOps> packages = mAppOpsManager.getPackagesForOps(PRIVACY_OPS);
        if (packages != null) {
            for (AppOpsManager.PackageOps ops : packages) {
                if (mAppOpsManager.isOperationActive(AppOpsManager.OP_CAMERA,
                        ops.getUid(), ops.getPackageName())) {
                    mActiveOps.add(ops.getPackageName());
                } else if (mAppOpsManager.isOperationActive(AppOpsManager.OP_RECORD_AUDIO,
                        ops.getUid(), ops.getPackageName())) {
                    mActiveOps.add(ops.getPackageName());
                } else if (mAppOpsManager.isOperationActive(AppOpsManager.OP_COARSE_LOCATION,
                        ops.getUid(), ops.getPackageName())) {
                    mActiveOps.add(ops.getPackageName());
                } else if (mAppOpsManager.isOperationActive(AppOpsManager.OP_FINE_LOCATION,
                        ops.getUid(), ops.getPackageName())) {
                    mActiveOps.add(ops.getPackageName());
                }
            }
        }
    }

    private void notifySubscribers(int sessionCount, int opCode) {
        synchronized (mCallbacks) {
            for (Callback cb : mCallbacks) {
                cb.onPrivacyChanged(sessionCount, opCode);
            }
        }
    }

    @Override
    public void onOpActiveChanged(int op, int uid, String packageName, boolean active) {
        Log.d(TAG, "Microphone operation changed for " + packageName + " to " + active);
        mAppOpCode = code;
        mAppOpPkg = packageName;
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