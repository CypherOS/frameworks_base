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
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.aoscp.LiveOpsController;
import com.android.systemui.aoscp.privacy.LiveOp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LiveOpsPrivacyChip extends LinearLayout implements LiveOpsController.Callback {

    private int mIconColor;
    private int mIconMargin;
    private int mIconSize;
    private LinearLayout mIconsContainer;
    private TextView mText;

    private LiveOpsController mLiveOpsController;

    private List<LiveOp> mActiveOps;
    private Set<String> mActivePackages;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public LiveOpsPrivacyChip(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mIconMargin = context.getResources().getDimensionPixelSize(R.dimen.ongoing_appops_chip_icon_margin);
        mIconSize = context.getResources().getDimensionPixelSize(R.dimen.ongoing_appops_chip_icon_size);
        mIconColor = context.getResources().getColor(R.color.status_bar_clock_color, context.getTheme());
        mLiveOpsController = Dependency.get(LiveOpsController.class);
    }

    public int getIconColor() {
        return mIconColor;
    }

    public List<LiveOp> getActiveOps() {
        return mActiveOps;
    }

    public Set<String> getActivePackages() {
        return mActivePackages;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mText = (TextView) findViewById(R.id.text_container);
        mIconsContainer = (LinearLayout) findViewById(R.id.icons_container);
        mLiveOpsController.addCallback(this);
    }

    @Override
    public void onPrivacyChanged(List<LiveOp> activeOps, Set<String> activePkgs) {
        mActiveOps = activeOps;
        mActivePackages = activePkgs;
        updateView(activeOps, activePkgs);
        setVisibility(activePkgs.size() != 0 ? View.VISIBLE : View.GONE);
    }

    private void updateView(List<LiveOp> activeOps, Set<String> activePkgs) {
        if (activeOps.size() == 0) {
            if (mText != null) {
                mText.setVisibility(View.GONE);
                if (mIconsContainer != null) {
                    mIconsContainer.removeAllViews();
                }
            }
        }
        if (mIconsContainer != null) {
            updateChipIcon(mIconsContainer, activeOps);
            if (mText != null) {
                mText.setVisibility(activeOps.size() >= 1
                        ? View.VISIBLE : View.GONE);
                mText.setText(getContext().getResources().getQuantityString(
                        R.plurals.ongoing_privacy_chip_multiple_apps, activePkgs.size(), 
                        new Object[]{Integer.valueOf(activePkgs.size())}));
            }
        }
        requestLayout();
    }

    public void updateChipIcon(ViewGroup container, List<LiveOp> activeOps) {
        container.removeAllViews();
        Drawable icon = null;
        for (Drawable drawable : generateIcons(activeOps)) {
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

    public List<Drawable> generateIcons(List<LiveOp> activeOps) {
        ArrayList icons = new ArrayList();
        for (LiveOp item : activeOps) {
            icons.add(item.getIcon(getContext()));
        }
        return icons;
    }
}
