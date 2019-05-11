package com.android.systemui.privacy.aoscp;

import com.android.systemui.privacy.PrivacyApplication;
import com.android.systemui.privacy.PrivacyType;

import java.util.List;

import kotlin.Pair;

public interface IComparisons<R> {
    R invoke(Pair<? extends PrivacyApplication, ? extends List<? extends PrivacyType>> pair);
}
