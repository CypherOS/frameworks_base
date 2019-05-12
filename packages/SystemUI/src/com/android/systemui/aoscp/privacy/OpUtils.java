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
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class OpUtils {

	public static <T> List<T> asList(Comparable[] tArr) {
        List asList = Arrays.asList(tArr);
        return asList;
    }

	public static final <T> List<T> asListArray(T[] tArr) {
        List asList = Arrays.asList(tArr);
        return asList;
    }

    public static <T> List<T> distinctInList(Iterable<? extends T> list) {
        return toList(toMutableSet(list));
    }

	public static <T> List<T> toList(Iterable<? extends T> list) {
        if (!(list instanceof Collection)) {
			return optimizeReadOnlyList(toMutableList((Iterable) list));
        }
        List<T> emptyList;
        Collection collection = (Collection) list;
        int size = collection.size();
        if (size == 0) {
            emptyList = Collections.emptyList();
        } else {
			emptyList = toMutableList(collection);
        }
        return emptyList;
    }

	public static <K, V> List<Pair<K, V>> toListMap(Map<? extends K, ? extends V> map) {
        if (map.size() == 0) {
            return Collections.emptyList();
        }
        Iterator it = map.entrySet().iterator();
        if (!it.hasNext()) {
            return Collections.emptyList();
        }
        Entry entry = (Entry) it.next();
        if (!it.hasNext()) {
            return listOf(new Pair(entry.getKey(), entry.getValue()));
        }
        ArrayList arrayList = new ArrayList(map.size());
        arrayList.add(new Pair(entry.getKey(), entry.getValue()));
        do {
            Entry entry2 = (Entry) it.next();
            arrayList.add(new Pair(entry2.getKey(), entry2.getValue()));
        } while (it.hasNext());
        return arrayList;
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

	public static <T, C extends Collection<? super T>> C toCollection(Iterable<? extends T> list, C c) {
        for (T obj : list) {
            c.add(obj);
        }
        return c;
    }

	public static <T> List<T> optimizeReadOnlyList(List<? extends T> list) {
        int size = list.size();
        if (size == 0) {
            return Collections.emptyList();
        }
		return Collections.singletonList(list.get(0));
    }

	public static <T> boolean addAll(Collection<? super T> collection, Iterable<? extends T> list) {
        if (list instanceof Collection) {
            return collection.addAll((Collection) list);
        }
        boolean z = false;
        for (T add : list) {
            if (collection.add(add)) {
                z = true;
            }
        }
        return z;
    }

	public static final <T> List<T> listOf(T t) {
        List list = Collections.singletonList(t);
        return list;
    }
	
	public static final <T> void sortWith(List<T> list, Comparator<? super T> comparator) {
        if (list.size() > 1) {
            Collections.sort(list, comparator);
        }
    }

	public static final <T> void sortWithArray(T[] tArr, Comparator<? super T> comparator) {
        if (tArr.length > 1) {
            Arrays.sort(tArr, comparator);
        }
    }

	public static <T> List<T> sortedWith(Iterable<? extends T> list, Comparator<? super T> comparator) {
        if (list instanceof Collection) {
            Collection collection = (Collection) list;
            if (collection.size() <= 1) {
                return toList(list);
            }
            Object[] toArray = collection.toArray(new Object[0]);
            if (toArray != null) {
                sortWithArray(toArray, comparator);
                return asListArray(toArray);
            }
        }
        List toMutableList = toMutableList((Iterable) list);
        sortWith(toMutableList, comparator);
        return toMutableList;
    }

	public static <T extends Comparable<? super T>> T min(Iterable<? extends T> iterable) {
        Iterator it = iterable.iterator();
        if (!it.hasNext()) {
            return null;
        }
        Comparable comparable = (Comparable) it.next();
        while (it.hasNext()) {
            Comparable comparable2 = (Comparable) it.next();
            if (comparable.compareTo(comparable2) > 0) {
                comparable = comparable2;
            }
        }
        return comparable;
    }
}
