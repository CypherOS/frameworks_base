/*
 * Copyright (C) 2018 CypherOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.systemui.colormanager;

import android.content.Context;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.os.RemoteException;
import android.provider.Settings;

import com.android.systemui.statusbar.NotificationLockscreenUserManager;

/**
 * Helper class for Color Manager that works as a bridge
 * to get/set aoscp overlays.
 */
public class ColorManagerHelper {

    public static final String TAG = "ColorManagerHelper";

    public static final String[] BLACK_THEME = {
            "co.aoscp.theme.black",
            "co.aoscp.theme.settings.black",
    };
    public static final String[] DARK_THEME = {
            "co.aoscp.theme.dark",
            "co.aoscp.theme.settings.dark",
    };

    // Accent Packages
    private static final String ACCENT_DEFAULT = "default";
    // Accent Packages: Blue
    private static final String ACCENT_BLUE = "co.aoscp.accent.blue";
    private static final String ACCENT_INDIGO = "co.aoscp.accent.indigo";
    private static final String ACCENT_OCEANIC = "co.aoscp.accent.oceanic";
    private static final String ACCENT_BRIGHTSKY = "co.aoscp.accent.brightsky";
    // Accent Packages: Green
    private static final String ACCENT_GREEN = "co.aoscp.accent.green";
    private static final String ACCENT_LIMA_BEAN = "co.aoscp.accent.limabean";
    private static final String ACCENT_LIME = "co.aoscp.accent.lime";
    private static final String ACCENT_TEAL = "co.aoscp.accent.teal";
    // Accent Packages: Pink
    private static final String ACCENT_PINK = "co.aoscp.accent.pink";
    private static final String ACCENT_PLAY_BOY = "co.aoscp.accent.playboy";
    // Accent Packages: Purple
    private static final String ACCENT_PURPLE = "co.aoscp.accent.purple";
    private static final String ACCENT_DEEP_VALLEY = "co.aoscp.accent.deepvalley";
    // Accent Packages: Red
    private static final String ACCENT_RED = "co.aoscp.accent.red";
    private static final String ACCENT_BLOODY_MARY = "co.aoscp.accent.bloodymary";
    // Accent Packages: Yellow
    private static final String ACCENT_YELLOW = "co.aoscp.accent.yellow";
    private static final String ACCENT_SUN_FLOWER = "co.aoscp.accent.sunflower";
    // Accent Packages: Other
    private static final String ACCENT_BLACK = "co.aoscp.accent.black";
    private static final String ACCENT_GREY = "co.aoscp.accent.grey";
    private static final String ACCENT_WHITE = "co.aoscp.accent.white";

    private static final String[] ACCENTS = {
            ACCENT_DEFAULT,
            ACCENT_BLUE,
            ACCENT_INDIGO,
            ACCENT_OCEANIC,
            ACCENT_BRIGHTSKY,
            ACCENT_GREEN,
            ACCENT_LIMA_BEAN,
            ACCENT_LIME,
            ACCENT_TEAL,
            ACCENT_PINK,
            ACCENT_PLAY_BOY,
            ACCENT_PURPLE,
            ACCENT_DEEP_VALLEY,
            ACCENT_RED,
            ACCENT_BLOODY_MARY,
            ACCENT_YELLOW,
            ACCENT_SUN_FLOWER,
            ACCENT_BLACK,
            ACCENT_GREY,
            ACCENT_WHITE,
    };

    public static boolean isUsingDarkTheme(IOverlayManager om, NotificationLockscreenUserManager lockUserManager) {
        OverlayInfo themeInfo = null;
        try {
            themeInfo = om.getOverlayInfo("co.aoscp.theme.dark",
                    lockUserManager.getCurrentUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return themeInfo != null && themeInfo.isEnabled();
    }

    public static boolean isUsingBlackTheme(IOverlayManager om, NotificationLockscreenUserManager lockUserManager) {
        OverlayInfo themeInfo = null;
        try {
            themeInfo = om.getOverlayInfo("co.aoscp.theme.black",
                    lockUserManager.getCurrentUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return themeInfo != null && themeInfo.isEnabled();
    }

    public static void restoreDefaultAccent(IOverlayManager om, NotificationLockscreenUserManager lockUserManager) {
        for (int i = 1; i < ACCENTS.length; i++) {
            String accents = ACCENTS[i];
            try {
                om.setEnabled(accents, false, lockUserManager.getCurrentUserId());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateAccent(IOverlayManager om, NotificationLockscreenUserManager lockUserManager, int systemAccent) throws RemoteException {
        switch (systemAccent) {
            case 0:
                restoreDefaultAccent(om, lockUserManager);
                break;
            case 1:
                om.setEnabledExclusive(ACCENT_BLUE, true, lockUserManager.getCurrentUserId());
                break;
            case 2:
                om.setEnabledExclusive(ACCENT_INDIGO, true, lockUserManager.getCurrentUserId());
                break;
            case 3:
                om.setEnabledExclusive(ACCENT_OCEANIC, true, lockUserManager.getCurrentUserId());
                break;
            case 4:
                om.setEnabledExclusive(ACCENT_BRIGHTSKY, true, lockUserManager.getCurrentUserId());
                break;
            case 5:
                om.setEnabledExclusive(ACCENT_GREEN, true, lockUserManager.getCurrentUserId());
                break;
            case 6:
                om.setEnabledExclusive(ACCENT_LIMA_BEAN, true, lockUserManager.getCurrentUserId());
                break;
            case 7:
                om.setEnabledExclusive(ACCENT_LIME, true, lockUserManager.getCurrentUserId());
                break;
            case 8:
                om.setEnabledExclusive(ACCENT_TEAL, true, lockUserManager.getCurrentUserId());
                break;
            case 9:
                om.setEnabledExclusive(ACCENT_PINK, true, lockUserManager.getCurrentUserId());
                break;
            case 10:
                om.setEnabledExclusive(ACCENT_PLAY_BOY, true, lockUserManager.getCurrentUserId());
                break;
            case 11:
                om.setEnabledExclusive(ACCENT_PURPLE, true, lockUserManager.getCurrentUserId());
                break;
            case 12:
                om.setEnabledExclusive(ACCENT_DEEP_VALLEY, true, lockUserManager.getCurrentUserId());
                break;
            case 13:
                om.setEnabledExclusive(ACCENT_RED, true, lockUserManager.getCurrentUserId());
                break;
            case 14:
                om.setEnabledExclusive(ACCENT_BLOODY_MARY, true, lockUserManager.getCurrentUserId());
                break;
            case 15:
                om.setEnabledExclusive(ACCENT_YELLOW, true, lockUserManager.getCurrentUserId());
                break;
            case 16:
                om.setEnabledExclusive(ACCENT_SUN_FLOWER, true, lockUserManager.getCurrentUserId());
                break;
            case 17:
                om.setEnabledExclusive(ACCENT_BLACK, true, lockUserManager.getCurrentUserId());
                break;
            case 18:
                om.setEnabledExclusive(ACCENT_GREY, true, lockUserManager.getCurrentUserId());
                break;
            case 19:
                om.setEnabledExclusive(ACCENT_WHITE, true, lockUserManager.getCurrentUserId());
                break;
        }
    }
}
