package com.android.systemui.privacy;

import android.content.Context;
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

import kotlin.collections.CollectionsKt;

import java.util.List;

public final class OngoingPrivacyChip extends LinearLayout {

    private PrivacyDialogBuilder mBuilder;
    private int mIconColor;
    private int mIconMargin;
    private int mIconSize;
    private LinearLayout mIconsContainer;
    private List<PrivacyItem> mPrivacyList;
    private TextView mText;

    public OngoingPrivacyChip(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mIconMargin = context.getResources().getDimensionPixelSize(R.dimen.ongoing_appops_chip_icon_margin);
        mIconSize = context.getResources().getDimensionPixelSize(R.dimen.ongoing_appops_chip_icon_size);
        mIconColor = context.getResources().getColor(R.color.status_bar_clock_color, context.getTheme());
        mBuilder = new PrivacyDialogBuilder(context, CollectionsKt.emptyList());
        mPrivacyList = CollectionsKt.emptyList();
    }

    public int getIconColor() {
        return mIconColor;
    }

    public PrivacyDialogBuilder getBuilder() {
        return mBuilder;
    }

    public void setPrivacyList(List<PrivacyItem> list) {
        mPrivacyList = list;
        mBuilder = new PrivacyDialogBuilder(getContext(), list);
        updateView();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        mText = (TextView) findViewById(R.id.text_container);
        mIconsContainer = (LinearLayout) findViewById(R.id.icons_container);
    }

    private void updateView() {
        if (mPrivacyList.isEmpty()) {
            if (mText != null) {
                mText.setVisibility(8);
                if (mIconsContainer != null) {
                    mIconsContainer.removeAllViews();
                }
            }
        }
        generateContentDescription();
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

	public void updateChipIcon(PrivacyDialogBuilder dialogBuilder, ViewGroup container) {
        container.removeAllViews();
        for (Drawable drawable : dialogBuilder.generateIcons()) {
            drawable.mutate();
            drawable.setTint(getIconColor());
            ImageView privacyIcon = new ImageView(getContext());
            privacyIcon.setImageDrawable(drawable);
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

    private void generateContentDescription() {
        String joinTypes = mBuilder.joinTypes();
        if (mBuilder.getTypes().size() > 1) {
            setContentDescription(getContext().getString(
			        R.string.ongoing_privacy_chip_content_multiple_apps, new Object[]{joinTypes}));
        } else if (mBuilder.getApp() != null) {
            Object[] appInfo = new Object[2];
            PrivacyApplication app = mBuilder.getApp();
            appInfo[0] = app != null ? app.getApplicationName() : null;
            appInfo[1] = joinTypes;
            setContentDescription(getContext().getString(
		            R.string.ongoing_privacy_chip_content_single_app, appInfo));
        } else {
            setContentDescription(getContext().getResources().getQuantityString(
			        R.plurals.ongoing_privacy_chip_content_multiple_apps_single_op, 
					mBuilder.getAppsAndTypes().size(), new Object[]{Integer.valueOf(
					mBuilder.getAppsAndTypes().size()), joinTypes}));
        }
    }
}
