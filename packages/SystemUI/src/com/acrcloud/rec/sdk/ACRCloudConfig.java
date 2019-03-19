package com.acrcloud.rec.sdk;

import android.content.Context;

public class ACRCloudConfig {
    public String accessKey = "";
    public String accessSecret = "";
    public IACRCloudListener acrcloudListener = null;
    public IACRCloudResultWithAudioListener acrcloudResultWithAudioListener = null;
    public int audioRecordSource = 1;
    public Context context = null;
    public String dbPath = "";
    public String host = "";
    public ACRCloudNetworkProtocol protocol = ACRCloudNetworkProtocol.PROTOCOL_HTTP;
    public ACRCloudRecMode reqMode = ACRCloudRecMode.REC_MODE_REMOTE;
    public int requestTimeout = 5000;

    public enum ACRCloudNetworkProtocol {
        PROTOCOL_HTTP,
        PROTOCOL_HTTPS
    }

    public enum ACRCloudRecMode {
        REC_MODE_REMOTE,
        REC_MODE_LOCAL,
        REC_MODE_BOTH
    }

    public enum ACRCloudResultType {
        RESULT_TYPE_NONE,
        RESULT_TYPE_AUDIO,
        RESULT_TYPE_LIVE,
        RESULT_TYPE_AUDIO_LIVE
    }
}
