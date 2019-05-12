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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.systemui.R;

import java.util.Collections;
import java.util.List;

public final class LiveOpsPrivacyChip extends LinearLayout {

    private int mIconColor;
    private int mIconMargin;
    private int mIconSize;
    private LinearLayout mIconsContainer;
    private TextView mText;

	private PrivacyDialogBuilder mBuilder;
	private List<PrivacyItem> mPrivacyList;

    public LiveOpsPrivacyChip(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mIconMargin = context.getResources().getDimensionPixelSize(R.dimen.ongoing_appops_chip_icon_margin);
        mIconSize = context.getResources().getDimensionPixelSize(R.dimen.ongoing_appops_chip_icon_size);
        mIconColor = context.getResources().getColor(R.color.status_bar_clock_color, context.getTheme());
		mBuilder = new PrivacyDialogBuilder(context, Collections.emptyList());
		mPrivacyList = Collections.emptyList();
    }

	public PrivacyDialogBuilder getBuilder() {
        return mBuilder;
    }

    public int getIconColor() {
        return mIconColor;
    }

    public void setPrivacyList(List<PrivacyItem> privacyList) {
		mPrivacyList = privacyList;
		mBuilder = new PrivacyDialogBuilder(getContext(), privacyList);
		updateView();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mText = (TextView) findViewById(R.id.text_container);
        mIconsContainer = (LinearLayout) findViewById(R.id.icons_container);
    }

	private void updateView() {
        if (mPrivacyList.isEmpty()) {
            if (mText != null) {
                mText.setVisibility(View.GONE);
                if (mIconsContainer != null) {
                    mIconsContainer.removeAllViews();
                }
            }
			return;
        }
        if (mIconsContainer != null) {
            updateChipIcon(mBuilder, mIconsContainer);
			if (mText != null) {
                mText.setVisibility(mBuilder.getTypes().size() == 1
				        ? View.VISIBLE : View.GONE);
                if (mBuilder.getTypes().size() == 1) {
                    if (mBuilder.getApp() != null) {
                        if (mText != null) {
                            PrivacyApplication app = mBuilder.getApp();
                            if (app != null) {
								mText.setText(app.getApplicationName());
                            }
                        }
                    }
                    if (mText != null) {
                        mText.setText(getContext().getResources().getQuantityString(
						        R.plurals.ongoing_privacy_chip_multiple_apps, 
								mBuilder.getAppsAndTypes().size(), 
								new Object[]{Integer.valueOf(mBuilder.getAppsAndTypes().size())}));
                    }
                }
            }
        }
        requestLayout();
    }

    public void updateChipIcon(PrivacyDialogBuilder builder, ViewGroup container) {
        container.removeAllViews();
		if (builder.generateIcons() == null) return;
        for (Drawable drawable : builder.generateIcons()) {
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
}
