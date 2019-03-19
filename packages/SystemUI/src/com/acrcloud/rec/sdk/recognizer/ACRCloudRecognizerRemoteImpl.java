package com.acrcloud.rec.sdk.recognizer;

import android.util.Base64;
import com.acrcloud.rec.engine.ACRCloudRecognizeEngine;
import com.acrcloud.rec.sdk.ACRCloudConfig;
import com.acrcloud.rec.sdk.ACRCloudConfig.ACRCloudNetworkProtocol;
import com.acrcloud.rec.sdk.utils.ACRCloudException;
import com.acrcloud.rec.sdk.utils.ACRCloudGetIPAddressAsyncTask;
import com.acrcloud.rec.sdk.utils.ACRCloudHttpWrapper;
import com.acrcloud.rec.sdk.utils.ACRCloudHttpWrapperImpl;
import com.acrcloud.rec.sdk.utils.ACRCloudJsonWrapper;
import com.acrcloud.rec.sdk.utils.ACRCloudLogger;
import com.acrcloud.rec.sdk.utils.ACRCloudResponse;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.json.JSONException;
import org.json.JSONObject;

public class ACRCloudRecognizerRemoteImpl implements IACRCloudRecognizer {
    private static final String PLATFORM = "android";
    private static final String TAG = "ACRCloudRecognizerRemoteImpl";
    private static int mGetServerIPRetryNum = 5;
    public static String serverIP = null;
    private String action = "/rec?access_key=";
    private Map<String, Object> initParam = null;
    private String mACRCloudId = "";
    private ACRCloudConfig mConfig = null;
    private int retry = 3;

    public ACRCloudRecognizerRemoteImpl(ACRCloudConfig config, String acrcloudId) {
        this.mConfig = config;
        this.mACRCloudId = acrcloudId;
    }

    private String getURL(String tAction) {
        String url = this.mConfig.host;
        String protocol = "http";
        if (this.mConfig.protocol == ACRCloudNetworkProtocol.PROTOCOL_HTTPS) {
            protocol = "https";
        } else if (serverIP == null || "".equals(serverIP)) {
            ACRCloudLogger.m26d(TAG, "ACRCloudGetIPAddressAsyncTask");
            if (mGetServerIPRetryNum > 0) {
                new ACRCloudGetIPAddressAsyncTask().execute(new String[]{url});
            }
            mGetServerIPRetryNum--;
        } else {
            url = serverIP;
        }
        return protocol + "://" + url + tAction;
    }

