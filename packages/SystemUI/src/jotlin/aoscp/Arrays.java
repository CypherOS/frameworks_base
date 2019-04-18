package jotlin.aoscp;

import java.util.Comparator;
import java.util.List;

class Arrays {
	
	public static <T> List<T> asList(T[] tArr) {
        return java.util.Arrays.asList(tArr);
    }

    public static <T> void sort(T[] tArr) {
        if (tArr.length > 1) {
            java.util.Arrays.sort(tArr);
        }
    }

    public static <T> void sortWith(T[] tArr, Comparator<? super T> comparator) {
        if (tArr.length > 1) {
            java.util.Arrays.sort(tArr, comparator);
        }
    }
}