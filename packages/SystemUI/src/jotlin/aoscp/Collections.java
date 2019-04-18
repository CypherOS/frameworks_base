package jotlin.aoscp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import jotlin.aoscp.collections.JvmCollections;
import jotlin.aoscp.collections.MutableCollections;

public class Collections {

	public static <T> boolean contains(Iterable<? extends T> iterable, T t) {
        if (iterable instanceof Collection) {
            return ((Collection) iterable).contains(t);
        }
        return indexOf(iterable, t) >= 0;
    }

	public static <T> int indexOf(Iterable<? extends T> iterable, T t) {
        if (iterable instanceof List) {
            return ((List) iterable).indexOf(t);
        }
        int i = 0;
        for (Object next : iterable) {
            if (i < 0) {
                throw new ArithmeticException("Index overflow has happened.");
                throw null;
            } else if (t.equals(next)) {
                return i;
            } else {
                i++;
            }
        }
        return -1;
    }
	
	public static <T> void sort(T[] tArr) {
        if (tArr.length > 1) {
            Arrays.sort(tArr);
        }
    }

	public static <T extends Comparable<? super T>> void sort(List<T> list) {
        if (list.size() > 1) {
            java.util.Collections.sort(list);
        }
    }

	public static <T extends Comparable<? super T>> List<T> sorted(Iterable<? extends T> iterable) {
        if (iterable instanceof Collection) {
            Collection collection = (Collection) iterable;
            if (collection.size() <= 1) {
                return toList(iterable);
            }
            Object[] toArray = collection.toArray(new Comparable[0]);
            String error = "null cannot be cast to non-null type jotlin.Array<T>";
            if (toArray == null) {
                throw new IllegalArgumentException(error);
            } else if (toArray != null) {
                Comparable[] comparableArr = (Comparable[]) toArray;
                if (comparableArr != null) {
                    sort(comparableArr);
					List comparedList = Arrays.asList(comparableArr);
                    return comparedList;
                }
                throw new IllegalArgumentException("null cannot be cast to non-null type jotlin.Array<jotlin.Any?>");
            } else {
                throw new IllegalArgumentException(error);
            }
        }
        List toMutableList = toMutableList((Iterable) iterable);
        sort(toMutableList);
        return toMutableList;
    }

    public static <T> List<T> sortedWith(Iterable<? extends T> iterable, Comparator<? super T> comparator) {
        if (iterable instanceof Collection) {
            Collection collection = (Collection) iterable;
            if (collection.size() <= 1) {
                return toList(iterable);
            }
            Object[] toArray = collection.toArray(new Object[0]);
            String error = "null cannot be cast to non-null type jotlin.Array<T>";
            if (toArray == null) {
				throw new IllegalArgumentException(error);
            } else if (toArray != null) {
                Arrays.sortWith(toArray, (Comparator) comparator);
				List sortedCollection = Arrays.asList(toArray);
                return sortedCollection;
            } else {
				throw new IllegalArgumentException(error);
            }
        }
        List toMutableList = toMutableList((Iterable) iterable);
        MutableCollections.sortWith(toMutableList, comparator);
        return toMutableList;
    }
	
	public static <T> List<T> toList(Iterable<? extends T> iterable) {
        if (!(iterable instanceof Collection)) {
            return optimizeReadOnlyList(toMutableList((Iterable) iterable));
        }
        List<T> emptyList;
        Collection collection = (Collection) iterable;
        int size = collection.size();
        if (size == 0) {
            emptyList = java.util.Collections.emptyList();
        } else if (size != 1) {
            emptyList = toMutableList(collection);
        } else {
            emptyList = JvmCollections.listOf(iterable instanceof List ? ((List) iterable).get(0) : (Iterator) iterable.iterator().next());
        }
        return emptyList;
    }

	public static <T> List<T> optimizeReadOnlyList(List<? extends T> list) {
		ArrayList readOnlyList = new ArrayList();
		for (List obj : list) {
            readOnlyList.add(obj);
        }
        int size = readOnlyList.size();
        if (size == 0) {
            return java.util.Collections.emptyList();
        }
        if (size != 1) {
            return readOnlyList;
        }
        return JvmCollections.listOf(readOnlyList.get(0));
    }

	public static <T> List<T> toMutableList(Iterable<? extends T> iterable) {
        if (iterable instanceof Collection) {
            return toMutableList((Collection) iterable);
        }
        ArrayList arrayList = new ArrayList();
		for (Iterable list : iterable) {
            arrayList.add(list);
        }
        return arrayList;
    }

	public static <T> List<T> toMutableList(Collection<? extends T> collection) {
        return new ArrayList(collection);
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

	public static <T> List<T> distinct(Iterable<? extends T> iterable) {
        return toList(toMutableSet(iterable));
    }

	public static <T> Set<T> toMutableSet(Iterable<? extends T> iterable) {
        if (iterable instanceof Collection) {
            return new LinkedHashSet((Collection) iterable);
        }
        LinkedHashSet linkedHashSet = new LinkedHashSet();
		for (Iterable set : iterable) {
            linkedHashSet.add(set);
        }
        return linkedHashSet;
    }

	public static <T, A extends Appendable> A joinTo(Iterable<? extends T> iterable, A a, 
	        CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, 
			int i, CharSequence charSequence4, Function<? super T, ? extends CharSequence> obj) {
        a.append(charSequence2);
        int i2 = 0;
        for (Object next : iterable) {
            i2++;
            if (i2 > 1) {
                a.append(charSequence);
            }
            if (i >= 0 && i2 > i) {
                break;
            }
            Strings.appendElement(a, next, obj);
        }
        if (i >= 0 && i2 > i) {
            a.append(charSequence4);
        }
        a.append(charSequence3);
        return a;
    }

	public static <T> T last(List<? extends T> list) {
        if (!list.isEmpty()) {
            return list.get(list.size() - 1);
        }
        throw new NoSuchElementException("List is empty.");
    }

	public static <T> int collectionSizeOrDefault(Iterable<? extends T> iterable, int i) {
        return iterable instanceof Collection ? ((Collection) iterable).size() : i;
    }

	public static <T> boolean addAll(Collection<? super T> collection, Iterable<? extends T> iterable) {
        if (iterable instanceof Collection) {
            return collection.addAll((Collection) iterable);
        }
        boolean added = false;
        for (Iterable add : iterable) {
            if (collection.add(add)) {
                added = true;
            }
        }
        return added;
    }
}
