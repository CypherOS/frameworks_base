package com.acrcloud.rec.sdk.utils;

import android.os.AsyncTask;
import com.acrcloud.rec.sdk.ACRCloudConfig;
import java.util.HashMap;
import java.util.Map;

public class ACRCloudLocalRecognizerInitAsyncTask extends AsyncTask<String, Void, Void> {
    private static final String TAG = "ACRCloudLocalRecognizerInitAsyncTask";
    private static final String localRecognizerInitURL = "http://api.acrcloud.com/v1/devices/login";
    private ACRCloudConfig mConfig;

    public ACRCloudLocalRecognizerInitAsyncTask(ACRCloudConfig config) {
        this.mConfig = config;
    }

    protected Void doInBackground(String... params) {
        try {
            Map<String, Object> initParams = new HashMap();
            initParams.put("access_key", this.mConfig.accessKey);
            initParams.put("dk", params[0]);
            initParams.put("type", "offline");
            ACRCloudHttpWrapper.doPost(localRecognizerInitURL, initParams, 5000);
        } catch (Exception e) {
        }
        return null;
    }

    protected void onPostExecute(Void result) {
        super.onPreExecute();
    }
}
