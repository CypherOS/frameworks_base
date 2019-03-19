package com.acrcloud.rec.sdk;

public interface IACRCloudResultWithAudioListener {
    void onResult(ACRCloudResult aCRCloudResult);

    void onVolumeChanged(double d);
}
