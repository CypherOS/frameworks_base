/* 
 * Copyright (C) 2018 CypherOS
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

package com.android.server;

import android.annotation.NonNull;
import android.content.Context;
import android.util.Log;

import aoscp.hardware.DisplayEngineController;

import com.android.internal.util.ConcurrentUtils;
import com.android.server.SystemServerInitThreadPool;

import java.util.concurrent.Future;

public class DeviceHardwareService extends SystemService {

    static final String TAG = "DeviceHardwareService";
    static final boolean DEBUG = true;
	
	private Future<?> mInitCompleteSignal;
	
	private DisplayEngineController mDisplayEngineController;

    public DeviceHardwareService(@NonNull final Context context) {
        super(context);
        mDisplayEngineController = new DisplayEngineController(context);
		mInitCompleteSignal = SystemServerInitThreadPool.get().submit(() -> {
			if (DEBUG) Log.d(TAG, "Loading hardware controllers");
			mDisplayEngineController.init();
		}, "DeviceHardwareService");
    }

    @Override
    public void onStart() {
		// no op
    }

    @Override
    public void onBootPhase(int phase) {
		if (phase == PHASE_SYSTEM_SERVICES_READY && mInitCompleteSignal != null) {
            ConcurrentUtils.waitForFutureNoInterrupt(mInitCompleteSignal,
                    "Wait for DeviceHardwareService init");
            mInitCompleteSignal = null;
        }
    }

    private void initHardwareControllers() {
		mDisplayEngineController.init();
	}
}
