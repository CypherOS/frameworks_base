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
 * limitations under the License
 */

package com.google.android.systemui.opa;

import android.view.View;
import android.view.View.OnLongClickListener;

import com.google.android.systemui.OpaLayout;

public final class HomeLongClickListener implements OnLongClickListener {

    private final OpaLayout mOpaLayout;
    private final OnLongClickListener mLongClickListener;

    public HomeLongClickListener(OpaLayout opaLayout, OnLongClickListener onLongClickListener) {
        mOpaLayout = opaLayout;
        mLongClickListener = onLongClickListener;
    }

    public final boolean onLongClick(View view) {
        return mLongClickListener.onLongClick(mOpaLayout.mHome);
    }
}