    private Map<String, Object> getInitParams() {
        Map<String, Object> params = new HashMap();
        params.put("rec_type", "recording");
        params.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Long.valueOf(System.currentTimeMillis())));
        params.put("action", "rec_init");
        params.put("dk", this.mACRCloudId);
        return params;
    }

    private Map<String, Object> getRecParams(byte[] buffer, int bufferLen, Map<String, Object> configParams, int engineType) {
        String ekey = (String) configParams.get("ekey");
        int fpTime = ((Integer) configParams.get("fp_time")).intValue();
        int serviceType = ((Integer) configParams.get("service_type")).intValue();
        Map<String, Object> params = getInitParams();
        byte[] fpBuffer;
        byte[] hummingFpBuffer;
        switch (engineType) {
            case 0:
            case 1:
                fpBuffer = ACRCloudRecognizeEngine.genFP(buffer, bufferLen, ekey, this.mConfig.accessSecret);
                if (fpBuffer != null) {
                    params.put("sample", fpBuffer);
                    params.put("sample_bytes", fpBuffer.length + "");
                    break;
                }
                return null;
            case 2:
                hummingFpBuffer = ACRCloudRecognizeEngine.genHumFP(buffer, bufferLen);
                if (hummingFpBuffer != null) {
                    params.put("sample_hum", hummingFpBuffer);
                    params.put("sample_hum_bytes", hummingFpBuffer.length + "");
                    break;
                }
                return null;
            case 3:
                fpBuffer = ACRCloudRecognizeEngine.genFP(buffer, bufferLen, ekey, this.mConfig.accessSecret);
                hummingFpBuffer = ACRCloudRecognizeEngine.genHumFP(buffer, bufferLen);
                if (fpBuffer != null || hummingFpBuffer != null) {
                    if (fpBuffer != null) {
                        params.put("sample", fpBuffer);
                        params.put("sample_bytes", fpBuffer.length + "");
                    }
                    if (hummingFpBuffer != null) {
                        params.put("sample_hum", hummingFpBuffer);
                        params.put("sample_hum_bytes", hummingFpBuffer.length + "");
                        break;
                    }
                }
                return null;
            default:
                ACRCloudLogger.m27e(TAG, "engine type error " + engineType);
                return null;
        }
        params.put("pcm_bytes", bufferLen + "");
        params.put("fp_time", fpTime + "");
        params.put("rec_type", serviceType + "");
        params.put("action", "rec");
        params.put("dk", this.mACRCloudId);
        return params;
    }

    private Map<String, Object> preProcess(Map<String, Object> params) {
        for (String key : params.keySet()) {
            Object value = params.get(key);
            if (value instanceof String) {
                String sValue = ACRCloudRecognizeEngine.niceEnc((String) value, this.mConfig.accessSecret);
                ACRCloudLogger.m26d(TAG, key + " : " + value + " : " + sValue);
                if (sValue != null) {
                    params.put(key, sValue);
                }
            }
        }
        return params;
    }

    public void init() throws ACRCloudException {
    }

    public void release() {
    }

    public ACRCloudResponse startRecognize(Map<String, String> userParams) {
        Map<String, Object> params = getInitParams();
        if (userParams != null) {
            for (String key : userParams.keySet()) {
                params.put(key, (String) userParams.get(key));
            }
        }
        preProcess(params);
        ACRCloudException ex = null;
        int i = 0;
        while (i < this.retry) {
            try {
                return ACRCloudJsonWrapper.parse(ACRCloudHttpWrapper.doPost(getURL(this.action + this.mConfig.accessKey), params, this.mConfig.requestTimeout), 0);
            } catch (ACRCloudException e) {
                ex = e;
                i++;
            }
        }
        ACRCloudResponse res = new ACRCloudResponse();
        res.setStatusCode(ex.getCode());
        res.setStatusMsg(ex.getErrorMsg());
        res.setResult(ex.toString());
        return res;
    }

    public ACRCloudResponse resumeRecognize(byte[] buffer, int bufferLen, Map<String, Object> configParams, Map<String, String> userParams, int engineType) {
        long currentTimeMS = System.currentTimeMillis();
        ACRCloudResponse re;
        if (engineType < 0 || engineType > 3) {
            re = new ACRCloudResponse();
            re.setResult(ACRCloudException.toErrorString(ACRCloudException.ENGINE_TYPE_ERROR));
            return re;
        }
        Map<String, Object> params = getRecParams(buffer, bufferLen, configParams, engineType);
        if (params == null) {
            re = new ACRCloudResponse();
            re.setResult(ACRCloudException.toErrorString(ACRCloudException.GEN_FP_ERROR));
            return re;
        }
        ACRCloudResponse res;
        if (userParams != null) {
            for (String key : userParams.keySet()) {
                params.put(key, (String) userParams.get(key));
            }
        }
        preProcess(params);
        String str = "";
        ACRCloudException ex = null;
        int i = 0;
        while (i < this.retry) {
            try {
                str = ACRCloudHttpWrapper.doPost(getURL(this.action + this.mConfig.accessKey), params, this.mConfig.requestTimeout);
                long offsetCorrValue = System.currentTimeMillis() - currentTimeMS;
                ACRCloudLogger.m26d(TAG, "offsetCorrValue=" + offsetCorrValue);
                res = ACRCloudJsonWrapper.parse(str, offsetCorrValue);
                res.setExtFingerprint((byte[]) params.get("sample"));
                return res;
            } catch (ACRCloudException e) {
                ex = e;
                i++;
            }
        }
        res = new ACRCloudResponse();
        res.setStatusCode(ex.getCode());
        res.setStatusMsg(ex.getErrorMsg());
        res.setResult(ex.toString());
        return res;
    }

    private String encryptByHMACSHA1(byte[] data, byte[] key) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            return Base64.encodeToString(mac.doFinal(data), 0);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getUTCTimeSeconds() {
        Calendar cal = Calendar.getInstance();
        cal.add(14, -(cal.get(15) + cal.get(16)));
        return (cal.getTimeInMillis() / 1000) + "";
    }

    public String recognize(byte[] buffer, int bufferLen, Map<String, String> userParams, boolean isAudio) {
        String res = null;
        byte[] fps = buffer;
        if (isAudio) {
            fps = ACRCloudRecognizeEngine.genFP(buffer, bufferLen);
        }
        if (fps == null) {
            return ACRCloudException.toErrorString(ACRCloudException.GEN_FP_ERROR);
        }
        String method = ACRCloudHttpWrapperImpl.HTTP_METHOD_POST;
        String httpURL = "/v1/identify";
        String dataType = "fingerprint";
        String sigVersion = "1";
        String timestamp = getUTCTimeSeconds();
        String reqURL = getURL(httpURL);
        String signature = encryptByHMACSHA1((method + "\n" + httpURL + "\n" + this.mConfig.accessKey + "\n" + dataType + "\n" + sigVersion + "\n" + timestamp).getBytes(), this.mConfig.accessSecret.getBytes());
        Map<String, Object> postParams = new HashMap();
        postParams.put("access_key", this.mConfig.accessKey);
        postParams.put("sample_bytes", fps.length + "");
        postParams.put("sample", fps);
        postParams.put("uuid", this.mACRCloudId);
        postParams.put("timestamp", timestamp);
        postParams.put("signature", signature);
        postParams.put("data_type", dataType);
        postParams.put("signature_version", sigVersion);
        if (userParams != null) {
            for (String key : userParams.keySet()) {
                postParams.put(key, (String) userParams.get(key));
            }
        }
		try {
            res = ACRCloudHttpWrapper.doPost(reqURL, postParams, this.mConfig.requestTimeout);
		} catch (ACRCloudException e) {
		}
		try {
            JSONObject jSONObject = new JSONObject(res);
		} catch (JSONException ex) {
		}
        return res;
    }
}
