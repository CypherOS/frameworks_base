package com.android.systemui.privacy.aoscp;

import com.android.systemui.privacy.PrivacyApplication;
import com.android.systemui.privacy.PrivacyType;

import java.util.Comparator;

import kotlin.Pair;
import kotlin.jvm.internal.Intrinsics;

public class Comparisons {

	public static <T> Comparator<T> compareBy(IComparisons<? extends Comparable<?>>... comparisons) {
		Intrinsics.checkParameterIsNotNull(comparisons, "selectors");
		if (comparisons.length > 0) {
			return new Comparator<T>() {
				@Override
				public final int compare(T a, T b) {
					return compareByValues(a, b, comparisons);
				}
			};
		}
		throw new IllegalArgumentException("Failed requirement.");
	}

	private static final <T> int compareByValues(T a, T b, IComparisons<? extends Comparable<?>>[] comparisons) {
        for (IComparisons comparables : comparisons) {
            int values = compareValues((Comparable) comparables.invoke((Pair) a), (Comparable) comparables.invoke((Pair) b));
            if (values != 0) {
                return values;
            }
        }
        return 0;
    }

	public static <T extends Comparable<T>> int compareValues(T a, T b) {
        if (a == b) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        return b == null ? 1 : a.compareTo(b);
    }
}
