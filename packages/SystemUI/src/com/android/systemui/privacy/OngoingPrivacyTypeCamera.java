package com.android.systemui.privacy;

import java.util.List;
import kotlin.Pair;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;

final class OngoingPrivacyTypeCamera extends Lambda implements Function1<Pair<? extends PrivacyApplication, ? extends List<? extends PrivacyType>>, Integer> {

    public static final OngoingPrivacyTypeCamera INSTANCE = new OngoingPrivacyTypeCamera();

    OngoingPrivacyTypeCamera() {
        super(1);
    }

    @Override
    public int invoke(Pair<? extends PrivacyApplication, ? extends List<? extends PrivacyType>> pair) {
		Intrinsics.checkParameterIsNotNull(pair, "it");
        return -((List) pair.getSecond()).size();
    }
}
