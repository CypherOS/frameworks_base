package jotlin.aoscp;

import java.util.List;

class ArraysUtil {
    static <T> List<T> asList(T[] tArr) {
        return Arrays.asList(tArr);
    }
}