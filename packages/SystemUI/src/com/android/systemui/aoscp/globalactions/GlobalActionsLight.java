/*
 * Copyright (C) 2018 CypherOS
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

import com.android.systemui.plugins.Plugin;
import com.android.systemui.plugins.annotations.ProvidesInterface;

/**
 * This interface is really just a stub for initialization/teardown, actual handling of
 * when to show will be done through {@link VolumeDialogController}
 */
@ProvidesInterface(action = GlobalActionsLight.ACTION, version = GlobalActionsLight.VERSION)
public interface GlobalActionsLight extends Plugin {
    String ACTION = "com.android.systemui.action.PLUGIN_GAL";
    int VERSION = 1;

    void onGalDestroy();
}
