package com.acrcloud.rec.sdk;

import android.app.Activity;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.p000v4.view.PointerIconCompat;
import android.telephony.TelephonyManager;
import com.acrcloud.rec.engine.ACRCloudRecognizeEngine;
import com.acrcloud.rec.record.ACRCloudRecorder;
import com.acrcloud.rec.sdk.ACRCloudConfig.ACRCloudRecMode;
import com.acrcloud.rec.sdk.recognizer.ACRCloudRecognizerBothImpl;
import com.acrcloud.rec.sdk.recognizer.ACRCloudRecognizerLocalImpl;
import com.acrcloud.rec.sdk.recognizer.ACRCloudRecognizerRemoteImpl;
import com.acrcloud.rec.sdk.recognizer.IACRCloudRecognizer;
import com.acrcloud.rec.sdk.utils.ACRCloudException;
import com.acrcloud.rec.sdk.utils.ACRCloudLogger;
import com.acrcloud.rec.sdk.utils.ACRCloudUtils;
import com.acrcloud.rec.sdk.worker.ACRCloudWorker;
import com.beat.light.util.ConstantHistory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.Map;

public class ACRCloudClient extends Activity {
    private static final String TAG = "ACRCloudClient";
    private final int RECOG_FINISH = 1001;
    private final int VOLUME_CHANGED = PointerIconCompat.TYPE_HAND;
    private boolean isPreRecord = false;
    private boolean isRecognizeing = false;
    private ACRCloudConfig mConfig = null;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1001:
                    ACRCloudResult res = msg.obj;
                    if (ACRCloudClient.this.mConfig.acrcloudResultWithAudioListener != null) {
                        ACRCloudClient.this.mConfig.acrcloudResultWithAudioListener.onResult(res);
                        return;
                    } else {
                        ACRCloudClient.this.mConfig.acrcloudListener.onResult(res.getResult());
                        return;
                    }
                case PointerIconCompat.TYPE_HAND /*1002*/:
                    double volume = ((Double) msg.obj).doubleValue();
                    if (ACRCloudClient.this.mConfig.acrcloudResultWithAudioListener != null) {
                        ACRCloudClient.this.mConfig.acrcloudResultWithAudioListener.onVolumeChanged(volume);
                        return;
                    } else {
                        ACRCloudClient.this.mConfig.acrcloudListener.onVolumeChanged(volume);
                        return;
                    }
                default:
                    return;
            }
        }
    };
    private IACRCloudRecognizer mRecognizer = null;
    private ACRCloudWorker mWorker = null;

    /* renamed from: com.acrcloud.rec.sdk.ACRCloudClient$1RecognizeWorker */
    class C03281RecognizeWorker extends Thread {
        public boolean isAudio = true;
        public byte[] mBuffer = null;
        public int mBufferLen = 0;
        public IRecognizeCallback mCallback = null;
        public ACRCloudClient mClient = null;
        public Map<String, String> mUserParams = null;

        public C03281RecognizeWorker(ACRCloudClient client, byte[] buffer, int bufferLen, Map<String, String> userParams, boolean isAudio, IRecognizeCallback callback) {
            this.mBuffer = buffer;
            this.mBufferLen = bufferLen;
            this.mUserParams = userParams;
            this.isAudio = isAudio;
            this.mCallback = callback;
            this.mClient = client;
            setDaemon(true);
        }

        public void run() {
            try {
                String result = "";
                if (this.isAudio) {
                    result = this.mClient.recognize(this.mBuffer, this.mBufferLen, this.mUserParams);
                } else {
                    result = this.mClient.recognizeByFingerprint(this.mBuffer, this.mBufferLen, this.mUserParams);
                }
                this.mCallback.onResult(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean initWithConfig(ACRCloudConfig config) {
        if (config == null) {
            return false;
        }
        if (config.audioRecordSource < 0 || config.audioRecordSource > 7) {
            ACRCloudLogger.m27e(TAG, "config.audioRecordSource : " + config.audioRecordSource);
        }
        ACRCloudRecorder.RECORDER_AUDIO_SOURCE = config.audioRecordSource;
        this.mConfig = config;
        String acrId = check();
        if (this.mRecognizer != null) {
            return true;
        }
        if (this.mConfig.reqMode == ACRCloudRecMode.REC_MODE_LOCAL) {
            this.mRecognizer = new ACRCloudRecognizerLocalImpl(this.mConfig, acrId);
        } else if (this.mConfig.reqMode == ACRCloudRecMode.REC_MODE_REMOTE) {
            this.mRecognizer = new ACRCloudRecognizerRemoteImpl(this.mConfig, acrId);
        } else if (this.mConfig.reqMode != ACRCloudRecMode.REC_MODE_BOTH) {
            return false;
        } else {
            this.mRecognizer = new ACRCloudRecognizerBothImpl(this.mConfig, acrId);
        }
        try {
            this.mRecognizer.init();
            return true;
        } catch (ACRCloudException e) {
            ACRCloudResult result = new ACRCloudResult();
            result.setResult(e.toString());
            onResult(result);
            this.mRecognizer = null;
            return false;
        }
    }

    public boolean startRecognize() {
        if (this.mConfig == null || this.mRecognizer == null || (this.mConfig.acrcloudListener == null && this.mConfig.acrcloudResultWithAudioListener == null)) {
            return false;
        }
        cancel();
        this.mWorker = new ACRCloudWorker(this.mRecognizer, this);
        this.mWorker.start();
        this.isRecognizeing = true;
        return true;
    }

    public boolean startRecognize(Map<String, String> userParams) {
        if (this.mConfig == null || this.mRecognizer == null || (this.mConfig.acrcloudListener == null && this.mConfig.acrcloudResultWithAudioListener == null)) {
            return false;
        }
        cancel();
        this.mWorker = new ACRCloudWorker(this.mRecognizer, this, userParams);
        this.mWorker.start();
        this.isRecognizeing = true;
        return true;
    }

    private void recognize(byte[] buffer, int bufferLen, Map<String, String> userParams, boolean isAudio, IRecognizeCallback callback) {
        new C03281RecognizeWorker(this, buffer, bufferLen, userParams, isAudio, callback).start();
    }

    public String recognize(byte[] buffer, int bufferLen) {
        if (this.mRecognizer == null) {
            return ACRCloudException.toErrorString(ACRCloudException.NO_INIT_ERROR);
        }
        return this.mRecognizer.recognize(buffer, bufferLen, null, true);
    }

    public String recognize(byte[] buffer, int bufferLen, int sampleRate, int nChannels) {
        if (this.mRecognizer == null) {
            return ACRCloudException.toErrorString(ACRCloudException.NO_INIT_ERROR);
        }
        byte[] pcm = buffer;
        if (!(sampleRate == 8000 && nChannels == 1)) {
            pcm = resample(buffer, bufferLen, sampleRate, nChannels);
            if (pcm == null) {
                return ACRCloudException.toErrorString(ACRCloudException.RESAMPLE_ERROR);
            }
        }
        return this.mRecognizer.recognize(pcm, pcm.length, null, true);
    }

    public void recognize(byte[] buffer, int bufferLen, IRecognizeCallback callback) {
        recognize(buffer, bufferLen, null, true, callback);
    }

    public void recognize(byte[] buffer, int bufferLen, int sampleRate, int nChannels, IRecognizeCallback callback) {
        try {
            if (this.mRecognizer == null) {
                callback.onResult(ACRCloudException.toErrorString(ACRCloudException.NO_INIT_ERROR));
            }
            byte[] pcm = buffer;
            if (!(sampleRate == 8000 && nChannels == 1)) {
                pcm = resample(buffer, bufferLen, sampleRate, nChannels);
                if (pcm == null) {
                    callback.onResult(ACRCloudException.toErrorString(ACRCloudException.RESAMPLE_ERROR));
                }
            }
            recognize(pcm, pcm.length, null, true, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String recognize(byte[] buffer, int bufferLen, Map<String, String> userParams) {
        if (this.mRecognizer == null) {
            return ACRCloudException.getErrorMsg(ACRCloudException.NO_INIT_ERROR);
        }
        return this.mRecognizer.recognize(buffer, bufferLen, userParams, true);
    }

    public String recognize(byte[] buffer, int bufferLen, int sampleRate, int nChannels, Map<String, String> userParams) {
        if (this.mRecognizer == null) {
            return ACRCloudException.toErrorString(ACRCloudException.NO_INIT_ERROR);
        }
        byte[] pcm = buffer;
        if (!(sampleRate == 8000 && nChannels == 1)) {
            pcm = resample(buffer, bufferLen, sampleRate, nChannels);
            if (pcm == null) {
                return ACRCloudException.toErrorString(ACRCloudException.RESAMPLE_ERROR);
            }
        }
        return this.mRecognizer.recognize(pcm, pcm.length, userParams, true);
    }

    public void recognize(byte[] buffer, int bufferLen, Map<String, String> userParams, IRecognizeCallback callback) {
        recognize(buffer, bufferLen, (Map) userParams, true, callback);
    }

    public void recognize(byte[] buffer, int bufferLen, int sampleRate, int nChannels, Map<String, String> userParams, IRecognizeCallback callback) {
        try {
            if (this.mRecognizer == null) {
                callback.onResult(ACRCloudException.toErrorString(ACRCloudException.NO_INIT_ERROR));
            }
            byte[] pcm = buffer;
            if (!(sampleRate == 8000 && nChannels == 1)) {
                pcm = resample(buffer, bufferLen, sampleRate, nChannels);
                if (pcm == null) {
                    callback.onResult(ACRCloudException.toErrorString(ACRCloudException.RESAMPLE_ERROR));
                }
            }
            recognize(pcm, pcm.length, (Map) userParams, true, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String recognizeByFingerprint(byte[] fps, int fpsLen) {
        if (this.mRecognizer == null) {
            return ACRCloudException.getErrorMsg(ACRCloudException.NO_INIT_ERROR);
        }
        return this.mRecognizer.recognize(fps, fpsLen, null, false);
    }

    public void recognizeByFingerprint(byte[] buffer, int bufferLen, IRecognizeCallback callback) {
        recognize(buffer, bufferLen, null, false, callback);
    }

    public String recognizeByFingerprint(byte[] fps, int fpsLen, Map<String, String> userParams) {
        if (this.mRecognizer == null) {
            return ACRCloudException.getErrorMsg(ACRCloudException.NO_INIT_ERROR);
        }
        return this.mRecognizer.recognize(fps, fpsLen, userParams, false);
    }

    public void recognizeByFingerprint(byte[] buffer, int bufferLen, Map<String, String> userParams, IRecognizeCallback callback) {
        recognize(buffer, bufferLen, (Map) userParams, false, callback);
    }

    public static byte[] createClientFingerprint(byte[] buffer, int bufferLen) {
        return ACRCloudRecognizeEngine.genFP(buffer, bufferLen);
    }

    public static byte[] createClientFingerprint(byte[] buffer, int bufferLen, int sampleRate, int nChannels) {
        byte[] pcm = resample(buffer, bufferLen, sampleRate, nChannels);
        if (pcm == null) {
            return null;
        }
        return ACRCloudRecognizeEngine.genFP(pcm, pcm.length);
    }

    public static byte[] createHummingClientFingerprint(byte[] buffer, int bufferLen) {
        return ACRCloudRecognizeEngine.genHumFP(buffer, bufferLen);
    }

    public static byte[] createHummingClientFingerprint(byte[] buffer, int bufferLen, int sampleRate, int nChannels) {
        if (resample(buffer, bufferLen, sampleRate, nChannels) == null) {
            return null;
        }
        return ACRCloudRecognizeEngine.genHumFP(buffer, bufferLen);
    }

    public static byte[] resample(byte[] pcmBuffer, int pcmBufferLen, int sampleRate, int nChannels) {
        if (pcmBuffer == null || pcmBufferLen < 0 || pcmBufferLen > pcmBuffer.length) {
            return null;
        }
        return ACRCloudRecognizeEngine.resample(pcmBuffer, pcmBufferLen, sampleRate, nChannels, 16, 0);
    }

    public static byte[] resampleBit32Int(byte[] pcmBuffer, int pcmBufferLen, int sampleRate, int nChannels) {
        if (pcmBuffer == null || pcmBufferLen < 0 || pcmBufferLen > pcmBuffer.length) {
            return null;
        }
        return ACRCloudRecognizeEngine.resample(pcmBuffer, pcmBufferLen, sampleRate, nChannels, 32, 0);
    }

    public static byte[] resampleBit32Float(byte[] pcmBuffer, int pcmBufferLen, int sampleRate, int nChannels) {
        if (pcmBuffer == null || pcmBufferLen < 0 || pcmBufferLen > pcmBuffer.length) {
            return null;
        }
        return ACRCloudRecognizeEngine.resample(pcmBuffer, pcmBufferLen, sampleRate, nChannels, 32, 1);
    }

    public void stopRecordToRecognize() {
        try {
            if (this.mWorker != null) {
                this.mWorker.reqStop();
            }
            if (!this.isPreRecord) {
                ACRCloudRecorder.getInstance().stopRecording();
            }
            this.isRecognizeing = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startPreRecord(int recordTimeMS) {
        try {
            ACRCloudRecorder.getInstance().setPreRecordTime(recordTimeMS);
            ACRCloudRecorder.getInstance().startRecording(this);
            this.isPreRecord = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopPreRecord() {
        try {
            if (this.isPreRecord) {
                ACRCloudRecorder.getInstance().stopRecording();
                this.isPreRecord = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        try {
            if (this.mWorker != null) {
                this.mWorker.reqCancel();
                this.mWorker = null;
            }
            if (!this.isPreRecord) {
                ACRCloudRecorder.getInstance().stopRecording();
            }
            this.isRecognizeing = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onResult(ACRCloudResult results) {
        try {
            Message msg = new Message();
            msg.obj = results;
            msg.what = 1001;
            this.mHandler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onVolumeChanged(double volume) {
        try {
            if (this.isRecognizeing) {
                Message msg = new Message();
                msg.obj = Double.valueOf(volume);
                msg.what = PointerIconCompat.TYPE_HAND;
                this.mHandler.sendMessage(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private InetAddress getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                Enumeration<InetAddress> enumIpAddr = ((NetworkInterface) en.nextElement()).getInetAddresses();
                while (enumIpAddr.hasMoreElements()) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private String getMacAddress() {
        String strMacAddr = null;
        try {
            InetAddress ip = getLocalIpAddress();
            if (ip == null) {
                return null;
            }
            byte[] b = NetworkInterface.getByInetAddress(ip).getHardwareAddress();
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < b.length; i++) {
                if (i != 0) {
                    buffer.append(':');
                }
                String str = Integer.toHexString(b[i] & 255);
                if (str.length() == 1) {
                    str = 0 + str;
                }
                buffer.append(str);
            }
            strMacAddr = buffer.toString().toUpperCase();
            return strMacAddr;
        } catch (Exception e) {
        }
    }

    private String getACRCloudId() {
        String uuid = "";
        try {
            if (this.mConfig.context == null) {
                return "";
            }
            TelephonyManager tm = (TelephonyManager) ((ContextWrapper) this.mConfig.context).getBaseContext().getSystemService("phone");
            uuid = getMacAddress();
            if (uuid != null) {
                return uuid;
            }
            uuid = tm.getDeviceId();
            if (uuid == null) {
                uuid = "";
            }
            uuid = uuid + System.currentTimeMillis() + this.mConfig.accessKey + Math.random();
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(uuid.getBytes());
                byte[] hash = md5.digest();
                StringBuilder hex = new StringBuilder(hash.length * 2);
                for (byte b : hash) {
                    if ((b & 255) < 16) {
                        hex.append("0");
                    }
                    hex.append(Integer.toHexString(b & 255));
                }
                return hex.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        } catch (Exception e2) {
            uuid = "" + System.currentTimeMillis() + this.mConfig.accessKey + Math.random();
        }
    }

    public boolean isNetworkConnected() {
        if (this.mConfig == null || this.mConfig.context == null) {
            return false;
        }
        return ACRCloudUtils.isNetworkConnected(this.mConfig.context);
    }

    public String check() {
        String id = "";
        try {
            SharedPreferences mySharedPreferences = this.mConfig.context.getSharedPreferences("acrcloud", 0);
            id = mySharedPreferences.getString(ConstantHistory.KEY_ID, "");
            if (id != null && !"".equals(id)) {
                return id;
            }
            id = getACRCloudId();
            Editor editor = mySharedPreferences.edit();
            editor.putString(ConstantHistory.KEY_ID, id);
            editor.commit();
            return id;
        } catch (Exception e) {
            e.printStackTrace();
            return id;
        }
    }

    public void release() {
        if (this.mRecognizer != null) {
            try {
                cancel();
                stopPreRecord();
                this.mRecognizer.release();
                this.mRecognizer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
