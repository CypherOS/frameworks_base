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

package com.android.settingslib.aoscp;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.settingslib.aoscp.SnackShackDialogView;

/**
 * Helper to manage user choices for OnTheSpot settings.
 */
public final class SnackShackDialogHelper {

    /** Whether to output debugging information to logs. */
    public static final boolean DEBUG = false;
    /** Log output tag. */
    public static final String LOG_TAG = "SnackShackDialog";

    /** Awaits for user input. Default value, when no user choice is known. */
    private static final int INFO = 0;

    /** No-op. Should not be used. */
    private SnackShackDialogHelper() {
    }
	
	/**
     * Gets a setting value. Internal convenience wrapper.
     *
     * @param resolver  {@link ContentResolver} for reading the setting
     */
    private static int getSetting(final ContentResolver resolver) {
		//Todo: Find a better way to return
        return INFO;
    }

    /**
     * Gets whether the user wants to allow the specific action. The callback may be
     * called twice - first with the default action and for a second time with the
     * user choice, but it implementations should not expect a set time of calls to
     * the callback.
     *
     * @param snackshack  {@link SnackShackDialogView} being shown to the user
     *                      false if it should be denied by default
	 * @param title  {@link String} title that objects the message
     * @param message  {@link String} message to display to the user
     * @param listener  {@link OnSettingChoiceListener} to notify about the choice
     * @param handler  {@link Handler} to notify the listener on,
     *                 or null to notify it on the UI thread instead
     */
    public static void prompt(final SnackShackDialogView snackshack,
            final String title, final String message,
            final OnSettingChoiceListener listener, Handler handler) {
        if (snackshack == null) {
            throw new IllegalArgumentException("snackshack == null");
        }
		if (title == null) {
            throw new IllegalArgumentException("title == null");
        }
        if (message == null) {
            throw new IllegalArgumentException("message == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener == null");
        }
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }

        final ContentResolver resolver = snackshack.getContext().getContentResolver();

        switch (getSetting(resolver)) {
            case INFO:
                if (DEBUG) Log.d(LOG_TAG, "User read info");
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        listener.onSettingInfo();
                    }

                });
                break;
            default:
                if (DEBUG) Log.d(LOG_TAG, "User read info");

                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        listener.onSettingInfo();
                    }

                });

                snackshack.show(title, message, new OnSettingChoiceListener() {

                    @Override
                    public void onSettingInfo() {
                        if (DEBUG) Log.d(LOG_TAG, "Confirming user read info");
                        listener.onSettingInfo();
                    }

                }, handler);
                break;
        }
    }

    /**
     * User choice callback listener.
     */
    public interface OnSettingChoiceListener {
        /**
          * Handles confirmation for the requested action by the user.
          *
          */
        void onSettingInfo();
    }

}