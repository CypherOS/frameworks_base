package com.android.systemui.privacy;

import com.android.systemui.privacy.aoscp.IComparisons;

import java.util.List;

import kotlin.Pair;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;

final class OngoingPrivacyTypeCamera extends Lambda implements IComparisons<Integer> {

    public static final OngoingPrivacyTypeCamera INSTANCE = new OngoingPrivacyTypeCamera();

    OngoingPrivacyTypeCamera() {
        super(1);
    }

    @Override
    public Integer invoke(Pair<? extends PrivacyApplication, ? extends List<? extends PrivacyType>> pair) {
		Intrinsics.checkParameterIsNotNull(pair, "it");
        return -((List) pair.getSecond()).size();
    }
}
