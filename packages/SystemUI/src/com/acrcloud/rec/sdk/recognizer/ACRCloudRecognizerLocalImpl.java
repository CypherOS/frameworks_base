package com.acrcloud.rec.sdk.recognizer;

import com.acrcloud.rec.engine.ACRCloudEngineResult;
import com.acrcloud.rec.engine.ACRCloudRecognizeEngine;
import com.acrcloud.rec.sdk.ACRCloudConfig;
import com.acrcloud.rec.sdk.utils.ACRCloudException;
import com.acrcloud.rec.sdk.utils.ACRCloudJsonWrapper;
import com.acrcloud.rec.sdk.utils.ACRCloudLocalRecognizerInitAsyncTask;
import com.acrcloud.rec.sdk.utils.ACRCloudLogger;
import com.acrcloud.rec.sdk.utils.ACRCloudResponse;
import java.io.File;
import java.util.Map;

public class ACRCloudRecognizerLocalImpl implements IACRCloudRecognizer {
    private static final String TAG = "ACRCloudRecognizerLocalImpl";
    private static int maxRecginzeTime = 10000;
    private int initRecginzeTime = ACRCloudException.RECORD_ERROR;
    private String mACRCloudId = "";
    private ACRCloudConfig mConfig = null;
    private ACRCloudRecognizeEngine mEngine = null;
    private int stepRecginzeTime = 1000;

    public ACRCloudRecognizerLocalImpl(ACRCloudConfig config, String acrcloudId) {
        this.mConfig = config;
        this.mACRCloudId = acrcloudId;
    }

    public void init() throws ACRCloudException {
        try {
            new ACRCloudLocalRecognizerInitAsyncTask(this.mConfig).execute(new String[]{this.mACRCloudId});
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.mEngine == null) {
            File iv = new File(this.mConfig.dbPath + "/afp.iv");
            File df = new File(this.mConfig.dbPath + "/afp.df");
            File op = new File(this.mConfig.dbPath + "/afp.op");
            if (!iv.canRead()) {
                throw new ACRCloudException(ACRCloudException.INIT_ERROR, "Offline DB file (afp.iv) are unreadable!");
            } else if (!df.canRead()) {
                throw new ACRCloudException(ACRCloudException.INIT_ERROR, "Offline DB file (afp.df) are unreadable!");
            } else if (op.canRead()) {
                this.mEngine = new ACRCloudRecognizeEngine();
                if (!this.mEngine.init(this.mConfig.dbPath)) {
                    this.mEngine = null;
                    throw new ACRCloudException(ACRCloudException.INIT_ERROR, "Offline DB files are illegal");
                }
            } else {
                throw new ACRCloudException(ACRCloudException.INIT_ERROR, "Offline DB file (afp.op) are unreadable!");
            }
        }
    }

    public ACRCloudResponse startRecognize(Map<String, String> map) {
        ACRCloudResponse initResp = new ACRCloudResponse();
        if (this.mEngine != null) {
            initResp.setFpTime(this.initRecginzeTime);
        } else {
            initResp.setStatusCode(ACRCloudException.NO_INIT_ERROR);
            initResp.setStatusMsg(ACRCloudException.getErrorMsg(ACRCloudException.NO_INIT_ERROR));
        }
        return initResp;
    }

    private ACRCloudResponse doRecognize(byte[] buffer, int bufferLen, Map<String, Object> configParams, boolean isAudio) {
        ACRCloudEngineResult[] r;
        long currentTimeMS = System.currentTimeMillis();
        if (isAudio) {
            r = this.mEngine.recognizePCM(buffer, bufferLen);
        } else {
            r = this.mEngine.recognizeFP(buffer, bufferLen);
        }
        ACRCloudResponse ar = new ACRCloudResponse();
        if (configParams != null) {
            int nowTime = ((Integer) configParams.get("fp_time")).intValue();
            ar.setFpTime(this.stepRecginzeTime + nowTime);
            if (this.stepRecginzeTime + nowTime > maxRecginzeTime) {
                ar.setFpTime(0);
            }
        }
        if (r == null) {
            ar.setStatusCode(1001);
            ar.setStatusMsg(ACRCloudException.getErrorMsg(1001));
        } else {
            ar.setFpTime(0);
        }
        long offsetCorrValue = System.currentTimeMillis() - currentTimeMS;
        ACRCloudLogger.m26d(TAG, "offsetCorrValue=" + offsetCorrValue);
        ar.setOffsetCorrectValue(offsetCorrValue);
        ar.setEngineResults(r);
        return ar;
    }

    public ACRCloudResponse resumeRecognize(byte[] buffer, int bufferLen, Map<String, Object> configParams, Map<String, String> map, int engineType) {
        if (buffer == null || bufferLen == 0 || this.mEngine == null) {
            return null;
        }
        return doRecognize(buffer, bufferLen, configParams, true);
    }

    public void release() {
        if (this.mEngine != null) {
            this.mEngine.release();
            this.mEngine = null;
        }
    }

    public String recognize(byte[] buffer, int bufferLen, Map<String, String> map, boolean isAudio) {
        if (this.mEngine == null) {
            return ACRCloudException.toErrorString(ACRCloudException.NO_INIT_ERROR);
        }
        try {
            return ACRCloudJsonWrapper.parse(doRecognize(buffer, bufferLen, null, isAudio));
        } catch (Exception ex) {
            return ACRCloudException.toErrorString(ACRCloudException.UNKNOW_ERROR, ex.getMessage());
        }
    }
}
