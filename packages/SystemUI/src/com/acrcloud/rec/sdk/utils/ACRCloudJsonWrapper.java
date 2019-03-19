package com.acrcloud.rec.sdk.utils;

public class ACRCloudJsonWrapper {
    private static IACRCloudJsonWrapper jsonObj = new ACRCloudJsonWrapperImpl();

    public static ACRCloudResponse parse(String result, long offsetCorrectValue) throws ACRCloudException {
        return jsonObj.parse(result, offsetCorrectValue);
    }

    public static String parse(ACRCloudResponse result) {
        return jsonObj.parse(result);
    }

    public static String parse(ACRCloudException error) {
        return jsonObj.parse(error);
    }
}
