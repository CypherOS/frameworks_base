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
import android.os.UserHandle;
import android.util.Log;

import com.android.systemui.R;
import com.android.systemui.aoscp.micode.LiveOp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages active operations and presents them to the user
 */
public class LiveOpsControllerImpl implements LiveOpsController, AppOpsManager.OnOpActiveChangedListener {

    private static final String TAG = "LiveOpsControllerImpl";

    private static final int[] OPS = new int[] {AppOpsManager.OP_CAMERA,
            AppOpsManager.OP_RECORD_AUDIO,
            AppOpsManager.OP_COARSE_LOCATION,
            AppOpsManager.OP_FINE_LOCATION,
            AppOpsManager.OP_MONITOR_HIGH_POWER_LOCATION};

    private ArrayList<Callback> mCallbacks = new ArrayList<Callback>();

    private AppOpsManager mAppOpsManager;
    private Context mContext;
    private List<LiveOp> mActiveOps = new ArrayList();
    private Set<String> mActivePackages = new HashSet<>();
    private Handler mHandler = new Handler();

    public LiveOpsControllerImpl(Context context) {
        mContext = context;
        mAppOpsManager = context.getSystemService(AppOpsManager.class);
    }

    @Override
    public void addCallback(Callback cb) {
        synchronized (mCallbacks) {
            if (mCallbacks.isEmpty()) {
                mAppOpsManager.startWatchingActive(OPS, this);
                synchronized (mActiveOps) {
                    mActiveOps.clear();
                }
                synchronized (mActivePackages) {
                    mActivePackages.clear();
                    initActivePackages();
                }
            }
            mCallbacks.add(cb);
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
    public List<LiveOp> getActiveOps() {
        ArrayList activeOps = new ArrayList();
        synchronized (mActiveOps) {
            int size = mActiveOps.size();
            for (int ops = 0; ops < size; ops++) {
                LiveOp opItem = (LiveOp) mActiveOps.get(ops);
                activeOps.add(opItem);
            }
        }
        return activeOps;
    }

    private void initActivePackages() {
        List<AppOpsManager.PackageOps> packages = mAppOpsManager.getPackagesForOps(OPS);
        if (packages != null) {
            for (AppOpsManager.PackageOps ops : packages) {
                if (mAppOpsManager.isOperationActive(AppOpsManager.OP_CAMERA,
                        ops.getUid(), ops.getPackageName())) {
                    mActivePackages.add(ops.getPackageName());
                } else if (mAppOpsManager.isOperationActive(AppOpsManager.OP_RECORD_AUDIO,
                        ops.getUid(), ops.getPackageName())) {
                    mActivePackages.add(ops.getPackageName());
                } else if (mAppOpsManager.isOperationActive(AppOpsManager.OP_COARSE_LOCATION,
                        ops.getUid(), ops.getPackageName())) {
                    mActivePackages.add(ops.getPackageName());
                } else if (mAppOpsManager.isOperationActive(AppOpsManager.OP_FINE_LOCATION,
                        ops.getUid(), ops.getPackageName())) {
                    mActivePackages.add(ops.getPackageName());
                } else if (mAppOpsManager.isOperationActive(AppOpsManager.OP_MONITOR_HIGH_POWER_LOCATION,
                        ops.getUid(), ops.getPackageName())) {
                    mActivePackages.add(ops.getPackageName());
                }
            }
        }
    }

    private LiveOp containsLiveOp(List<LiveOp> list, int op, int uid, String packageName) {
        int size = list.size();
        for (int items = 0; items < size; items++) {
            LiveOp opItem = (LiveOp) list.get(items);
            if (opItem.getOp() == op && opItem.getUid() == uid && opItem.getPackageName().equals(packageName)) {
                return opItem;
            }
        }
        return null;
    }

    private void notifySubscribers(List<LiveOp> activeOps, Set<String> activePkgs) {
        Log.d(TAG, "Notifying subscribers");
        synchronized (mCallbacks) {
            for (Callback cb : mCallbacks) {
                cb.onPrivacyChanged(activeOps, activePkgs);
            }
        }
    }

    private void updateActiveOps(int op, int uid, String packageName, boolean active) {
        List<LiveOp> oldOps, newOps;
        synchronized (mActiveOps) {
            LiveOp opItem = containsLiveOp(mActiveOps, op, uid, packageName);
            oldOps = mActiveOps;
            if (opItem == null && active) {
                mActiveOps.add(new LiveOp(op, uid, packageName, System.currentTimeMillis()));
            } else {
                mActiveOps.remove(opItem);
            }
            newOps = mActiveOps;
        }
        updateActivePackages(newOps, packageName, active);
    }

    private void updateActivePackages(List<LiveOp> newOps, String packageName, boolean active) {
        Set<String> oldPkgs, newPkgs;
        int oldCount, newCount;
        synchronized (mActivePackages) {
            oldPkgs = mActivePackages;
            oldCount = mActivePackages.size();
            if (active) {
                mActivePackages.add(packageName);
            } else {
                mActivePackages.remove(packageName);
            }
            newPkgs = mActivePackages;
            newCount = mActivePackages.size();
        }
        if (oldCount != newCount) {
            mHandler.post(() -> notifySubscribers(newOps, newPkgs));
        }
    }

    @Override
    public void onOpActiveChanged(int op, int uid, String packageName, boolean active) {
        Log.d(TAG, "Operation changed for " + packageName + " to " + active);
        updateActiveOps(op, uid, packageName, active);
    }
}
