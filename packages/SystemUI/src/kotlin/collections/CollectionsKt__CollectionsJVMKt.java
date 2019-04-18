package kotlin.collections;

import java.util.Collections;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: CollectionsJVM.kt */
class CollectionsKt__CollectionsJVMKt {
    public static <T> List<T> listOf(T t) {
        List singletonList = Collections.singletonList(t);
        Intrinsics.checkExpressionValueIsNotNull(singletonList, "java.util.Collections.singletonList(element)");
        return singletonList;
    }
}
