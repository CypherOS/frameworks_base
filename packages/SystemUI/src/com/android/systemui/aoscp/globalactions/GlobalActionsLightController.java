/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.android.systemui.aoscp.globalactions;

import android.annotation.IntegerRes;
import android.content.ComponentName;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.Handler;
import android.os.VibrationEffect;
import android.util.SparseArray;

import com.android.systemui.aoscp.globalactions.GlobalActionsLightController.Callbacks;
import com.android.systemui.aoscp.globalactions.GlobalActionsLightController.State;
import com.android.systemui.plugins.annotations.DependsOn;
import com.android.systemui.plugins.annotations.ProvidesInterface;

@ProvidesInterface(version = GlobalActionsLightController.VERSION)
@DependsOn(target = State.class)
@DependsOn(target = Callbacks.class)
public interface GlobalActionsLightController {
    int VERSION = 1;

    void notifyVisible(boolean visible);

    void addCallback(Callbacks callbacks, Handler handler);
    void removeCallback(Callbacks callbacks);

    void userActivity();

    @ProvidesInterface(version = Callbacks.VERSION)
    public interface Callbacks {
        int VERSION = 1;

        void onShowRequested();
        void onDismissRequested();
        void onLayoutDirectionChanged(int layoutDirection);
        void onConfigurationChanged();
        void onScreenOff();
    }
}
