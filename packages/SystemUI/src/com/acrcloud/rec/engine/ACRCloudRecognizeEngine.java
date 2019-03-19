package com.acrcloud.rec.engine;

public class ACRCloudRecognizeEngine {
    private long mNativeEngineId = 0;

    private native void native_engine_finalizer(long j);

    private native long native_engine_init(String str);

    private native ACRCloudEngineResult[] native_engine_recognize(long j, byte[] bArr, int i, int i2);

    private static native byte[] native_gen_fp(byte[] bArr, int i, String str, String str2);

    private static native byte[] native_gen_hum_fp(byte[] bArr, int i);

    private static native byte[] native_nice_enc(byte[] bArr, int i, byte[] bArr2, int i2);

    private static native byte[] native_resample(byte[] bArr, int i, int i2, int i3, int i4, int i5);

    static {
        try {
            System.loadLibrary("ACRCloudEngine");
        } catch (Exception e) {
            System.err.println("ACRCloudEngine loadLibrary error!");
        }
    }

    public boolean init(String DBPath) {
        if (DBPath == null || "".equals(DBPath)) {
            return false;
        }
        this.mNativeEngineId = native_engine_init(DBPath);
        if (this.mNativeEngineId != 0) {
            return true;
        }
        return false;
    }

    public void release() {
        if (this.mNativeEngineId != 0) {
            native_engine_finalizer(this.mNativeEngineId);
        }
    }

    public ACRCloudEngineResult[] recognizePCM(byte[] buffer, int bufferLen) {
        if (buffer == null || bufferLen <= 0) {
            return null;
        }
        return native_engine_recognize(this.mNativeEngineId, buffer, bufferLen, 0);
    }

    public ACRCloudEngineResult[] recognizeFP(byte[] buffer, int bufferLen) {
        if (buffer == null || bufferLen <= 0) {
            return null;
        }
        return native_engine_recognize(this.mNativeEngineId, buffer, bufferLen, 1);
    }

    public static byte[] genFP(byte[] buffer, int bufferLen, String ekey, String skey) {
        if (buffer == null || bufferLen <= 0) {
            return null;
        }
        return native_gen_fp(buffer, bufferLen, ekey, skey);
    }

    public static byte[] genFP(byte[] buffer, int bufferLen) {
        if (buffer == null || bufferLen <= 0) {
            return null;
        }
        return native_gen_fp(buffer, bufferLen, null, null);
    }

    public static byte[] resample(byte[] buffer, int bufferLen, int sampleRate, int nChannels, int nBit, int isFloat) {
        if (buffer == null || bufferLen <= 0) {
            return null;
        }
        return native_resample(buffer, bufferLen, sampleRate, nChannels, nBit, isFloat);
    }

    public static byte[] genHumFP(byte[] buffer, int bufferLen) {
        if (buffer == null || bufferLen <= 0) {
            return null;
        }
        return native_gen_hum_fp(buffer, bufferLen);
    }

    public static String niceEnc(String value, String key) {
        if (value == null || "".equals(value)) {
            return null;
        }
        byte[] valueBytes = value.getBytes();
        byte[] keyBytes = key.getBytes();
        byte[] re = native_nice_enc(valueBytes, valueBytes.length, keyBytes, keyBytes.length);
        if (re != null) {
            return new String(re);
        }
        return null;
    }
}
