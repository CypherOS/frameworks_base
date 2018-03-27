/**
 * Copyright (C) 2018 CypherOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package android.content.cm;

import android.content.Context;
import android.content.om.OverlayInfo;
import android.os.RemoteException;

public class ColorManager {

    private static final String TAG = "ColorManager";

    private Context mContext;
    private IColorManager mColorManager;

    public ColorManager(Context context, IColorManager colorMgr) {
        mContext = context;
        mColorManager = colorMgr;
    }

    /**
     * Replaces the current theme with a new supported one
     * @param themePackage the overlay that is replacing the current
     * @return tries to update the theme, if unsuccessful return false
     */
    public boolean updateTheme(String themePackage) {
        try {
            return mColorManager.updateTheme(themePackage);
        } catch (RemoteException e) {
            return false;
        }
    }
	
	/**
     * Replaces the current accent with a new supported one
     * @param accentPackage the overlay that is replacing the current
     * @return tries to update the accent, if unsuccessful return false
     */
    public boolean updateAccent(String accentPackage) {
        try {
            return mColorManager.updateAccent(accentPackage);
        } catch (RemoteException e) {
            return false;
        }
    }
}
