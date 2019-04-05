/**
 * Copyright (C) 2019 The Android Open Source Project
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

package com.android.systemui.aoscp.fingerprint;

import android.content.pm.PackageManager;
import android.view.View;

import com.android.systemui.SystemUI;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.CommandQueue.Callbacks;

public class InScreenFingerprintImpl extends SystemUI implements CommandQueue.Callbacks {
    private static final String TAG = "InScreenFingerprintImpl";

    private InScreenFingerprint mInScreenFingerprint;

    @Override
    public void start() {
        if (mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            getComponent(CommandQueue.class).addCallbacks(this);
        }
    }

    @Override
    public void handleOnScreenFingerprintView(boolean show, boolean isEnrolling) {
        if (mInScreenFingerprint == null) {
            mInScreenFingerprint = new InScreenFingerprint(mContext);
        }

        if (!mInScreenFingerprint.viewAdded && show) {
            mInScreenFingerprint.show(isEnrolling);
        } else if (mInScreenFingerprint.viewAdded) {
            mInScreenFingerprint.hide();
        }
    }
}
