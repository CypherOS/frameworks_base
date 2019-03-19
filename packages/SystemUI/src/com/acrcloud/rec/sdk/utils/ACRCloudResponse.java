package com.acrcloud.rec.sdk.utils;

import com.acrcloud.rec.engine.ACRCloudEngineResult;
import com.acrcloud.rec.sdk.ACRCloudConfig.ACRCloudResultType;

public class ACRCloudResponse {
    private String eKey = "";
    private ACRCloudEngineResult[] engineResults = null;
    private int engineType = 0;
    private byte[] extFingerprint = null;
    private int fpTime = 0;
    private byte[] humFingerprint = null;
    private long offsetCorrectValue = 0;
    private String result = "";
    private int resultType = 0;
    private int serviceType = ACRCloudResultType.RESULT_TYPE_AUDIO.ordinal();
    private int statusCode = 0;
    private String statusMsg = "Success";
    private String statusVersion = "1.0";

    public byte[] getExtFingerprint() {
        return this.extFingerprint;
    }

    public void setExtFingerprint(byte[] extFingerprint) {
        this.extFingerprint = extFingerprint;
    }

    public byte[] getHumFingerprint() {
        return this.humFingerprint;
    }

    public void setHumFingerprint(byte[] humFingerprint) {
        this.humFingerprint = humFingerprint;
    }

    public long getOffsetCorrectValue() {
        return this.offsetCorrectValue;
    }

    public void setOffsetCorrectValue(long offset) {
        this.offsetCorrectValue = offset;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getEngineType() {
        return this.engineType;
    }

    public void setEngineType(int engineType) {
        this.engineType = engineType;
    }

    public String getStatusMsg() {
        return this.statusMsg;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }

    public String getStatusVersion() {
        return this.statusVersion;
    }

    public void setStatusVersion(String statusVersion) {
        this.statusVersion = statusVersion;
    }

    public int getFpTime() {
        return this.fpTime;
    }

    public void setFpTime(int fpTime) {
        this.fpTime = fpTime;
    }

    public String geteKey() {
        return this.eKey;
    }

    public void seteKey(String eKey) {
        this.eKey = eKey;
    }

    public String getResult() {
        return this.result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public ACRCloudEngineResult[] getEngineResults() {
        return this.engineResults;
    }

    public void setEngineResults(ACRCloudEngineResult[] engineResults) {
        this.engineResults = engineResults;
    }

    public int getServiceType() {
        return this.serviceType;
    }

    public void setServiceType(int serviceType) {
        this.serviceType = serviceType;
    }

    public int getResultType() {
        return this.resultType;
    }

    public void setResultType(int resultType) {
        this.resultType = resultType;
    }
}
