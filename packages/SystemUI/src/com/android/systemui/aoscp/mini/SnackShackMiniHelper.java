/*
 * Copyright 2016 ParanoidAndroid Project
 * Copyright 2017 CypherOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.aoscp.mini;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.systemui.aoscp.mini.SnackShackMiniView;

/**
 * Helper to manage user choices for OnTheSpot settings.
 */
public final class SnackShackMiniHelper {

    /** Whether to output debugging information to logs. */
    public static final boolean DEBUG = false;
    /** Log output tag. */
    public static final String LOG_TAG = "SnackShackMini";

    /** No-op. Should not be used. */
    private SnackShackMiniHelper() {
    }

    /**
     * Gets whether the user wants to allow the specific action. The callback may be
     * called twice - first with the default action and for a second time with the
     * user choice, but it implementations should not expect a set time of calls to
     * the callback.
     *
     * @param snackshack  {@link SnackShackMiniView} being shown to the user
     * @param message  {@link String} message to display to the user
     * @param handler  {@link Handler} to notify the listener on,
     *                 or null to notify it on the UI thread instead
     */
    public static void prompt(final SnackShackMiniView snackshack,
            final String message, Handler handler) {
        if (snackshack == null) {
            throw new IllegalArgumentException("snackshack == null");
        }
        if (message == null) {
            throw new IllegalArgumentException("message == null");
        }
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }

		if (DEBUG) Log.d(LOG_TAG, "Displaying brief information");
        snackshack.show(message, handler);

    }
}