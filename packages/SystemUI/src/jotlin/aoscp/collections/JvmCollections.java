package jotlin.aoscp.collections;

import java.util.Collections;
import java.util.List;

public class JvmCollections {

    public static <T> List<T> listOf(T t) {
        List singletonList = Collections.singletonList(t);
        return singletonList;
    }
}
