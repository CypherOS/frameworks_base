package jotlin.aoscp;

import java.lang.reflect.Method;
import java.util.Comparator;

import jotlin.aoscp.comparisons.ObjectComparator;

public class Comparisons {

    private static final <T> int compareValuesByImpl(T o1, T o2, Function<? super T, ? extends Comparable<?>>[] objs) {
        for (Function obj : objs) {
            int compareValues = compareValues((Comparable) obj.invoke(o1), (Comparable) obj.invoke(o2));
            if (compareValues != 0) {
                return compareValues;
            }
        }
        return 0;
    }

    public static <T extends Comparable<?>> int compareValues(T o1, T o2) {
        if (o1 == o2) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        }
        return o2 == null ? 1 : o1.compareTo(o2);
    }

    public static <T> Comparator<T> compareBy(Function<? super T, ? extends Comparable<?>>... objs) {
        if ((objs.length > 0 ? 1 : null) != null) {
            return new ObjectComparator(objs);
        }
        throw new IllegalArgumentException("Failed requirement.");
    }
}
