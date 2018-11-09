package com.android.server.wm.onehand;

import android.content.Context;
import android.database.ContentObserver;
import android.os.UserHandle;
import android.provider.Settings;

import static android.provider.Settings.System.ONEHANDED_MODE;

class OneHandedSettings {

    private static final String SETTINGS_YADJ = "com.android.onehand.yadj";
    private static final String SETTINGS_XADJ = "com.android.onehand.xadj";
    private static final String SETTINGS_SCALE = "com.android.onehand.scale";
    private static final String SETTINGS_GRAVITY = "com.android.onehand.gravity";

    final static Object sSync = new Object();

    static void saveGravity(Context ctx, int gravity) {
        Settings.System.putIntForUser(ctx.getContentResolver(),
                SETTINGS_GRAVITY, gravity, OneHandedAnimator.getCurrentUser());
    }

    static void saveScale(Context ctx, float scale) {
        Settings.System.putFloatForUser(ctx.getContentResolver(),
                SETTINGS_SCALE, scale, OneHandedAnimator.getCurrentUser());
    }

    static void saveXAdj(Context ctx, int xadj) {
        Settings.System.putIntForUser(ctx.getContentResolver(),
                SETTINGS_XADJ, xadj, OneHandedAnimator.getCurrentUser());
    }

    static void saveYAdj(Context ctx, int yadj) {
        Settings.System.putIntForUser(ctx.getContentResolver(),
                SETTINGS_YADJ, yadj, OneHandedAnimator.getCurrentUser());
    }

    static void setFeatureEnabled(Context ctx, boolean enabled, int userId) {
		final boolean permitted = isDefaultNavigation(ctx) && enabled;
        Settings.System.putIntForUser(ctx.getContentResolver(), ONEHANDED_MODE, permitted ? 1 : 0, userId);
    }

    static int getSavedGravity(Context ctx, int defaultGravity) {
        return Settings.System.getIntForUser(ctx.getContentResolver(), SETTINGS_GRAVITY, defaultGravity, OneHandedAnimator.getCurrentUser());
    }

    static float getSavedScale(Context ctx, float defaultV) {
        return Settings.System.getFloatForUser(ctx.getContentResolver(),SETTINGS_SCALE, defaultV, OneHandedAnimator.getCurrentUser());
    }

    static int getSavedXAdj(Context ctx, int defaultV) {
        return Settings.System.getIntForUser(ctx.getContentResolver(),SETTINGS_XADJ, defaultV, OneHandedAnimator.getCurrentUser());
    }

    static int getSavedYAdj(Context ctx, int defaultV) {
        return Settings.System.getIntForUser(ctx.getContentResolver(),SETTINGS_YADJ, defaultV, OneHandedAnimator.getCurrentUser());
    }

    static boolean isFeatureEnabled(Context ctx) {
		final boolean permitted = isDefaultNavigation(ctx);
        return Settings.System.getIntForUser(ctx.getContentResolver(), ONEHANDED_MODE, permitted ? 1 : 0, OneHandedAnimator.getCurrentUser()) != 0;
    }

	static boolean isDefaultNavigation(Context ctx) {
        return Settings.Secure.getInt(ctx.getContentResolver(), Settings.Secure.SWIPE_UP_TO_SWITCH_APPS_ENABLED, 0) == 0;
    }

    static void registerFeatureEnableDisableObserver(Context ctx,
                            ContentObserver observer) {
        ctx.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(ONEHANDED_MODE),
                true,
                observer, UserHandle.USER_ALL);
    }
}
