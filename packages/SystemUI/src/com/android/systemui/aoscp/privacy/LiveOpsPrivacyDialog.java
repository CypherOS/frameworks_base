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

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.IconDrawableFactory;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.plugins.ActivityStarter;

import java.util.List;
import java.util.concurrent.TimeUnit;

public final class LiveOpsPrivacyDialog {

    private final int MAX_ITEMS = 5;
	private final List<Pair<PrivacyApplication, List<PrivacyType>>> mAppsAndTypes;
    private final Context mContext;
    private final PrivacyDialogBuilder mDialogBuilder;
    private int mIconColor;
    private IconDrawableFactory mIconFactory;
    private int mIconMargin;
    private int mIconSize;
    private int mPlusColor;
    private int mPlusSize;

    public LiveOpsPrivacyDialog(Context context, PrivacyDialogBuilder builder) {
        mContext = context;
        mDialogBuilder = builder;
		mAppsAndTypes = mDialogBuilder.getAppsAndTypes();
		//mIconColor = context.getResources().getColor(com.android.internal.R.attr.textColorPrimary, context.getTheme());
		mIconFactory = IconDrawableFactory.newInstance(context, true);
		mIconMargin = context.getResources().getDimensionPixelSize(R.dimen.ongoing_appops_dialog_icon_margin);
		mIconSize = context.getResources().getDimensionPixelSize(R.dimen.ongoing_appops_dialog_icon_size);
		mPlusSize = context.getResources().getDimensionPixelSize(R.dimen.ongoing_appops_dialog_app_plus_size);

        TypedArray ta = context.getTheme().obtainStyledAttributes(new int[]{com.android.internal.R.attr.colorAccent});
        mPlusColor = ta.getColor(0, 0);
        ta.recycle();
    }

    public final Dialog createDialog() {
        Builder builder = new Builder(mContext);
        builder.setPositiveButton(R.string.ongoing_privacy_dialog_ok, null);
        builder.setNeutralButton(R.string.ongoing_privacy_dialog_open_settings, 
		        new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int resId) {
						((ActivityStarter) Dependency.get(ActivityStarter.class))
						        .postStartActivityDismissingKeyguard(
								new Intent(Settings.ACTION_ENTERPRISE_PRIVACY_SETTINGS).putExtra(
								"android.intent.extra.DURATION_MILLIS", TimeUnit.MINUTES.toMillis(1)), 0);
					}
				});
        builder.setView(getContentView());
        AlertDialog dialog = builder.create();
        return dialog;
    }

    public View getContentView() {
        View dialog = LayoutInflater.from(mContext).inflate(R.layout.ongoing_privacy_dialog_content, null);
        TextView title = (TextView) dialog.findViewById(R.id.title);
        if (title != null) {
            LinearLayout items = dialog.findViewById(R.id.items_container);
            if (items != null) {
                title.setText(mDialogBuilder.getDialogTitle());
                int multiItems = mAppsAndTypes.size() - 1;
                if (multiItems >= 0) {
                    for (int item = 0; item < MAX_ITEMS; item++) {
						boolean isMultiItems = mDialogBuilder.getTypes().size() > 1;
                        Pair pair = (Pair) mAppsAndTypes.get(item);
                        addAppItem(items, (PrivacyApplication) mDialogBuilder.getApp(), (List) mDialogBuilder.getTypes(), isMultiItems);
                        if (item == multiItems) {
                            break;
                        }
                    }
                }
                if (mAppsAndTypes.size() > MAX_ITEMS) {
                    items = dialog.findViewById(R.id.overflow);
                    if (items != null) {
                        items.setVisibility(View.VISIBLE);
                        TextView appName = (TextView) items.findViewById(R.id.app_name);
                        if (appName != null) {
                            appName.setText(mContext.getResources().getQuantityString(
							        R.plurals.ongoing_privacy_dialog_overflow_text, mAppsAndTypes.size() - MAX_ITEMS, 
									new Object[]{Integer.valueOf(mAppsAndTypes.size() - MAX_ITEMS)}));
                            ImageView appIcon = (ImageView) items.findViewById(R.id.app_icon);
                            if (appIcon != null) {
                                LayoutParams layoutParams = appIcon.getLayoutParams();
                                layoutParams.height = mPlusSize;
                                layoutParams.width = mPlusSize;
                                appIcon.setLayoutParams(layoutParams);
                                Drawable plus = appIcon.getContext().getDrawable(R.drawable.plus);
                                appIcon.setImageTintList(ColorStateList.valueOf(mPlusColor));
                                appIcon.setImageDrawable(plus);
                            }
                        }
                    }
                }
                return dialog;
            }
			return null;
        }
		return null;
    }

    private void addAppItem(LinearLayout container, PrivacyApplication privacyApp, List<? extends PrivacyType> list, boolean multiTypes) {
        View dialogItem = LayoutInflater.from(mContext).inflate(R.layout.ongoing_privacy_dialog_item, container, false);
		ImageView appIcon = (ImageView) dialogItem.findViewById(R.id.app_icon);
        if (appIcon != null) {
			TextView appName = (TextView) dialogItem.findViewById(R.id.app_name);
            if (appName != null) {
				LinearLayout icons = (LinearLayout) dialogItem.findViewById(R.id.icons);
                if (icons != null) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mIconSize, mIconSize);
                    params.gravity = 16;
                    params.setMarginStart(mIconMargin);
                    appIcon.setImageDrawable(mIconFactory.getShadowedIcon(privacyApp.getIcon()));
                    appName.setText(privacyApp.getApplicationName());
                    if (multiTypes) {
                        for (Object next : mDialogBuilder.generateIconsForApp(list)) {
                            if (mIconSize >= 0) {
                                Drawable generatedIcon = (Drawable) next;
                                generatedIcon.setBounds(0, 0, mIconSize, mIconSize);
                                ImageView newIcon = new ImageView(mContext);
                                //newIcon.setImageTintList(ColorStateList.valueOf(mIconColor));
                                newIcon.setImageDrawable(generatedIcon);
                                newIcon.setContentDescription(((PrivacyType) list.get(mIconSize)).getName(mContext));
                                icons.addView(newIcon, params);
                            }
                        }
                        icons.setVisibility(View.VISIBLE);
                    } else {
                        icons.setVisibility(View.GONE);
                    }
                    try {
                        mContext.getPackageManager().getPackageInfo(privacyApp.getPackageName(), 0);
                        dialogItem.setOnClickListener(
						        new View.OnClickListener() {
									@Override
									public void onClick(View view) {
										((ActivityStarter) Dependency.get(ActivityStarter.class))
										        .postStartActivityDismissingKeyguard(
												new Intent("android.intent.action.REVIEW_APP_PERMISSION_USAGE")
												.putExtra("android.intent.extra.PACKAGE_NAME", privacyApp.getPackageName())
												.putExtra("android.intent.extra.USER", UserHandle.getUserHandleForUid(privacyApp.getUid())), 0);
									}
								});
                    } catch (NameNotFoundException unused) {
                    }
                    container.addView(dialogItem);
                    return;
                }
            }
        }
    }
}