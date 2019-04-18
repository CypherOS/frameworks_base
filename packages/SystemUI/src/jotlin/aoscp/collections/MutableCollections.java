package jotlin.aoscp.collections;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MutableCollections {

    public static <T extends Comparable<? super T>> void sort(List<T> list) {
        if (list.size() > 1) {
            Collections.sort(list);
        }
    }

    public static <T> void sortWith(List<T> list, Comparator<? super T> comparator) {
        if (list.size() > 1) {
            Collections.sort(list, comparator);
        }
    }
}
