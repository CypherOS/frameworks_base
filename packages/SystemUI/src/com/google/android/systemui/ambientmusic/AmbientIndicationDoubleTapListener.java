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

import com.android.systemui.statusbar.phone.DoubleTapHelper;

public class AmbientIndicationDoubleTapListener implements DoubleTapHelper.DoubleTapListener {
	
    private Object mContainer;

    private boolean isDoubleTap() {
        return ((AmbientIndicationContainer)mContainer).getDoubleTap();
    }

    public AmbientIndicationDoubleTapListener(Object object) {
        mContainer = object;
    }

    @Override
    public boolean onDoubleTap() {
        return isDoubleTap();
    }
}

