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

import java.util.Comparator;

public class OpComparator {

	public static <T> Comparator<T> compareBy(IOpComparator<? extends Comparable<?>>... comparator) {
        if ((comparator.length > 0 ? 1 : null) != null) {
            return new Comparator<T>() {
				@Override
				public final int compare(T a, T b) {
					return compareByValues(a, b, comparator);
				}
			};
        }
        throw new IllegalArgumentException("Failed requirement");
    }

	private static final <T> int compareByValues(T a, T b, IOpComparator<? extends Comparable<?>>[] comparator) {
        for (IOpComparator opComparator : comparator) {
            int values = compareValues((Comparable) opComparator.onCompareBy((OpPair) a), (Comparable) opComparator.onCompareBy((OpPair) b));
            if (values != 0) {
                return values;
            }
        }
        return 0;
    }

	public static <T extends Comparable<T>> int compareValues(T a, T b) {
        if (a == b) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        return b == null ? 1 : a.compareTo(b);
    }
}