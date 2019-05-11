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

public class LiveOpsPrivacyDialog {

    private Context mContext;
    private IconDrawableFactory mIconFactory;
    private int mIconMargin;
    private int mIconSize;
	private int mAppOpCode;
	private String mAppOpPkg;

    public LiveOpsPrivacyDialog(Context context, int appOpCode, String appOpPackage) {
        mContext = context;
		mAppOpCode = appOpCode;
		mAppOpPkg = appOpPackage;

		mIconFactory = IconDrawableFactory.newInstance(context, true);
		mIconMargin = context.getResources().getDimensionPixelSize(R.dimen.ongoing_appops_dialog_icon_margin);
		mIconSize = context.getResources().getDimensionPixelSize(R.dimen.ongoing_appops_dialog_icon_size);
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
        AlertDialog dialog = builder.create();
        return dialog;
    }

    public View getContentView() {
        View d = LayoutInflater.from(mContext).inflate(R.layout.ongoing_privacy_dialog_content, null);
        TextView title = (TextView) d.findViewById(R.id.title);
        if (title != null) {
            LinearLayout container = d.findViewById(R.id.items_container);
            if (container != null) {
                title.setText(getDialogTitle());
				addCurrentOp(container);
                return d;
            }
			return null;
        }
		return null;
    }

    private void addCurrentOp(LinearLayout container) {
        View d = LayoutInflater.from(mContext).inflate(R.layout.ongoing_privacy_dialog_item, container, false);
		ImageView appIcon = (ImageView) d.findViewById(R.id.app_icon);
        if (appIcon != null) {
			TextView appName = (TextView) d.findViewById(R.id.app_name);
            if (appName != null) {
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mIconSize, mIconSize);
                params.gravity = 16;
                params.setMarginStart(mIconMargin);
                appIcon.setImageDrawable(mIconFactory.getShadowedIcon(getApplicationIcon()));
                appName.setText(getApplicationName());
				try {
					mContext.getPackageManager().getPackageInfo(mAppOpPkg, 0);
					d.setOnClickListener(
					        new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									((ActivityStarter) Dependency.get(ActivityStarter.class)).postStartActivityDismissingKeyguard(
									        new Intent("android.intent.action.REVIEW_APP_PERMISSION_USAGE")
											.putExtra("android.intent.extra.PACKAGE_NAME", mAppOpPkg)
											.putExtra("android.intent.extra.USER", UserHandle.USER_CURRENT), 0);
								}
							});
				} catch (NameNotFoundException e) {
                }
				container.addView(d);
				return;
            }
        }
    }

	private String getDialogTitle() {
		String dialogTitle = null;
		if (mAppOpCode == AppOpsManager.OP_COARSE_LOCATION 
		            | mAppOpCode == AppOpsManager.OP_FINE_LOCATION 
					| mAppOpCode == AppOpsManager.OP_MONITOR_LOCATION 
					| mAppOpCode == AppOpsManager.OP_MONITOR_HIGH_POWER_LOCATION) {
            dialogTitle = String.format(mContext.getResources().getString(
                    R.string.ongoing_privacy_dialog_single_app_title), "location");
		} else if (mAppOpCode == AppOpsManager.OP_CAMERA) {
            dialogTitle = String.format(mContext.getResources().getString(
                    R.string.ongoing_privacy_dialog_single_app_title), "camera");
		} else if (mAppOpCode == AppOpsManager.OP_RECORD_AUDIO) {
            dialogTitle = String.format(mContext.getResources().getString(
                    R.string.ongoing_privacy_dialog_single_app_title), "microphone");
		}
		return dialogTitle;
	}

	private String getApplicationName() {
		final PackageManager pm = mContext.getPackageManager();
		ApplicationInfo info;
		
		try {
			info = pm.getApplicationInfo(mAppOpPkg, 0);
		} catch (NameNotFoundException e) {
			info = null;
		}
		if (info != null) {
			String appName = pm.getApplicationLabel(info).toString();
			return appName;
		}
		return null;
	}

	public Drawable getApplicationIcon() {
		final PackageManager pm = mContext.getPackageManager();
		ApplicationInfo info;
		
		try {
			info = pm.getApplicationInfo(mAppOpPkg, 0);
		} catch (NameNotFoundException e) {
			info = null;
		}
        if (info != null) {
			Drawable appIcon = mContext.getPackageManager().getApplicationIcon(info);
			if (appIcon != null) {
                return appIcon;
            }
        }
        return mContext.getDrawable(com.android.internal.R.drawable.sym_def_app_icon);
    }
}