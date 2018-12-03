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

import android.content.Context;

/**
 * The sub class for hardware services imported
 * from aoscp hardware
 * {@hide}
 */
public abstract class HwSystemService extends SystemService {

    public HwSystemService(Context context) {
        super(context);
    }

    /**
     * Called when device hardware features are requested by a service
     * and returns to declared list of features for the given device.
     * When this method returns, the service should be published.
     */
    public abstract String getHardwareFeatures();
}
