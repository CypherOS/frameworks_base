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
 * limitations under the License.
 */
package com.android.settingslib.aoscp.footerlib;

/**
 * Interface used to notify of all {@link FooterConfirm} display events. Useful if you want
 * to move other views while the FooterConfirmMixin is on screen.
 */
public interface FooterEventListener {
	
    /**
     * Called when a {@link FooterConfirm} is about to enter the screen
     *
     * @param footerConfirm the {@link FooterConfirm} that's being shown
     */
    public void onShow(FooterConfirm footerConfirm);

    /**
     * Called when a {@link FooterConfirm} is about to enter the screen while
     * a {@link FooterConfirm} is about to exit the screen by replacement.
     *
     * @param footerConfirm the {@link FooterConfirm} that's being shown
     */
    public void onShowByReplace(FooterConfirm footerConfirm);

    /**
     * Called when a {@link FooterConfirm} is fully shown
     *
     * @param footerConfirm the {@link FooterConfirm} that's being shown
     */
    public void onShown(FooterConfirm footerConfirm);

    /**
     * Called when a {@link FooterConfirm} is about to exit the screen
     *
     * @param footerConfirm the {@link FooterConfirm} that's being dismissed
     */
    public void onDismiss(FooterConfirm footerConfirm);

    /**
     * Called when a {@link FooterConfirm} is about to exit the screen
     * when a new {@link FooterConfirm} is about to enter the screen.
     *
     * @param footerConfirm the {@link FooterConfirm} that's being dismissed
     */
    public void onDismissByReplace(FooterConfirm footerConfirm);

    /**
     * Called when a {@link FooterConfirm} had just been dismissed
     *
     * @param footerConfirm the {@link FooterConfirm} that's being dismissed
     */
    public void onDismissed(FooterConfirm footerConfirm);
}