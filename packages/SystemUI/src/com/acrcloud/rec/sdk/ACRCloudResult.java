package com.acrcloud.rec.sdk;

public class ACRCloudResult {
    private byte[] audioFingerprint = null;
    private byte[] recordDataPCM = null;
    private String result = null;

    public String getResult() {
        return this.result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public byte[] getRecordDataPCM() {
        return this.recordDataPCM;
    }

    public void setRecordDataPCM(byte[] recordDataPCM) {
        this.recordDataPCM = recordDataPCM;
    }

    public byte[] getAudioFingerprint() {
        return this.audioFingerprint;
    }

    public void setAudioFingerprint(byte[] audioFingerprint) {
        this.audioFingerprint = audioFingerprint;
    }

    public void reset() {
        this.result = null;
        this.recordDataPCM = null;
        this.audioFingerprint = null;
    }
}
