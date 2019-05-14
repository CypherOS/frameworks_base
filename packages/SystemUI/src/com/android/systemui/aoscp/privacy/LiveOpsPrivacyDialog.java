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
import android.app.AppOpsManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.IconDrawableFactory;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.settingslib.Utils;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.aoscp.micode.LiveOp;
import com.android.systemui.plugins.ActivityStarter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class LiveOpsPrivacyDialog {

    private final int MAX_ITEMS = 5;
    private Context mContext;
    private IconDrawableFactory mIconFactory;
    private int mIconMargin;
    private int mIconSize;
	private int mPlusColor;
    private int mPlusSize;

	private List<LiveOp> mActiveOps;
	private Set<String> mActivePackages;

    public LiveOpsPrivacyDialog(Context context, List<LiveOp> activeOps, Set<String> activePkgs) {
        mContext = context;
		mActiveOps = activeOps;
		mActivePackages = activePkgs;
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
        builder.setNegativeButton(R.string.ongoing_privacy_dialog_view_details, 
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
        AlertDialog d = builder.create();
        return d;
    }

    public View getContentView() {
        View d = LayoutInflater.from(mContext).inflate(R.layout.ongoing_privacy_dialog_content, null);
        TextView title = (TextView) d.findViewById(R.id.title);
        if (title != null) {
            LinearLayout container = d.findViewById(R.id.items_container);
            if (container != null) {
                title.setText(getDialogTitle());
				int multiItems = mActivePackages.size() - 1;
				if (multiItems >= 0) {
					for (int item = 0; item < MAX_ITEMS; item++) {
						boolean hasMultiOps = mActiveOps.size() > 1;
                        addAppItem(container, (LiveOp) mActiveOps.get(item), hasMultiOps);
                        if (item == multiItems) {
                            break;
                        }
                    }
				}
				if (mActivePackages.size() > MAX_ITEMS) {
                    container = d.findViewById(R.id.overflow);
                    if (container != null) {
                        container.setVisibility(View.VISIBLE);
                        TextView appName = (TextView) container.findViewById(R.id.app_name);
                        if (appName != null) {
                            appName.setText(mContext.getResources().getQuantityString(
							        R.plurals.ongoing_privacy_dialog_overflow_text, mActivePackages.size() - MAX_ITEMS, 
									new Object[]{Integer.valueOf(mActivePackages.size() - MAX_ITEMS)}));
                            ImageView appIcon = (ImageView) container.findViewById(R.id.app_icon);
                            if (appIcon != null) {
                                LayoutParams params = appIcon.getLayoutParams();
                                params.height = mPlusSize;
                                params.width = mPlusSize;
                                appIcon.setLayoutParams(params);
                                Drawable plus = appIcon.getContext().getDrawable(R.drawable.plus);
                                appIcon.setImageTintList(ColorStateList.valueOf(mPlusColor));
                                appIcon.setImageDrawable(plus);
                            }
                        }
                    }
                }
                return d;
            }
			return null;
        }
		return null;
    }

	private void addAppItem(LinearLayout container, LiveOp liveOps, boolean hasMultiOps) {
        View dialogItem = LayoutInflater.from(mContext).inflate(R.layout.ongoing_privacy_dialog_item, container, false);
		ImageView appIcon = (ImageView) dialogItem.findViewById(R.id.app_icon);
        if (appIcon != null) {
			TextView appName = (TextView) dialogItem.findViewById(R.id.app_name);
            if (appName != null) {
				LinearLayout icons = (LinearLayout) dialogItem.findViewById(R.id.icons);
                if (icons != null) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mIconSize, mIconSize);
                    params.gravity = Gravity.CENTER_VERTICAL;
                    params.setMarginStart(mIconMargin);
                    appIcon.setImageDrawable(mIconFactory.getShadowedIcon(liveOps.getAppIcon(mContext)));
                    appName.setText(liveOps.getApplicationName(mContext));
                    if (hasMultiOps) {
                        for (Object iconsForApp : generateIconsForApp()) {
                            if (mIconSize >= 0) {
                                Drawable icon = (Drawable) iconsForApp;
                                icon.setBounds(0, 0, mIconSize, mIconSize);
                                ImageView iconView = new ImageView(mContext);
                                iconView.setImageDrawable(icon);
                                icons.addView(iconView, params);
                            }
                        }
                        icons.setVisibility(View.VISIBLE);
                    } else {
                        icons.setVisibility(View.GONE);
                    }
                    try {
                        mContext.getPackageManager().getPackageInfo(liveOps.getPackageName(), 0);
                        dialogItem.setOnClickListener(
						        new View.OnClickListener() {
									@Override
									public void onClick(View view) {
										((ActivityStarter) Dependency.get(ActivityStarter.class))
										        .postStartActivityDismissingKeyguard(
												new Intent("android.intent.action.REVIEW_APP_PERMISSION_USAGE")
												.putExtra("android.intent.extra.PACKAGE_NAME", liveOps.getPackageName())
												.putExtra("android.intent.extra.USER", UserHandle.getUserHandleForUid(liveOps.getUid())), 0);
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

	private <T, A extends Appendable> A joinTo(Iterable<? extends T> iterable, A a, String s) {
		int size = 0;
		for (Object obj : iterable) {
            size++;
            if (size > 1) {
				try {
					a.append(s);
				} catch(IOException e) {
				}
            }
			if (obj != null) {
				try {
					a.append(String.valueOf(obj));
				} catch(IOException e) {
				}
			}
        }
        return a;
	}

	private <T> StringBuilder joinWithAnd(List<? extends T> list) {
        List subList = list.subList(0, list.size() - 1);
        Appendable sbAppend = new StringBuilder();
        joinTo(subList, sbAppend, ", ");
        StringBuilder sb = (StringBuilder) sbAppend;
        sb.append(" and ");
        sb.append(list.get(list.size() - 1));
        return sb;
    }

	public String joinTypes() {
        if (mActiveOps.size() == 0) {
            return "";
        }
        String sb;
        if (mActiveOps.size() != 1) {
            ArrayList opName = new ArrayList();
            for (LiveOp name : mActiveOps) {
                opName.add(name.getOpName(mContext));
            }
            sb = joinWithAnd(opName).toString();
            return sb;
        }
        sb = ((LiveOp) mActiveOps.get(0)).getOpName(mContext);
        return sb;
    }

	private String getDialogTitle() {
		String title = null;
		if (mActivePackages.size() == 1) {
            title = mContext.getString(R.string.ongoing_privacy_dialog_single_app_title, new Object[]{joinTypes()});
            return title;
        }
        title = mContext.getString(R.string.ongoing_privacy_dialog_multiple_apps_title, new Object[]{joinTypes()});
		return title;
	}

	public List<Drawable> generateIconsForApp() {
        ArrayList icons = new ArrayList();
        for (LiveOp icon : mActiveOps) {
            icons.add(icon.getIcon(mContext));
        }
        return icons;
    }
}