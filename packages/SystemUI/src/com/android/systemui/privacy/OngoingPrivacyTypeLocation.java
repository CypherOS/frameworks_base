package com.android.systemui.privacy;

import java.lang.reflect.Method;
import java.util.List;

import jotlin.aoscp.Pair;

final class OngoingPrivacyTypeLocation implements Method<Pair<? extends PrivacyApplication, ? extends List<? extends PrivacyType>>, PrivacyApplication> {

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
