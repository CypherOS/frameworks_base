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

public class PrivacyTypeCoordinator {
	static final int[] ORDERED_TYPES = new int[PrivacyType.values().length];
        static {
            ORDERED_TYPES[PrivacyType.TYPE_CAMERA.ordinal()] = 1;
            ORDERED_TYPES[PrivacyType.TYPE_LOCATION.ordinal()] = 2;
            try {
                ORDERED_TYPES[PrivacyType.TYPE_MICROPHONE.ordinal()] = 3;
            } catch (NoSuchFieldError unused) {
            }
        }
    }