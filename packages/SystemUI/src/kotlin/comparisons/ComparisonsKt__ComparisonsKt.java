package kotlin.comparisons;

import java.util.Comparator;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: Comparisons.kt */
class ComparisonsKt__ComparisonsKt {
    private static final <T> int compareValuesByImpl$ComparisonsKt__ComparisonsKt(T t, T t2, Function1<? super T, ? extends Comparable<?>>[] function1Arr) {
        for (Function1 function1 : function1Arr) {
            int compareValues = compareValues((Comparable) function1.invoke(t), (Comparable) function1.invoke(t2));
            if (compareValues != 0) {
                return compareValues;
            }
        }
        return 0;
    }

    public static <T extends Comparable<?>> int compareValues(T t, T t2) {
        if (t == t2) {
            return 0;
        }
        if (t == null) {
            return -1;
        }
        return t2 == null ? 1 : t.compareTo(t2);
    }

    public static <T> Comparator<T> compareBy(Function1<? super T, ? extends Comparable<?>>... function1Arr) {
        Intrinsics.checkParameterIsNotNull(function1Arr, "selectors");
        if ((function1Arr.length > 0 ? 1 : null) != null) {
            return new ComparisonsKt__ComparisonsKt$compareBy$1(function1Arr);
        }
        throw new IllegalArgumentException("Failed requirement.");
    }
}
