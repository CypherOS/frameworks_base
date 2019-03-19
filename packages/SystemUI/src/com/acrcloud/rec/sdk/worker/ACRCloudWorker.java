package com.acrcloud.rec.sdk.worker;

import com.acrcloud.rec.engine.ACRCloudRecognizeEngine;
import com.acrcloud.rec.record.ACRCloudRecorder;
import com.acrcloud.rec.sdk.ACRCloudClient;
import com.acrcloud.rec.sdk.ACRCloudResult;
import com.acrcloud.rec.sdk.recognizer.IACRCloudRecognizer;
import com.acrcloud.rec.sdk.utils.ACRCloudException;
import com.acrcloud.rec.sdk.utils.ACRCloudJsonWrapper;
import com.acrcloud.rec.sdk.utils.ACRCloudLogger;
import com.acrcloud.rec.sdk.utils.ACRCloudResponse;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ACRCloudWorker extends Thread {
    private static final int MAX_RECOGNIZE_BUFFER_LEN = 480000;
    private static final String TAG = "ACRCloudWorker";
    private final int REC_EXT = 1;
    private final int REC_HUM = 2;
    private final int REC_HUM_EXT = 3;
    private ByteArrayOutputStream audioBufferStream;
    private volatile boolean cancel = false;
    private int curEngineType = 0;
    private Map<String, Object> initParams = null;
    private ACRCloudClient mACRCloudClient;
    private IACRCloudRecognizer mRecognizer;
    private int nextRecginzeLen = 0;
    private Map<String, Object> recParams = null;
    private String startRecognizeErrorMsg = "";
    private volatile boolean stop = false;
    private Map<String, String> userParams = null;

    public ACRCloudWorker(IACRCloudRecognizer recogizer, ACRCloudClient acrcloudClient) {
        this.mRecognizer = recogizer;
        this.mACRCloudClient = acrcloudClient;
        this.audioBufferStream = new ByteArrayOutputStream();
        setDaemon(true);
    }

    public ACRCloudWorker(IACRCloudRecognizer recogizer, ACRCloudClient acrcloudClient, Map<String, String> userParams) {
        this.mRecognizer = recogizer;
        this.mACRCloudClient = acrcloudClient;
        this.userParams = userParams;
        this.audioBufferStream = new ByteArrayOutputStream();
        setDaemon(true);
    }

    public void reqCancel() {
        this.cancel = true;
    }

    public void reqStop() {
        this.stop = true;
    }

    private void reset() {
        try {
            this.cancel = false;
            this.stop = false;
            if (this.audioBufferStream != null) {
                this.audioBufferStream.close();
                this.audioBufferStream = null;
            }
            this.mRecognizer = null;
            this.initParams = null;
            this.recParams = null;
            this.userParams = null;
            this.mACRCloudClient = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean startRecognize() {
        ACRCloudLogger.m26d(TAG, "startRecognize");
        try {
            ACRCloudResponse acrcRes = this.mRecognizer.startRecognize(this.userParams);
            if (acrcRes.getStatusCode() == 0) {
                this.initParams = new HashMap();
                this.recParams = new HashMap();
                this.initParams.put("ekey", acrcRes.geteKey());
                this.recParams.put("ekey", acrcRes.geteKey());
                this.initParams.put("fp_time", Integer.valueOf(acrcRes.getFpTime()));
                this.recParams.put("fp_time", Integer.valueOf(acrcRes.getFpTime()));
                this.initParams.put("service_type", Integer.valueOf(acrcRes.getServiceType()));
                this.recParams.put("service_type", Integer.valueOf(acrcRes.getServiceType()));
                this.initParams.put("engine_type", Integer.valueOf(acrcRes.getEngineType()));
                this.recParams.put("engine_type", Integer.valueOf(acrcRes.getEngineType()));
                this.curEngineType = acrcRes.getEngineType();
                this.nextRecginzeLen = acrcRes.getFpTime() * 16;
                return true;
            } else if (acrcRes.getStatusCode() == 3000) {
                this.startRecognizeErrorMsg = acrcRes.getResult();
                return true;
            } else {
                ACRCloudResult ares = new ACRCloudResult();
                ares.setResult(acrcRes.getResult());
                onResult(ares);
                return false;
            }
        } catch (Exception e) {
            this.startRecognizeErrorMsg = ACRCloudException.toErrorString(ACRCloudException.UNKNOW_ERROR, e.getMessage());
            return true;
        }
    }

    private void resumeRecognize() {
        int retryReadAudioDataNum = 0;
        while (!this.cancel) {
            ACRCloudResult result;
            try {
                byte[] data = ACRCloudRecorder.getInstance().getCurrentAudioBuffer();
                if (this.stop || data != null) {
                    retryReadAudioDataNum = 0;
                    if (data != null) {
                        this.audioBufferStream.write(data);
                    }
                    if (ACRCloudRecorder.getInstance().hasAudioData()) {
                        continue;
                    } else if (!this.cancel) {
                        int curBufferLen = this.audioBufferStream.size();
                        byte[] curBuffer;
                        if (this.initParams != null) {
                            if ((curBufferLen >= this.nextRecginzeLen && !this.cancel) || this.stop) {
                                curBuffer = this.audioBufferStream.toByteArray();
                                int curBufferFPLen = curBuffer.length;
                                if (curBufferFPLen > MAX_RECOGNIZE_BUFFER_LEN) {
                                    curBufferFPLen = MAX_RECOGNIZE_BUFFER_LEN;
                                }
                                if (this.stop && this.curEngineType != 2) {
                                    this.curEngineType = 3;
                                }
                                ACRCloudResponse acrcRes = this.mRecognizer.resumeRecognize(curBuffer, curBufferFPLen, this.recParams, this.userParams, this.curEngineType);
                                if (!this.stop || this.curEngineType == 1) {
                                    this.curEngineType = acrcRes.getEngineType();
                                    int curFpTime = acrcRes.getFpTime();
                                    this.recParams.put("fp_time", Integer.valueOf(curFpTime));
                                    if (acrcRes.getStatusCode() == 0) {
                                        int nextServiceType = ((Integer) this.recParams.get("service_type")).intValue() - acrcRes.getResultType();
                                        if (nextServiceType == 0) {
                                            nextServiceType = ((Integer) this.initParams.get("service_type")).intValue();
                                        }
                                        this.recParams.put("service_type", Integer.valueOf(nextServiceType));
                                        result = new ACRCloudResult();
                                        result.setAudioFingerprint(acrcRes.getExtFingerprint());
                                        result.setRecordDataPCM(curBuffer);
                                        result.setResult(ACRCloudJsonWrapper.parse(acrcRes));
                                        onResult(result);
                                    }
                                    if (curFpTime == 0) {
                                        if (acrcRes.getStatusCode() == 3000 || acrcRes.getStatusCode() == ACRCloudException.HTTP_ERROR_TIMEOUT) {
                                            if (curBufferFPLen >= MAX_RECOGNIZE_BUFFER_LEN) {
                                                result = new ACRCloudResult();
                                                result.setAudioFingerprint(acrcRes.getExtFingerprint());
                                                result.setRecordDataPCM(curBuffer);
                                                result.setResult(acrcRes.getResult());
                                                onResult(result);
                                            } else {
                                                this.nextRecginzeLen = MAX_RECOGNIZE_BUFFER_LEN;
                                                this.recParams.put("fp_time", Integer.valueOf(12000));
                                                ACRCloudLogger.m26d(TAG, "http error, next rec len MAX_RECOGNIZE_BUFFER_LEN");
                                            }
                                        } else if (acrcRes.getStatusCode() != 0) {
                                            result = new ACRCloudResult();
                                            result.setAudioFingerprint(acrcRes.getExtFingerprint());
                                            result.setRecordDataPCM(curBuffer);
                                            result.setResult(acrcRes.getResult());
                                            onResult(result);
                                        }
                                        curFpTime = ((Integer) this.initParams.get("fp_time")).intValue();
                                        this.recParams.put("fp_time", Integer.valueOf(curFpTime));
                                        this.recParams.put("service_type", this.initParams.get("service_type"));
                                        this.curEngineType = ((Integer) this.initParams.get("engine_type")).intValue();
                                        this.audioBufferStream.reset();
                                    }
                                    ACRCloudLogger.m26d(TAG, "curBufferLen=" + curBufferLen + "  nextRecginzeLen=" + this.nextRecginzeLen + " curFpTime=" + curFpTime + " service_type=" + ((Integer) this.recParams.get("service_type")));
                                    this.nextRecginzeLen = curFpTime * 16;
                                } else {
                                    result = new ACRCloudResult();
                                    result.setAudioFingerprint(acrcRes.getExtFingerprint());
                                    result.setRecordDataPCM(curBuffer);
                                    result.setResult(ACRCloudJsonWrapper.parse(acrcRes));
                                    onResult(result);
                                    return;
                                }
                            }
                        } else if (curBufferLen >= MAX_RECOGNIZE_BUFFER_LEN) {
                            result = new ACRCloudResult();
                            if (this.startRecognizeErrorMsg == null || "".equals(this.startRecognizeErrorMsg)) {
                                this.startRecognizeErrorMsg = ACRCloudException.toErrorString(3000);
                            }
                            result.setResult(this.startRecognizeErrorMsg);
                            curBuffer = this.audioBufferStream.toByteArray();
                            result.setRecordDataPCM(curBuffer);
                            result.setAudioFingerprint(ACRCloudRecognizeEngine.genFP(curBuffer, curBuffer.length));
                            onResult(result);
                            if (!this.cancel) {
                                startRecognize();
                            }
                            this.audioBufferStream.reset();
                        }
                    } else {
                        return;
                    }
                } else if (retryReadAudioDataNum < 10) {
                    retryReadAudioDataNum++;
                } else {
                    result = new ACRCloudResult();
                    result.setResult(ACRCloudException.toErrorString(ACRCloudException.RECORD_ERROR));
                    onResult(result);
                    ACRCloudRecorder.getInstance().release();
                    return;
                }
            } catch (Exception e) {
                result = new ACRCloudResult();
                result.setResult(ACRCloudException.toErrorString(ACRCloudException.RECORD_ERROR, e.getMessage()));
                onResult(result);
                ACRCloudRecorder.getInstance().release();
                return;
            }
        }
    }

    public void run() {
        super.run();
        if (ACRCloudRecorder.getInstance().startRecording(this.mACRCloudClient)) {
            if (startRecognize()) {
                resumeRecognize();
            }
            reset();
            return;
        }
        ACRCloudResult ares = new ACRCloudResult();
        ares.setResult(ACRCloudException.toErrorString(ACRCloudException.RECORD_ERROR));
        onResult(ares);
        ACRCloudRecorder.getInstance().release();
    }

    private void onResult(ACRCloudResult result) {
        if (!this.cancel) {
            if (this.stop) {
                this.cancel = true;
            }
            if (result.getResult() == null || "".equals(result.getResult())) {
                result.setResult(ACRCloudException.toErrorString(1001));
            }
            ACRCloudLogger.m26d(TAG, "onResult:" + result.getResult());
            this.mACRCloudClient.onResult(result);
        }
    }
}
