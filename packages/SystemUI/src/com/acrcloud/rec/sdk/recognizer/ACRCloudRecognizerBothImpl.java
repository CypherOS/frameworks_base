package com.acrcloud.rec.sdk.recognizer;

import com.acrcloud.rec.sdk.ACRCloudConfig;
import com.acrcloud.rec.sdk.utils.ACRCloudException;
import com.acrcloud.rec.sdk.utils.ACRCloudJsonWrapper;
import com.acrcloud.rec.sdk.utils.ACRCloudLogger;
import com.acrcloud.rec.sdk.utils.ACRCloudResponse;
import java.util.Map;

public class ACRCloudRecognizerBothImpl implements IACRCloudRecognizer {
    private static final String TAG = "ACRCloudRecognizerBothImpl";
    private boolean isLocalStart = false;
    private boolean isRemoteStart = false;
    private String mACRCloudId = "";
    private ACRCloudConfig mConfig = null;
    private IACRCloudRecognizer mRecognizerLocal = null;
    private IACRCloudRecognizer mRecognizerRemote = null;

    public ACRCloudRecognizerBothImpl(ACRCloudConfig config, String acrcloudId) {
        this.mConfig = config;
        this.mACRCloudId = acrcloudId;
        this.mRecognizerLocal = new ACRCloudRecognizerLocalImpl(this.mConfig, this.mACRCloudId);
        this.mRecognizerRemote = new ACRCloudRecognizerRemoteImpl(this.mConfig, this.mACRCloudId);
    }

    public ACRCloudResponse startRecognize(Map<String, String> userParams) {
        this.isLocalStart = true;
        this.isRemoteStart = false;
        this.mRecognizerLocal.startRecognize(userParams);
        ACRCloudResponse resp = this.mRecognizerRemote.startRecognize(userParams);
        if (resp.getStatusCode() == 0) {
            this.isRemoteStart = true;
        }
        return resp;
    }

    public ACRCloudResponse resumeRecognize(byte[] buffer, int bufferLen, Map<String, Object> configParams, Map<String, String> userParams, int engineType) {
        ACRCloudResponse resp = null;
        if (this.isLocalStart) {
            resp = this.mRecognizerLocal.resumeRecognize(buffer, bufferLen, configParams, userParams, engineType);
            if (resp.getStatusCode() == 0) {
                return resp;
            }
        }
        if (this.isRemoteStart) {
            resp = this.mRecognizerRemote.resumeRecognize(buffer, bufferLen, configParams, userParams, engineType);
        }
        return resp;
    }

    public String recognize(byte[] buffer, int bufferLen, Map<String, String> userParams, boolean isAudio) {
        String res = "";
        res = this.mRecognizerLocal.recognize(buffer, bufferLen, userParams, isAudio);
        try {
            if (ACRCloudJsonWrapper.parse(res, 0).getStatusCode() == 0) {
                return res;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.mRecognizerRemote.recognize(buffer, bufferLen, userParams, isAudio);
    }

    public void init() throws ACRCloudException {
        try {
            this.mRecognizerLocal.init();
        } catch (ACRCloudException e) {
            this.mRecognizerLocal = null;
            ACRCloudLogger.m27e(TAG, "ACRCloud local library init error, " + e.toString());
        }
        this.mRecognizerRemote.init();
    }

    public void release() {
        if (this.mRecognizerLocal != null) {
            this.mRecognizerLocal.release();
        }
        if (this.mRecognizerRemote != null) {
            this.mRecognizerRemote.release();
        }
    }
}
