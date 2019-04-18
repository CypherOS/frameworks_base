package jotlin.aoscp.comparisons;

import java.lang.reflect.Method;
import java.util.Comparator;

import jotlin.aoscp.Comparisons;

final class ObjectComparator<T> implements Comparator<T> {

    private Method[] objs;

    ObjectComparator(Method[] objs) {
        this.objs = objs;
    }

    @Override
    public int compare(T o1, T o2) {
        return Comparisons.compareValuesByImpl(o1, o2, this.objs);
    }
}
