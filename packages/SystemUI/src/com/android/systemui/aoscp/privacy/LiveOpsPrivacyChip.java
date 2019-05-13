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

	private List<LiveOpItem> mActiveOps = new ArrayList();

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

    public void setChip(List<LiveOpItem> activeOps) {
		mActiveOps = activeOps;
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
        if (mActiveOps.size() == 0) {
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
                mText.setVisibility(mActiveOps.size() >= 1
				        ? View.VISIBLE : View.GONE);
				mText.setText(getContext().getResources().getQuantityString(
						R.plurals.ongoing_privacy_chip_multiple_apps, mActiveOps.size(), 
						new Object[]{Integer.valueOf(mActiveOps.size())}));
            }
        }
        requestLayout();
    }
	
	public void updateChipIcon(ViewGroup container) {
        container.removeAllViews();
		Drawable icon = null;
        for (Drawable drawable : generateIcons()) {
            drawable.mutate();
            drawable.setTint(getIconColor());
            ImageView opIcon = new ImageView(getContext());
            opIcon.setImageDrawable(drawable);
            opIcon.setScaleType(ScaleType.CENTER_INSIDE);
            container.addView(opIcon, mIconSize, mIconSize);
            ViewGroup.LayoutParams params = opIcon.getLayoutParams();
            if (params != null) {
                MarginLayoutParams marginParams = (MarginLayoutParams) params;
                marginParams.setMarginStart(mIconMargin);
                opIcon.setLayoutParams(marginParams);
            }
        }
    }

	public List<Drawable> generateIcons() {
		ArrayList icons = new ArrayList();
		for (LiveOpItem item : mActiveOps) {
			icons.add(item.getIcon(getContext()));
        }
		return icons;
    }
}
