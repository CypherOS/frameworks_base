package com.android.systemui.privacy;

import java.util.List;

import kotlin.Pair;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;

final class OngoingPrivacyTypeMicrophone extends Lambda implements Function1<Pair<? extends PrivacyApplication, ? extends List<? extends PrivacyType>>, PrivacyType> {

    public static final OngoingPrivacyTypeMicrophone INSTANCE = new OngoingPrivacyTypeMicrophone();

    OngoingPrivacyTypeMicrophone() {
        super(1);
    }

    @Override
    public PrivacyType invoke(Pair<? extends PrivacyApplication, ? extends List<? extends PrivacyType>> pair) {
        Intrinsics.checkParameterIsNotNull(pair, "it");
        return (PrivacyType) CollectionsKt___CollectionsKt.min((Iterable) pair.getSecond());
    }
}
