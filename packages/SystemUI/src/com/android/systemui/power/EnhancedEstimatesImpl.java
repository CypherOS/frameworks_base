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
package com.android.systemui.power;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri.Builder;
import android.provider.Settings.Global;
import android.util.KeyValueListParser;
import android.util.Log;

import com.android.systemui.power.EnhancedEstimates;
import com.android.systemui.power.Estimate;

import java.time.Duration;


public class EnhancedEstimatesImpl implements EnhancedEstimates {

	private static final Uri BATTERY_ESTIMATE_PROVIDER = new Builder()
	        .scheme("content")
	        .authority("com.google.android.apps.turbo.estimated_time_remaining")
			.appendPath("time_remaining")
			.build();
	private static final String IS_BASED_ON_USAGE = "is_based_on_usage";

	private Context mContext;
    private final KeyValueListParser mParser = new KeyValueListParser(',');

	public EnhancedEstimatesImpl(Context context) {
        mContext = context;
    }

    @Override
    public boolean isHybridNotificationEnabled() {
        try {
            if (!mContext.getPackageManager().getPackageInfo("com.google.android.apps.turbo", 512).applicationInfo.enabled) {
                return false;
            }
            updateFlags();
            return mParser.getBoolean("hybrid_enabled", true);
        } catch (NameNotFoundException unused) {
            return false;
        }
    }

	@Override
	public Estimate getEstimate() {
		try (Cursor query = mContext.getContentResolver().query(BATTERY_ESTIMATE_PROVIDER, null, null, null, null)) {
            if (query != null) {
                try {
                    if (query.moveToFirst()) {
                        boolean isBasedOnUsage = true;
                        if (query.getColumnIndex(IS_BASED_ON_USAGE) != -1) {
                            if (query.getInt(query.getColumnIndex(IS_BASED_ON_USAGE)) == 0) {
                                isBasedOnUsage = false;
                            }
                        }
                        Estimate estimate = new Estimate(query.getLong(query.getColumnIndex("battery_estimate")), isBasedOnUsage);
                        if (query != null) {
                            query.close();
                        }
                        return estimate;
                    }
                } catch (Throwable ignored) {
                }
            }
            if (query != null) {
                query.close();
            }
        } catch (Exception e) {
            Log.d("EnhancedEstimates", "Something went wrong when getting an estimate from Turbo", e);
        }
        return null;
    }

    @Override
    public long getLowWarningThreshold() {
        updateFlags();
        return mParser.getLong("low_threshold", Duration.ofHours(3).toMillis());
    }

    @Override
    public long getSevereWarningThreshold() {
        updateFlags();
        return mParser.getLong("severe_threshold", Duration.ofHours(1).toMillis());
    }

	protected void updateFlags() {
        try {
            mParser.setString(Global.getString(mContext.getContentResolver(), "hybrid_sysui_battery_warning_flags"));
        } catch (IllegalArgumentException unused) {
            Log.e("EnhancedEstimates", "Bad hybrid sysui warning flags");
        }
    }
}
