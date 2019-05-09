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

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.util.Log;

import com.android.systemui.Dependency;
import com.android.systemui.ForegroundServiceController;
import com.android.systemui.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class PrivacyChipController {

    public static final String TAG = "PrivacyChipController";

    private static PrivacyChipController sController;
    private Context mContext;
    private final Handler mHandler;
	private final List<AppOpItem> mActiveOps = new ArrayList();
	private final List<IPrivacyChip> mListeners = new ArrayList();
    protected final ForegroundServiceController mForegroundServiceController =
            Dependency.get(ForegroundServiceController.class);

    public static PrivacyChipController get(Context context) {
        Assert.isMainThread();
        if (sController == null) {
            sController = new PrivacyChipController(context);
        }
        return sController;
    }

    private PrivacyChipController(Context context) {
        mContext = context;
        mHandler = new Handler();
    }

	private AppOpItem getAppOpItem(List<AppOpItem> list, int code, int uid, String packageName) {
        int size = list.size();
        for (int items = 0; items < size; items++) {
            AppOpItem item = (AppOpItem) list.get(items);
            if (item.getCode() == code && item.getUid() == uid && item.getPackageName().equals(packageName)) {
                return item;
            }
        }
        return null;
    }

	private boolean isAvailableOp(int code, int uid, String packageName, boolean active) {
        synchronized (mActiveOps) {
            AppOpItem item = getAppOpItem(mActiveOps, code, uid, packageName);
            if (item == null && active) {
                mActiveOps.add(new AppOpItem(code, uid, packageName, System.currentTimeMillis()));
                return true;
            } else if (item == null || active) {
                return false;
            } else {
                mActiveOps.remove(item);
                return true;
            }
        }
    }

    public void updateChipForAppOp(int code, int uid, String packageName, boolean active) {
        String foregroundKey = mForegroundServiceController.getStandardLayoutKey(
                UserHandle.getUserId(uid), packageName);
        if (foregroundKey != null) {
            if (isAvailableOp(code, uid, packageName, active)) {
				for (IPrivacyChip listeners : mListeners) {
                    listeners.onPrivacyChipChanged(code, uid, packageName, active);
                }
            }
        }
    }

    public void addCallback(IPrivacyChip listener) {
        mListeners.add(listener);
    }

	public void removeCallback(IPrivacyChip listener) {
        mListeners.remove(listener);
    }
}
