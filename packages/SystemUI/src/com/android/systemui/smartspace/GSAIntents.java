package com.android.systemui.smartspace;

import android.content.IntentFilter;

public class GSAIntents {

    public static IntentFilter getGsaPackageFilter(String... actions) {
        return getPackageFilter("com.google.android.googlequicksearchbox", actions);
    }

    public static IntentFilter getPackageFilter(String pkg, String... actions) {
        IntentFilter packageFilter = new IntentFilter();
        for (String action : actions) {
            packageFilter.addAction(action);
        }
        packageFilter.addDataScheme("package");
        packageFilter.addDataSchemeSpecificPart(pkg, 0);
        return packageFilter;
    }
}
