package com.acrcloud.rec.sdk.utils;

import java.util.Map;

public interface IACRCloudHttpWrapper {
    String doGet(String str, int i) throws ACRCloudException;

    String doPost(String str, Map<String, Object> map, int i) throws ACRCloudException;
}
