package com.android.systemui.privacy;

import java.lang.reflect.Method;
import java.util.List;

import jotlin.aoscp.Collections;
import jotlin.aoscp.Pair;

final class OngoingPrivacyTypeMicrophone implements Method<Pair<? extends PrivacyApplication, ? extends List<? extends PrivacyType>>, PrivacyType> {

    public static final OngoingPrivacyTypeMicrophone INSTANCE = new OngoingPrivacyTypeMicrophone();

    OngoingPrivacyTypeMicrophone() {
        super(1);
    }

    @Override
    public PrivacyType invoke(Pair<? extends PrivacyApplication, ? extends List<? extends PrivacyType>> pair) {
        Intrinsics.checkParameterIsNotNull(pair, "it");
        return (PrivacyType) Collections.min((Iterable) pair.getSecond());
    }
}
