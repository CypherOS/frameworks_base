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
 * limitations under the License
 */

package com.android.systemui.ambientplay;

import  static android.provider.Settings.System.AMBIENT_RECOGNITION;
import  static android.provider.Settings.System.AMBIENT_RECOGNITION_KEYGUARD;

import android.app.AlarmManager;
import android.app.AlarmManager.OnAlarmListener;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import co.aoscp.miservices.shared.Bits;

import com.android.internal.aoscp.AmbientHistoryManager;

import com.android.systemui.quickspace.ambientindication.AmbientIndicationContainer;

import com.acrcloud.rec.sdk.ACRCloudClient;
import com.acrcloud.rec.sdk.ACRCloudConfig;
import com.acrcloud.rec.sdk.IACRCloudListener;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AmbientPlayManager implements IACRCloudListener {

    private static final String TAG = "AmbientPlayManager";
    private String ACTION_UPDATE_AMBIENT_INDICATION = "update_ambient_indication";
    private int UPDATE_AMBIENT_INDICATION_PENDING_INTENT_CODE = 96545687;

    private int AMBIENT_RECOGNITION_INTERVAL = 180000; // 3 Minutes
    private int AMBIENT_RECOGNITION_INTERVAL_CHARGING = 300000; // 5 Minutes
    private int AMBIENT_RECOGNITION_INTERVAL_EXCEEDED_MATCH_COUNT = 330000; // 5 Minutes 30 Seconds
    private int AMBIENT_RECOGNITION_INTERVAL_DATA_ONLY = 210000; // 3 Minutes 30 Seconds
    private int AMBIENT_EVENT_DURATION = 60000; // 1 Minute

    private ACRCloudClient mClient;
    private ACRCloudConfig mConfig;

    private AlarmManager mAlarmManager;
    private BatteryManager mBatteryManager;
    private Context mContext;
    private AmbientIndicationContainer mAmbientIndication;
    private AmbientPlaySettingsObserver mSettingsObserver;
    private Handler mHandler;

    private int mResultCode;
    private boolean mIsEnabled;
    private boolean mProcessing = false;
    private boolean mIsProperState = false;
    private String mArtist;
    private String mSong;
    private int mLastAlarm = 0;
    private long mLastUpdated = 0;
    private int NO_MATCH_COUNT = 0;

    private boolean mReceiverRegistered = false;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction()) || Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                if (needsUpdate()) {
                    Log.d(TAG, "Update needed, starting recognition");
                    updateAlarm(true);
                    startRecognition();
                }
            } else if (ACTION_UPDATE_AMBIENT_INDICATION.equals(intent.getAction())) {
                Log.d(TAG, "Starting recognition by alarm");
                updateAlarm(true);
                startRecognition();
            } else if (Intent.ACTION_TIME_CHANGED.equals(intent.getAction()) || Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
                mLastUpdated = 0;
                mLastAlarm = 0;
                updateAlarm(false);
            }
        }
    };

    public AmbientPlayManager(Context context, AmbientIndicationContainer ambientIndicationContainer) {
        mContext = context;
        mAmbientIndication = ambientIndicationContainer;
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mBatteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        mSettingsObserver = new AmbientPlaySettingsObserver();
        mSettingsObserver.observe();
        mSettingsObserver.updateEnabled();
        mSettingsObserver.updateAllowedOnKeyguard();
        mHandler = new Handler();

        String path = Environment.getExternalStorageDirectory().toString() + "/acrcloud/model";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        mConfig = new ACRCloudConfig();
        mConfig.acrcloudListener = this;
        mConfig.context = mContext;
        mConfig.host = "identify-global.acrcloud.com";
        mConfig.dbPath = path;
        mConfig.accessKey = Bits.getKey();
        mConfig.accessSecret = Bits.getSecret();
        mConfig.protocol = ACRCloudConfig.ACRCloudNetworkProtocol.PROTOCOL_HTTP;
        mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_REMOTE;

        mClient = new ACRCloudClient();
        mIsProperState = mClient.initWithConfig(mConfig);
    }

    private void initUpdateReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(ACTION_UPDATE_AMBIENT_INDICATION);
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        mContext.registerReceiver(mReceiver, filter);
        mReceiverRegistered = true;
    }

    private boolean needsUpdate() {
        if (!mIsEnabled) {
            return false;
        }
        return System.currentTimeMillis() - mLastUpdated > mLastAlarm;
    }

    public void startRecognition() {
        if (!mIsProperState) {
            Log.d(TAG, "Client configuration is not proper");
            return;
        }
        mClient.startPreRecord(3000);
        if (mIsEnabled) {
            if (!mProcessing) {
                mProcessing = true;
                if (mClient == null || !mClient.startRecognize()) {
                    mProcessing = false;
                    Log.d(TAG, "Cannot start recognition");
                }
            }
        }
    }

    protected void stopRecognition() {
        if (mProcessing && mClient != null) {
            mClient.stopRecordToRecognize();
            mClient.stopPreRecord();
            Log.d(TAG, "Stopping record to recognize");
        }
        mProcessing = false;
    }

    protected void cancelRecognition() {
        if (mProcessing && mClient != null) {
            mProcessing = false;
            mClient.cancel();
            mClient.stopPreRecord();
            Log.d(TAG, "Canceling recognition");
            updateAlarm(true);
        }
    }

    protected void setEnabled(boolean enabled) {
        if (enabled) {
            initUpdateReceiver();
        } else {
            cancelRecognition();
            if (mReceiverRegistered) {
                mContext.unregisterReceiver(mReceiver);
                mReceiverRegistered = false;
            }
        }
    }

    @Override
    public void onResult(String result) {
        if (mClient != null) {
            mClient.cancel();
            mClient.stopPreRecord();
            mProcessing = false;
        }
        if (!mIsEnabled) return;
        try {
            JSONObject info = new JSONObject(result);
            mResultCode = info.getJSONObject("status").getInt("code");
            if (mResultCode == 0) {
                JSONObject metadata = info.getJSONObject("metadata");
                if (metadata.has("music")) {
                    JSONObject music = (JSONObject) metadata.getJSONArray("music").get(0);
                    mSong = music.getString("title");
                    JSONArray artists = music.getJSONArray("artists");
                    for (int t = 0; t < artists.length(); t++) {
                        JSONObject art = (JSONObject) artists.get(t);
                        if (artists.length() > 1) {
                            boolean contains = false;
                            for (String ss : art.getString("name").split(" ")) {
                                if (!contains) {
                                    contains = mArtist.matches(".*\\b" + ss + "\\b.*");
                                }
                            }
                            if (!contains) {
                                if (t == 0) {
                                    mArtist = art.getString("name");
                                } else if (t > 0) {
                                    mArtist += " - " + art.getString("name");
                                }
                            }
                        } else {
                            mArtist = art.getString("name");
                        }
                        Log.d(TAG, "Found a match, showing song in AmbientIndication");
                        AmbientHistoryManager.addSong(mSong, mArtist, mContext);
                        mAmbientIndication.setAmbientMusic(mSong, mArtist);
                        mLastUpdated = System.currentTimeMillis();
                        NO_MATCH_COUNT = 0;
                        mHandler.postDelayed(() -> {
                            mAmbientIndication.hideAmbientMusic();
                        }, AMBIENT_EVENT_DURATION);
                        updateAlarm(false);
                    }
                }
                return;
            }
            if (mResultCode == 1001) {
                Log.d(TAG, "No results found");
                mLastUpdated = System.currentTimeMillis();
                if (!mBatteryManager.isCharging()){
                    NO_MATCH_COUNT++;
                } else {
                    NO_MATCH_COUNT = 0;
                }
                updateAlarm(false);
            } else {
                Log.d(TAG, "Something went wrong" + mResultCode);
                mLastUpdated = System.currentTimeMillis();
                if (!mBatteryManager.isCharging()){
                    NO_MATCH_COUNT++;
                } else {
                    NO_MATCH_COUNT = 0;
                }
                updateAlarm(false);
            }
          } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onVolumeChanged(double volume) {
        // no op
    }

    private void updateAlarm(boolean cancelOnly) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, UPDATE_AMBIENT_INDICATION_PENDING_INTENT_CODE, new Intent(ACTION_UPDATE_AMBIENT_INDICATION), 0);
        mAlarmManager.cancel(pendingIntent);
        if (cancelOnly) {
            Log.d(TAG, "Alarm canceled");
            return;
        }
        mLastAlarm = 0;
        if (!mIsEnabled) return;
        int networkStatus = getNetworkStatus();
        int duration = AMBIENT_RECOGNITION_INTERVAL; // Default

        /*
         * Let's try to reduce battery consumption here.
         *  - If device is charging then let's not worry about scan interval and let's scan every 2 minutes, else
         *  - If device is not able to find matches for 20 consecutive times.
         *    then chances are that user is probably not listening to music or maybe sleeping
         *    So, Bump the scan interval to 5 minutes, else
         *  - If device is on Mobile Data or anything else then let's set it to 3 minutes.
         */

        if (mBatteryManager.isCharging()) {
            duration = AMBIENT_RECOGNITION_INTERVAL_CHARGING;
        } else if (NO_MATCH_COUNT >= 20) {
            duration = AMBIENT_RECOGNITION_INTERVAL_EXCEEDED_MATCH_COUNT;
        } else if (networkStatus == 1 || networkStatus == 2) {
            duration = AMBIENT_RECOGNITION_INTERVAL_DATA_ONLY;
        }
        mLastAlarm = duration;
        mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + duration, pendingIntent);
        Log.d(TAG, "Alarm set");
    }

    public int getNetworkStatus() {
        final ConnectivityManager connectivityManager
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        final Network network = connectivityManager.getActiveNetwork();
        final NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        /*
         * Return -1 if We don't have any network connectivity
         * Return 0 if we are on WiFi  (desired)
         * Return 1 if we are on MobileData (Little less desired)
         * Return 2 if not sure which connection is user on but has network connectivity
         */
        // NetworkInfo object will return null in case device is in flight mode.
        if (activeNetworkInfo == null)
            return -1;
        else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
            return 0;
        else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
            return 1;
        else if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
            return 2;
        else
            return -1;
    }

    private class AmbientPlaySettingsObserver extends ContentObserver {
        AmbientPlaySettingsObserver() {
            super(null);
        }

        void observe() {
            mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(AMBIENT_RECOGNITION),
                    false, this, UserHandle.USER_ALL);
            mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(AMBIENT_RECOGNITION_KEYGUARD),
                    false, this, UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (uri.equals(Settings.System.getUriFor(AMBIENT_RECOGNITION))) {
                updateEnabled();
                updateAlarm(false);
            } else if (uri.equals(Settings.System.getUriFor(AMBIENT_RECOGNITION_KEYGUARD))) {
                updateAllowedOnKeyguard();
            }
        }

        public void updateEnabled() {
            mIsEnabled = Settings.System.getIntForUser(mContext.getContentResolver(),
                    AMBIENT_RECOGNITION, 0, UserHandle.USER_CURRENT) != 0;
            setEnabled(mIsEnabled);
            mAmbientIndication.setEnabled(mIsEnabled);
        }

        public void updateAllowedOnKeyguard() {
            boolean allowedOnKeyguard = Settings.System.getIntForUser(mContext.getContentResolver(),
                    AMBIENT_RECOGNITION_KEYGUARD, 1, UserHandle.USER_CURRENT) != 0;
            mAmbientIndication.setAllowedOnKeyguard(allowedOnKeyguard);
        }
    }
}
