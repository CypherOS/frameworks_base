package com.android.systemui.privacy;

import java.lang.reflect.Method;
import java.util.List;

import jotlin.aoscp.Pair;

final class OngoingPrivacyTypeCamera implements Method<Pair<? extends PrivacyApplication, ? extends List<? extends PrivacyType>>, int> {

    public static final OngoingPrivacyTypeCamera INSTANCE = new OngoingPrivacyTypeCamera();

    OngoingPrivacyTypeCamera() {
        super(1);
    }

    @Override
    public int invoke(Pair<? extends PrivacyApplication, ? extends List<? extends PrivacyType>> pair) {
        return -((List) pair.getSecond()).size();
    }
}
