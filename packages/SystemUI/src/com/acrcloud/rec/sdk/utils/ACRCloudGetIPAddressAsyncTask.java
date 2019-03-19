package com.acrcloud.rec.sdk.utils;

import android.os.AsyncTask;
import com.acrcloud.rec.sdk.recognizer.ACRCloudRecognizerRemoteImpl;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ACRCloudGetIPAddressAsyncTask extends AsyncTask<String, Integer, String> {
    private static final String TAG = "ACRCloudAsynGetIPAddressTask";

    protected String doInBackground(String... params) {
        String ip = "";
        try {
            return InetAddress.getByName(params[0]).getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "";
        } catch (Exception e2) {
            return "";
        }
    }

    protected void onPostExecute(String ip) {
        super.onPreExecute();
        ACRCloudLogger.m26d(TAG, ">>>>>>>>>>>>>>>  " + ip);
        ACRCloudRecognizerRemoteImpl.serverIP = ip;
    }
}
