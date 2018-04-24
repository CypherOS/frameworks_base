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

package com.android.systemui.navigation;

import android.content.Context;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.os.RemoteException;
import android.provider.Settings;

/**
 * Helper class for changing Navbar theme
 */
public class NavbarThemeHelper {

    public static final String TAG = "NavbarThemeHelper";

    // Packages
    private static final String DEFAULT = "default";
    private static final String PIXEL = "co.aoscp.navbar.pixel";
    private static final String PIXEL_HOLA = "co.aoscp.navbar.pixelhola";
    private static final String SAMSUNG = "co.aoscp.navbar.samsung";
    private static final String XPERIA = "co.aoscp.navbar.xperia";
    private static final String ONEPLUS = "co.aoscp.navbar.oneplus";

    private static final String[] THEMES = {
            DEFAULT,
            PIXEL,
            PIXEL_HOLA,
            SAMSUNG,
            XPERIA,
            ONEPLUS,
    };

    public static void restoreDefault(IOverlayManager om, int userId) {
        for (int i = 1; i < THEMES.length; i++) {
            String themes = THEMES[i];
            try {
                om.setEnabled(themes, false, userId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateNavbarTheme(IOverlayManager om, int userId, int navBarTheme) throws RemoteException {
        switch (navBarTheme) {
            case 0:
                restoreDefault(om, userId);
                break;
            case 1:
                om.setEnabledExclusive(PIXEL, true, userId);
                break;
            case 2:
                om.setEnabledExclusive(PIXEL_HOLA, true, userId);
                break;
            case 3:
                om.setEnabledExclusive(SAMSUNG, true, userId);
                break;
            case 4:
                om.setEnabledExclusive(XPERIA, true, userId);
                break;
            case 5:
                om.setEnabledExclusive(ONEPLUS, true, userId);
                break;
        }
    }
}
