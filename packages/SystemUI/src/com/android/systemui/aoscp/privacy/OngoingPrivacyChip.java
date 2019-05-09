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

public final class OngoingPrivacyChip extends LinearLayout {

    private PrivacyDialogBuilder mBuilder;
    private int mIconColor;
    private int mIconMargin;
    private int mIconSize;
    private LinearLayout mIconsContainer;
    private TextView mText;

	private boolean mIsActive;
	private int mAppOpCode;
	private String mAppOpPkg;

    public OngoingPrivacyChip(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mIconMargin = context.getResources().getDimensionPixelSize(R.dimen.ongoing_appops_chip_icon_margin);
        mIconSize = context.getResources().getDimensionPixelSize(R.dimen.ongoing_appops_chip_icon_size);
        mIconColor = context.getResources().getColor(R.color.status_bar_clock_color, context.getTheme());
    }

    public int getIconColor() {
        return mIconColor;
    }

    public void setChip(int code, String packageName, boolean active) {
		mAppOpCode = code;
		mAppOpPkg = packageName;
		if (mIsActive != active) {
			mIsActive = active;
            updateView();
		}
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
		int imageRes;
		if (code == AppOpsManager.OP_COARSE_LOCATION 
		            | AppOpsManager.OP_FINE_LOCATION) {
			imageRes = getContext().getResources().getDrawable(stat_sys_location);
		} else if (code == AppOpsManager.OP_CAMERA) {
			imageRes = getContext().getResources().getDrawable(stat_sys_camera);
		} else if (code == AppOpsManager.OP_RECORD_AUDIO) {
			imageRes = getContext().getResources().getDrawable(stat_sys_mic_none);
		}
		ImageView privacyIcon = new ImageView(getContext());
		privacyIcon.setImageResource(imageRes);
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
