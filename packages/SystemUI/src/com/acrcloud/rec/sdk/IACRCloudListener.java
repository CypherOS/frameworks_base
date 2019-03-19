package com.acrcloud.rec.sdk;

public interface IACRCloudListener {
    void onResult(String str);

    void onVolumeChanged(double d);
}
