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

package com.android.systemui.aoscp.privacy.utils;

import com.android.systemui.aoscp.privacy.PrivacyApplication;
import com.android.systemui.aoscp.privacy.PrivacyType;

import java.util.List;

public interface IOpComparator<R> {
    R onCompareBy(OpPair<? extends PrivacyApplication, ? extends List<? extends PrivacyType>> pair);
}