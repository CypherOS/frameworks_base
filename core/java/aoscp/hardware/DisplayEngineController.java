/*
 * Copyright (C) 2018 The LineageOS Project
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
 * limitations under the License.
 */

package aoscp.hardware;

import android.content.Context;

import aoscp.hardware.display.DisplayMode;

/*
 * Display Engine API for Display Modes
 *
 * A device may implement a list of preset display modes for different
 * viewing intents, such as movies, photos, or extra vibrance. These
 * modes may have multiple components such as gamma correction, white
 * point adjustment, etc, but are activated by a single control point.
 *
 * This API provides support for enumerating and selecting the
 * modes supported by the hardware.
 */

public class DisplayEngineController {

    /** Placeholder for available modes **/
    private static final DisplayMode[] DISPLAY_MODES = {
        new DisplayMode(0, "Default"),
    };

    private Context mContext;

    public DisplayEngineController(Context context) {
        mContext = context;
    }

    /*
     * Common initializer for all hardware controllers.
     * Some controllers may not need an initializer therefore
     * it is not required.
     */
    public void init() { }

    /*
     * Determines whether the DisplayEngineController is
     * accessible and supported by the device.
     */
    public boolean isAvailable() {
        return false;
    }

    /*
     * Get the list of available modes. A mode has an integer
     * identifier and a string name.
     *
     * It is the responsibility of the upper layers to
     * map the name to a human-readable format or perform translation.
     */
    public DisplayMode[] getAvailableModes() {
        return new DisplayMode[0];
    }

    /*
     * Get the name of the currently selected mode. This can return
     * null if no mode is selected.
     */
    public DisplayMode getCurrentMode() {
        return null;
    }

    /*
     * Selects a mode from the list of available modes by it's
     * string identifier. Returns true on success, false for
     * failure. It is up to the implementation to determine
     * if this mode is valid.
     */
    public boolean setMode(DisplayMode mode, boolean makeDefault) {
        return false;
    }

    /*
     * Gets the preferred default mode for this device by it's
     * string identifier. Can return null if there is no default.
     */
    public DisplayMode getDefaultMode() {
        return null;
    }

    /*
     * Gets the preferred mode entry for this device.
     * Some devices have specific names for their modes
     * so each device can specify their mode names here.
     */
    public String getModeEntry(int mode) {
        return null;
    }
}