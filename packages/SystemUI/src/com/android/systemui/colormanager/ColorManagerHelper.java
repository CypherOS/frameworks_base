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
import android.content.cm.IColorManager;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

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
	
	// Accents
    private static final String ACCENT_DEEP_PURPLE = "co.aoscp.accent.deeppurple";
    private static final String ACCENT_INDIGO = "co.aoscp.accent.indigo";
    private static final String ACCENT_PINK = "co.aoscp.accent.pink";
    private static final String ACCENT_PURPLE = "co.aoscp.accent.purple";
    private static final String ACCENT_RED = "co.aoscp.accent.red";
    private static final String ACCENT_SKY_BLUE = "co.aoscp.accent.skyblue";
    private static final String ACCENT_TEAL = "co.aoscp.accent.teal";
    private static final String ACCENT_WHITE = "co.aoscp.accent.white";
    private static final String ACCENT_YELLOW = "co.aoscp.accent.yellow";
	
	private IColorManager mColorManager;
	
	public ColorManagerHelper() {
		mColorManager = IColorManager.Stub.asInterface(
                ServiceManager.getService(Context.COLOR_MANAGER));
	}

    public static boolean isUsingDarkTheme(IOverlayManager om, int userId) {
        OverlayInfo themeInfo = null;
        try {
            themeInfo = om.getOverlayInfo("co.aoscp.theme.dark",
                    userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return themeInfo != null && themeInfo.isEnabled();
    }

    public static boolean isUsingBlackTheme(IOverlayManager om, int userId) {
        OverlayInfo themeInfo = null;
        try {
            themeInfo = om.getOverlayInfo("co.aoscp.theme.black",
                    userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return themeInfo != null && themeInfo.isEnabled();
    }
	
	protected void updateAccent(Context context) {
        int userAccentSetting = Settings.Secure.getIntForUser(context.getContentResolver(),
                Settings.Secure.DEVICE_ACCENT, 0, UserHandle.USER_CURRENT);
		switch (userAccentSetting) {
            case 0:
                mColorManager.restoreDefaultAccent();
                break;
            case 1:
			    mColorManager.updateAccent(ACCENT_DEEP_PURPLE);
                break;
            case 2:
                mColorManager.updateAccent(ACCENT_INDIGO);
                break;
            case 3:
                mColorManager.updateAccent(ACCENT_PINK);
                break;
            case 4:
                mColorManager.updateAccent(ACCENT_PURPLE);
                break;
            case 5:
                mColorManager.updateAccent(ACCENT_RED);
                break;
            case 6:
                mColorManager.updateAccent(ACCENT_SKY_BLUE);
                break;
            case 7:
                mColorManager.updateAccent(ACCENT_TEAL);
                break;
            case 8:
                mColorManager.updateAccent(ACCENT_WHITE);
                break;
            case 9:
                mColorManager.updateAccent(ACCENT_YELLOW);
                break;
		}
	}
}
