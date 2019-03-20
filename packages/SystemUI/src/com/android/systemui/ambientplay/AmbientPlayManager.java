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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.android.systemui.ambientindication.AmbientIndicationContainer;

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

	private int AMBIENT_RECOGNITION_INTERVAL = 60000; // 1 Minute
	private int AMBIENT_RECOGNITION_INTERVAL_CHARGING = 120000; // 2 Minutes
	private int AMBIENT_RECOGNITION_INTERVAL_EXCEEDED_MATCH_COUNT = 300000; // 5 Minutes
	private int AMBIENT_RECOGNITION_INTERVAL_DATA_ONLY = 180000; // 3 Minutes

	private ACRCloudClient mClient;
	private ACRCloudConfig mConfig;

	private AlarmManager mAlarmManager;
	private BatteryManager mBatteryManager;
	private Context mContext;
	private AmbientIndicationContainer mAmbientIndication;

	private int mResultCode;
	private boolean mProcessing = false;
	private boolean mIsProperState = false;
	private String mArtist;
	private String mSong;
	private int mLastAlarm = 0;
	private long mLastUpdated = 0;
	private int NO_MATCH_COUNT = 0;
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction()) || Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                if (needsUpdate()) {
					Log.d(TAG, "Update needed, starting recognition");
                    updateAmbientPlayAlarm(true);
                    startRecognition();
                }
            } else if (ACTION_UPDATE_AMBIENT_INDICATION.equals(intent.getAction())) {
                updateAmbientPlayAlarm(true);
                startRecognition();
            } else if (Intent.ACTION_TIME_CHANGED.equals(intent.getAction()) || Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
                mLastUpdated = 0;
                mLastAlarm = 0;
                updateAmbientPlayAlarm(false);
            }
        }
    };

    public AmbientPlayManager(Context context, AmbientIndicationContainer ambientIndicationContainer) {
		mContext = context;
		mAmbientIndication = ambientIndicationContainer;
		mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		mBatteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
		
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
        mConfig.accessKey = "e49c241cdaa4257f" + "d4607ef93aa9ca7d";
        mConfig.accessSecret = "TomUVHpZeWvtH2JIUqEXhdp" + "DyY8bizyS6AGJ8Rds";
        mConfig.protocol = ACRCloudConfig.ACRCloudNetworkProtocol.PROTOCOL_HTTP;
        mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_REMOTE;
		
		mClient = new ACRCloudClient();
		mIsProperState = mClient.initWithConfig(mConfig);
		if (mIsProperState) {
            mClient.startPreRecord(3000);
        }

		IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(ACTION_UPDATE_AMBIENT_INDICATION);
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        context.registerReceiver(mReceiver, filter);
    }
	
	private boolean needsUpdate() {
        return System.currentTimeMillis() - mLastUpdated > mLastAlarm;
    }

	public void startRecognition() {
        if (!mIsProperState) {
            Log.d(TAG, "Client configuration is not proper");
            return;
        }
		
		if (!mProcessing) {
			mProcessing = true;
			if (mClient == null || !mClient.startRecognize()) {
				mProcessing = false;
				Log.d(TAG, "Cannot start recognition");
			}
		}
	}

	protected void stopRecognition() {
		if (mProcessing && mClient != null) {
			mClient.stopRecordToRecognize();
			Log.d(TAG, "Stopping record to recognize");
		}
		mProcessing = false;
	}

	protected void cancelRecognition() {
		if (mProcessing && mClient != null) {
			mProcessing = false;
			mClient.cancel();
			Log.d(TAG, "Canceling recognition");
		} 		
	}
	
	@Override
	public void onResult(String result) {
		if (mClient != null) {
			mClient.cancel();
			mProcessing = false;
		}
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
						mAmbientIndication.setAmbientMusic(mSong, mArtist);
						mLastUpdated = System.currentTimeMillis();
						NO_MATCH_COUNT = 0;
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
			} else {
				Log.d(TAG, "Something went wrong" + mResultCode);
				mLastUpdated = System.currentTimeMillis();
				if (!mBatteryManager.isCharging()){
					NO_MATCH_COUNT++;
				} else {
					NO_MATCH_COUNT = 0;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onVolumeChanged(double volume) {
        // no op
    }

	private void updateAmbientPlayAlarm(boolean cancelOnly) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, UPDATE_AMBIENT_INDICATION_PENDING_INTENT_CODE, new Intent(ACTION_UPDATE_AMBIENT_INDICATION), 0);
        mAlarmManager.cancel(pendingIntent);
        if (cancelOnly) {
            return;
        }
        mLastAlarm = 0;
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

}
