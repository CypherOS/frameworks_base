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
import android.util.Slog;

import com.android.systemui.SystemUI;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.CommandQueue.Callbacks;

public class InScreenFingerprintImpl extends SystemUI implements CommandQueue.Callbacks {
    private static final String TAG = "InScreenFingerprintImpl";

    private InScreenFingerprint mInScreenFingerprint;

    @Override
    public void start() {
        PackageManager packageManager = mContext.getPackageManager();
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) ||
                !packageManager.hasSystemFeature(aoscp.content.Context.Features.INSCREEN_FINGERPRINT)) {
            return;
        }
        getComponent(CommandQueue.class).addCallbacks(this);
        try {
            mInScreenFingerprint = new InScreenFingerprint(mContext);
        } catch (RuntimeException e) {
            Slog.e(TAG, "Failed to initialize InScreenFingerprint", e);
        }
    }

    @Override
    public void showInDisplayFingerprintView(boolean enrolling) {
        if (mInScreenFingerprint != null) {
            mInScreenFingerprint.show(enrolling);
        }
    }

    @Override
    public void hideInDisplayFingerprintView() {
        if (mInScreenFingerprint != null) {
            mInScreenFingerprint.hide();
        }
    }
}
