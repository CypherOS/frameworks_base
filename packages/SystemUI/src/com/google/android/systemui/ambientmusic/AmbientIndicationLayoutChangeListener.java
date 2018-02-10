/*
 * Copyright (C) 2017-2018 Google Inc.
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

package com.google.android.systemui.ambientmusic;

import android.view.View;

public class AmbientIndicationLayoutChangeListener implements View.OnLayoutChangeListener {
	
    private Object mContainer;

    private void updateContainerBottomPadding() {
        ((AmbientIndicationContainer)mContainer).updateAmbientIndicationBottomPadding();
    }

    public AmbientIndicationLayoutChangeListener(Object object) {
        mContainer = object;
    }

    public void onLayoutChange(View view, int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8) {
        updateContainerBottomPadding();
    }
}

