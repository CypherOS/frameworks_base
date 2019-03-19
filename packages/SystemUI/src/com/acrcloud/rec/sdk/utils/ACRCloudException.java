package com.acrcloud.rec.sdk.utils;

import java.util.HashMap;
import java.util.Map;

public class ACRCloudException extends Exception {
    public static final int ENGINE_TYPE_ERROR = 2006;
    private static final Map<Integer, String> ERROR_INFO_MAP = new C03291();
    public static final int GEN_FP_ERROR = 2004;
    public static final int HTTP_ERROR = 3000;
    public static final int HTTP_ERROR_TIMEOUT = 2005;
    public static final int INIT_ERROR = 2001;
    public static final int JSON_ERROR = 2002;
    public static final int NO_INIT_ERROR = 2003;
    public static final int NO_RESULT = 1001;
    public static final int RECORD_ERROR = 2000;
    public static final int RESAMPLE_ERROR = 2008;
    public static final int SUCCESS = 0;
    public static final int UNKNOW_ERROR = 2010;
    private static final long serialVersionUID = 1;
    private int code = 0;
    private String errorMsg = "";

    /* renamed from: com.acrcloud.rec.sdk.utils.ACRCloudException$1 */
    static class C03291 extends HashMap<Integer, String> {
        C03291() {
            put(Integer.valueOf(0), "Success");
            put(Integer.valueOf(1001), "No Result");
            put(Integer.valueOf(ACRCloudException.ENGINE_TYPE_ERROR), "Engine type error");
            put(Integer.valueOf(ACRCloudException.JSON_ERROR), "JSON error");
            put(Integer.valueOf(3000), "HTTP error");
            put(Integer.valueOf(ACRCloudException.HTTP_ERROR_TIMEOUT), "HTTP timeout error");
            put(Integer.valueOf(ACRCloudException.GEN_FP_ERROR), "Create none fingerprint: may be mute audio");
            put(Integer.valueOf(ACRCloudException.RECORD_ERROR), "Record error: may be no recording permission");
            put(Integer.valueOf(ACRCloudException.INIT_ERROR), "Init error");
            put(Integer.valueOf(ACRCloudException.UNKNOW_ERROR), "UnKnow error");
            put(Integer.valueOf(ACRCloudException.NO_INIT_ERROR), "No init error");
            put(Integer.valueOf(ACRCloudException.RESAMPLE_ERROR), "Resample audio error");
        }
    }

    public ACRCloudException(int code) {
        super("");
        this.code = code;
        String errorMainMsg = (String) ERROR_INFO_MAP.get(Integer.valueOf(code));
        if (errorMainMsg == null) {
            errorMainMsg = "unknow error";
        }
        this.errorMsg = errorMainMsg;
    }

    public ACRCloudException(int code, String msg) {
        super(msg);
        this.code = code;
        String errorMainMsg = (String) ERROR_INFO_MAP.get(Integer.valueOf(code));
        this.errorMsg = msg;
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getErrorMsg() {
        return this.errorMsg;
    }

    public static String getErrorMsg(int code) {
        String ms = (String) ERROR_INFO_MAP.get(Integer.valueOf(code));
        if (ms == null) {
            return "";
        }
        return ms;
    }

    public String toString() {
        return ACRCloudJsonWrapper.parse(this);
    }

    public static String toErrorString(int code, String msg) {
        String ms = (String) ERROR_INFO_MAP.get(Integer.valueOf(code));
        if (ms == null) {
            ms = "";
        }
        return ACRCloudJsonWrapper.parse(new ACRCloudException(code, ms + ":" + msg));
    }

    public static String toErrorString(int code) {
        String ms = (String) ERROR_INFO_MAP.get(Integer.valueOf(code));
        if (ms == null) {
            ms = "";
        }
        return ACRCloudJsonWrapper.parse(new ACRCloudException(code, ms));
    }
}
