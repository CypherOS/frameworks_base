package com.acrcloud.rec.sdk.utils;

import java.util.Map;

public class ACRCloudHttpWrapper {
    private static IACRCloudHttpWrapper httpWrapper = new ACRCloudHttpWrapperImpl();

    public static String doPost(String posturl, Map<String, Object> params, int timeOut) throws ACRCloudException {
        return httpWrapper.doPost(posturl, params, timeOut);
    }

    public static String doGet(String url, int timeout) throws ACRCloudException {
        return httpWrapper.doGet(url, timeout);
    }
}
