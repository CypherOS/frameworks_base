/*
 * Copyright 2016 ParanoidAndroid Project
 * Copyright 2017 CypherOS
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

package com.android.systemui.aoscp.mini;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;

import com.android.systemui.R;

/**
 * SnackShack view creator for use with OnTheSpot & SnackShackMini.
 */
public class SnackShackMiniViewCreator {

    private Context mContext;
    private SnackShackMiniView mSnackshackView;
    private WindowManager mWindowManager;

    public SnackShackMiniViewCreator(Context context) {
        mContext = context;
        mWindowManager = (WindowManager) mContext.getSystemService(
                Context.WINDOW_SERVICE);
    }

    private void initSnackShack() {
        mSnackshackView = (SnackShackMiniView) View.inflate(mContext,
                R.layout.snack_shack_mini, null);
        if (mSnackshackView != null) attachSnackShack();
    }

    private void attachSnackShack() {
        if (mSnackshackView != null) {
            final WindowManager.LayoutParams snackshackLp = new WindowManager.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_NAVIGATION_BAR_PANEL,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            snackshackLp.gravity = Gravity.BOTTOM;
            mWindowManager.addView(mSnackshackView, snackshackLp);
        }
    }

    public SnackShackMiniView getSnackshackView() {
        initSnackShack();
        return mSnackshackView;
    }
}