/*
 * Copyright (c) 2017 CypherOS
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

package com.android.systemui.doze;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

public final class DozeUtils {

    private static final String TAG = "DozeUtils";
    private static final boolean DEBUG = false;

    private static final String DOZE_INTENT = "com.android.systemui.doze.pulse";

    protected static void startUserPulse(Context context) {
        if (DEBUG) Log.d(TAG, "Starting doze pulse");
        context.sendBroadcastAsUser(new Intent(DOZE_INTENT),
                new UserHandle(UserHandle.USER_CURRENT));
    }

    protected static boolean pulseOnHandWaveEnabled(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(),
                        Settings.Secure.DOZE_PULSE_ON_HAND_WAVE, 1) != 0;
    }

    protected static boolean pocketLockEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                        Settings.System.POCKET_JUDGE, 1) != 0;
    }
}