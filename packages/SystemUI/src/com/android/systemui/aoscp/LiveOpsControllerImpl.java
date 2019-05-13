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
import com.android.systemui.aoscp.privacy.LiveOpItem;

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
			AppOpsManager.OP_MONITOR_LOCATION,
			AppOpsManager.OP_MONITOR_HIGH_POWER_LOCATION};

    private ArrayList<Callback> mCallbacks = new ArrayList<Callback>();

    private AppOpsManager mAppOpsManager;
	private Context mContext;
	private List<LiveOpItem> mActiveOps = new ArrayList();
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
    public List<LiveOpItem> getActiveOps() {
        ArrayList activeOps = new ArrayList();
        synchronized (mActiveOps) {
            int size = mActiveOps.size();
            for (int ops = 0; ops < size; ops++) {
                LiveOpItem opItem = (LiveOpItem) mActiveOps.get(ops);
                activeOps.add(opItem);
            }
        }
        return activeOps;
    }

	private LiveOpItem getLiveOp(List<LiveOpItem> list, int op, int uid, String packageName) {
        int size = list.size();
        for (int items = 0; items < size; items++) {
            LiveOpItem opItem = (LiveOpItem) list.get(items);
            if (opItem.getCode() == op && opItem.getUid() == uid && opItem.getPackageName().equals(packageName)) {
                return opItem;
            }
        }
        return null;
    }

    private void notifySubscribers(List<LiveOpItem> activeOps) {
		Log.d(TAG, "Notifying subscribers");
        synchronized (mCallbacks) {
            for (Callback cb : mCallbacks) {
                cb.onPrivacyChanged(activeOps);
            }
        }
    }

    @Override
    public void onOpActiveChanged(int op, int uid, String packageName, boolean active) {
        Log.d(TAG, "Operation changed for " + packageName + " to " + active);
        synchronized (mActiveOps) {
            LiveOpItem opItem = getLiveOp(mActiveOps, op, uid, packageName);
            if (opItem == null && active) {
                mActiveOps.add(new LiveOpItem(op, uid, packageName, System.currentTimeMillis()));
            } else {
                mActiveOps.remove(opItem);
            }
        }
		mHandler.post(() -> notifySubscribers(mActiveOps));
    }
}
