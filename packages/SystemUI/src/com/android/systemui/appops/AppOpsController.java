package com.android.systemui.appops;

import java.util.List;

public interface AppOpsController {

    public interface Callback {
        void onActiveStateChanged(int code, int uid, String packageName, boolean active);
    }

    void addCallback(int[] keys, Callback callback);

    List<AppOpItem> getActiveAppOpsForUser(int uid);
}
