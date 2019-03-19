package com.acrcloud.rec.sdk.utils;

public interface IACRCloudJsonWrapper {
    ACRCloudResponse parse(String str, long j) throws ACRCloudException;

    String parse(ACRCloudException aCRCloudException);

    String parse(ACRCloudResponse aCRCloudResponse);
}
