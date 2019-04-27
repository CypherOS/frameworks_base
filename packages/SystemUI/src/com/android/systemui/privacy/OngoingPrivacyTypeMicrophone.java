package com.android.systemui.privacy;

import com.android.systemui.privacy.aoscp.IComparisons;

import java.util.Collections;
import java.util.List;

import kotlin.Pair;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;

final class OngoingPrivacyTypeMicrophone extends Lambda implements IComparisons<PrivacyType> {

    public static final OngoingPrivacyTypeMicrophone INSTANCE = new OngoingPrivacyTypeMicrophone();

    OngoingPrivacyTypeMicrophone() {
        super(1);
    }

    @Override
    public PrivacyType invoke(Pair<? extends PrivacyApplication, ? extends List<? extends PrivacyType>> pair) {
        Intrinsics.checkParameterIsNotNull(pair, "it");
        return (PrivacyType) Collections.min(pair.getSecond());
    }
}
