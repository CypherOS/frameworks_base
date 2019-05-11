/*
 * Copyright (C) 2019 CypherOS
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

package com.android.systemui.aoscp.privacy;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.systemui.R;

import java.util.List;

public final class LiveOpsPrivacyChip extends LinearLayout {

    private int mIconColor;
    private int mIconMargin;
    private int mIconSize;
    private LinearLayout mIconsContainer;
    private TextView mText;

	private boolean mIsActive;
	private int mAppOpCode;
	private String mAppOpPkg;

    public LiveOpsPrivacyChip(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mIconMargin = context.getResources().getDimensionPixelSize(R.dimen.ongoing_appops_chip_icon_margin);
        mIconSize = context.getResources().getDimensionPixelSize(R.dimen.ongoing_appops_chip_icon_size);
        mIconColor = context.getResources().getColor(R.color.status_bar_clock_color, context.getTheme());
    }

    public int getIconColor() {
        return mIconColor;
    }

    public int getOpCode() {
        return mAppOpCode;
    }

	public String getOpPackage() {
        return mAppOpPkg;
    }

    public void setChip(int activeOps, int opCode, String opPackage) {
		mAppOpCode = opCode;
		mAppOpPkg = opPackage;
        mIsActive = activeOps > 0;
		updateView();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        mText = (TextView) findViewById(R.id.text_container);
        mIconsContainer = (LinearLayout) findViewById(R.id.icons_container);
    }

    private void updateView() {
		final PackageManager pm = getContext().getPackageManager();
		ApplicationInfo appInfo;
        if (!mIsActive) {
            if (mText != null) {
                mText.setVisibility(View.GONE);
                if (mIconsContainer != null) {
                    mIconsContainer.removeAllViews();
                }
            }
        }
        if (mIconsContainer != null) {
            updateChipIcon(mIconsContainer);
            if (mText != null) {
                mText.setVisibility(mIsActive
				        ? View.VISIBLE : View.GONE);
				if (mIsActive && mAppOpPkg != null) {
					if (mText != null) {
						try {
							appInfo = pm.getApplicationInfo(mAppOpPkg, 0);
						} catch (NameNotFoundException e) {
							appInfo = null;
						}
						if (appInfo != null) {
							mText.setText(pm.getApplicationLabel(appInfo));
						}
					}
				}
            }
        }
        requestLayout();
    }

	public void updateChipIcon(ViewGroup container) {
        container.removeAllViews();
		Drawable icon = null;
		if (mAppOpCode == AppOpsManager.OP_COARSE_LOCATION 
		            | mAppOpCode == AppOpsManager.OP_FINE_LOCATION 
					| mAppOpCode == AppOpsManager.OP_MONITOR_LOCATION 
					| mAppOpCode == AppOpsManager.OP_MONITOR_HIGH_POWER_LOCATION) {
            icon = getContext().getResources().getDrawable(R.drawable.stat_sys_location);
		} else if (mAppOpCode == AppOpsManager.OP_CAMERA) {
            icon = getContext().getResources().getDrawable(R.drawable.stat_sys_camera);
		} else if (mAppOpCode == AppOpsManager.OP_RECORD_AUDIO) {
            icon = getContext().getResources().getDrawable(R.drawable.stat_sys_mic_none);
		}
		ImageView privacyIcon = new ImageView(getContext());
		privacyIcon.setImageDrawable(icon);
		privacyIcon.setScaleType(ScaleType.CENTER_INSIDE);
		container.addView(privacyIcon, mIconSize, mIconSize);
        ViewGroup.LayoutParams params = privacyIcon.getLayoutParams();
        if (params != null) {
            MarginLayoutParams marginParams = (MarginLayoutParams) params;
            marginParams.setMarginStart(mIconMargin);
            privacyIcon.setLayoutParams(marginParams);
        }
    }
}
