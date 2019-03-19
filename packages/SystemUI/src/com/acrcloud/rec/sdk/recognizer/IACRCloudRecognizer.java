package com.acrcloud.rec.sdk.recognizer;

import com.acrcloud.rec.sdk.utils.ACRCloudException;
import com.acrcloud.rec.sdk.utils.ACRCloudResponse;
import java.util.Map;

public interface IACRCloudRecognizer {
    void init() throws ACRCloudException;

    String recognize(byte[] bArr, int i, Map<String, String> map, boolean z);

    void release();

    ACRCloudResponse resumeRecognize(byte[] bArr, int i, Map<String, Object> map, Map<String, String> map2, int i2);

    ACRCloudResponse startRecognize(Map<String, String> map);
}
