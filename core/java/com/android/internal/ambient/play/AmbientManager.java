/*
 * Copyright (C) 2018 CypherOS
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses>.
 */

package com.android.internal.ambient.play;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class AmbientManager {

    private Context mContext;
    private List<AmbientIndicationManagerCallback> mCallbacks;

    public AmbientManager(Context context) {
        mContext = context;
        mCallbacks = new ArrayList<>();
    }

    public void initResultCallback(DataObserver observed) {
        for (AmbientIndicationManagerCallback cb : mCallbacks) {
            try {
                cb.onRecognitionResult(observed);
            } catch (Exception ignored) {
            }
        }
    }

    public void initNoResultCallback() {
        for (AmbientIndicationManagerCallback cb : mCallbacks) {
            try {
                cb.onRecognitionNoResult();
            } catch (Exception ignored) {
            }
        }
    }

    public void initErrorCallback() {
        for (AmbientIndicationManagerCallback cb : mCallbacks) {
            try {
                cb.onRecognitionError();
            } catch (Exception ignored) {
            }
        }
    }

    public void initSettingsCallback(String key, boolean newValue) {
        for (AmbientIndicationManagerCallback cb : mCallbacks) {
            try {
                cb.onSettingsChanged(key, newValue);
            } catch (Exception ignored) {
            }
        }
    }
}
