package com.android.systemui.privacy;

import com.android.systemui.privacy.aoscp.IComparisons;

import java.util.List;
import kotlin.Pair;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;

final class OngoingPrivacyTypeLocation extends Lambda implements IComparisons<PrivacyApplication> {

    public static final OngoingPrivacyTypeLocation INSTANCE = new OngoingPrivacyTypeLocation();

    OngoingPrivacyTypeLocation() {
        super(1);
    }

    @Override
    public PrivacyApplication invoke(Pair<? extends PrivacyApplication, ? extends List<? extends PrivacyType>> pair) {
        Intrinsics.checkParameterIsNotNull(pair, "it");
        return (PrivacyApplication) pair.getFirst();
    }
}
