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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

public class OpUtils {

    public static <T> List<T> distinctInList(Iterable<? extends T> list) {
        return toList(toMutableSet(list));
    }
	
	public static <T> List<T> toList(Iterable<? extends T> list) {
        if (!(list instanceof Collection)) {
            int size = list.size();
			if (size == 0) {
				return Collections.emptyList();
			}
			if (size != 1) {
				return list;
			}
			return Collections.singletonList(list.get(0));
        }
        List<T> emptyList;
        Collection collection = (Collection) list;
        int size = collection.size();
        if (size == 0) {
            emptyList = Collections.emptyList();
        } else if (size != 1) {
            emptyList = toMutableList(collection);
        } else {
			emptyList = Collections.singletonList(list instanceof List ? ((List) list).get(0) : list.iterator().next());
        }
        return emptyList;
    }
	
	public static <T> List<T> toMutableList(Iterable<? extends T> list) {
        if (list instanceof Collection) {
            return new ArrayList((Collection) list);
        }
        ArrayList newList = new ArrayList();
        toCollection(list, newList);
        return newList;
    }
	
	public static <T> Set<T> toMutableSet(Iterable<? extends T> list) {
        if (list instanceof Collection) {
            return new LinkedHashSet((Collection) list);
        }
        LinkedHashSet set = new LinkedHashSet();
        toCollection(list, set);
        return set;
    }

	private <T, C extends Collection<? super T>> C toCollection(Iterable<? extends T> list, C c) {
        for (Object obj : list) {
            c.add(obj);
        }
        return c;
    }
}
