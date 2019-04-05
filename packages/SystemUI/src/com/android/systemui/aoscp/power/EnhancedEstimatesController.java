/*
 * Copyright (C) 2018-2019 CypherOS
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

package com.android.systemui.aoscp.power;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.android.systemui.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class EnhancedEstimatesController {

    public static final String TAG = "EnhancedEstimatesController";

    public static final String MISERVICES_PACKAGE = "co.aoscp.miservices";
    private static final Uri POWER_ESTIMATE_PROVIDER = Uri.parse("content://co.aoscp.miservices.providers.batterybridge/estimate");
    private static final String[] DATA = new String[] {"_id", "battery_estimate"};
    private static String ACTION_UPDATE_ESTIMATE = "co.aoscp.miservices.battery.bridge.UPDATE_ESTIMATE";
    public static Intent INTENT_UPDATE_ESTIMATE = new Intent(ACTION_UPDATE_ESTIMATE);

    private static EnhancedEstimatesController sController;
    private Context mContext;
    private final Handler mHandler;
    private IEnhancedEstimates mListener;

    private BroadcastReceiver mEstimatesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }
            if (intent.getAction().equals(INTENT_UPDATE_ESTIMATE.getAction())) {
                updateBatteryEstimates();
            }
        }
    };

    public static EnhancedEstimatesController get(Context context) {
        Assert.isMainThread();
        if (sController == null) {
            sController = new EnhancedEstimatesController(context);
        }
        return sController;
    }

    private EnhancedEstimatesController(Context context) {
        mContext = context;
        mHandler = new Handler();
        mContext.registerReceiver(mEstimatesReceiver, new IntentFilter(INTENT_UPDATE_ESTIMATE.getAction()));
    }

    private void updateBatteryEstimates() {
        List<EstimatesData> info = getEstimates();
        if (info.size() < 1) return;
        EstimatesData data = info.get(0);
        if (mListener != null) {
            mListener.onBatteryRemainingEstimateRetrieved(data.getEstimate());
        }
    }

    private List<EstimatesData> getEstimates() {
        List<EstimatesData> result = new ArrayList<>();
        try (Cursor c = mContext.getContentResolver().query(POWER_ESTIMATE_PROVIDER, DATA, null, null, null)) {
            if (c != null) {
                while (c.moveToNext()) {
                    result.add(new EstimatesData(c.getInt(0), c.getString(1)));
                }
            }
        }
        return result;
    }

    public void setListener(IEnhancedEstimates listener) {
        mListener = listener;
    }
}
