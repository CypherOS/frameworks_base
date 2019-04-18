package jotlin.aoscp.comparisons;

import java.lang.reflect.Method;
import java.util.Comparator;

import jotlin.aoscp.Comparisons;
import jotlin.aoscp.Function;

public final class ObjectComparator<T> implements Comparator<T> {

    private Function[] obj;

    ObjectComparator(Function[] obj) {
        this.obj = obj;
    }

    @Override
    public int compare(T o1, T o2) {
        return Comparisons.compareValuesByImpl(o1, o2, this.obj);
    }
}
