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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class OpUtilArrayMapper {

	public static final <T> List<T> asList(T[] tArr) {
        List asList = Arrays.asList(tArr);
        return asList;
    }

    public static final <T> void sortWith(T[] tArr, Comparator<? super T> comparator) {
        if (tArr.length > 1) {
            Arrays.sort(tArr, comparator);
        }
    }
}