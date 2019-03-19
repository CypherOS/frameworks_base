package com.acrcloud.rec.sdk.utils;

import com.acrcloud.rec.engine.ACRCloudEngineResult;
import org.json.JSONArray;
import org.json.JSONObject;

public class ACRCloudJsonWrapperImpl implements IACRCloudJsonWrapper {
    private static final String TAG = "ACRCloudWorker";

    public ACRCloudResponse parse(String result, long offsetCorrectValue) throws ACRCloudException {
        if (result == null) {
            return null;
        }
        ACRCloudResponse r = new ACRCloudResponse();
        try {
            JSONObject resp = new JSONObject(result);
            JSONObject aStatus = resp.getJSONObject("status");
            r.setStatusCode(aStatus.getInt("code"));
            r.setStatusMsg(aStatus.getString("msg"));
            r.setStatusVersion(aStatus.getString("version"));
            if (resp.has("fp_time")) {
                r.setFpTime(resp.getInt("fp_time"));
            }
            if (resp.has("engine_type")) {
                r.setEngineType(resp.getInt("engine_type"));
            }
            if (resp.has("ekey")) {
                r.seteKey(resp.getString("ekey"));
            }
            if (resp.has("service_type")) {
                r.setServiceType(resp.getInt("service_type"));
            }
            if (resp.has("result_type")) {
                r.setResultType(resp.getInt("result_type"));
            }
            if (offsetCorrectValue != 0 && resp.has("metadata")) {
                JSONObject metadataRoot = resp.getJSONObject("metadata");
                int i;
                JSONObject jt;
                if (metadataRoot.has("custom_files")) {
                    JSONArray customFiles = metadataRoot.getJSONArray("custom_files");
                    for (i = 0; i < customFiles.length(); i++) {
                        jt = customFiles.getJSONObject(i);
                        if (jt.has("play_offset_ms")) {
                            jt.put("play_offset_ms", ((long) jt.getInt("play_offset_ms")) + offsetCorrectValue);
                        }
                    }
                } else if (metadataRoot.has("music")) {
                    JSONArray music = metadataRoot.getJSONArray("music");
                    for (i = 0; i < music.length(); i++) {
                        jt = music.getJSONObject(i);
                        if (jt.has("play_offset_ms")) {
                            jt.put("play_offset_ms", ((long) jt.getInt("play_offset_ms")) + offsetCorrectValue);
                        }
                    }
                }
                result = resp.toString();
            }
            r.setResult(result);
            return r;
        } catch (Exception e) {
            throw new ACRCloudException(ACRCloudException.JSON_ERROR, e.getMessage() + "; src result: " + result);
        }
    }

    public String parse(ACRCloudResponse result) {
        if (result == null) {
            return null;
        }
        if (result.getResult() != null && !"".equals(result.getResult())) {
            return result.getResult();
        }
        String r = "";
        try {
            JSONObject jroot = new JSONObject();
            JSONObject jStatus = new JSONObject();
            jStatus.put("code", result.getStatusCode());
            jStatus.put("msg", result.getStatusMsg());
            jStatus.put("version", result.getStatusVersion());
            ACRCloudEngineResult[] lr = result.getEngineResults();
            if (lr == null) {
                return ACRCloudException.toErrorString(1001);
            }
            JSONObject jMetadata = new JSONObject();
            JSONArray jMetainfo = new JSONArray();
            for (int i = 0; i < lr.length; i++) {
                ACRCloudLogger.m27e(TAG, lr[i].getMetainfo());
                JSONObject tr = new JSONObject(lr[i].getMetainfo());
                if (result.getOffsetCorrectValue() != 0) {
                    tr.put("play_offset_ms", ((long) lr[i].getOffsetTime()) + result.getOffsetCorrectValue());
                }
                jMetainfo.put(tr);
            }
            jMetadata.put("custom_files", jMetainfo);
            jroot.put("metadata", jMetadata);
            jroot.put("status", jStatus);
            jroot.put("result_type", result.getServiceType());
            return jroot.toString();
        } catch (Exception e) {
            return new ACRCloudException(ACRCloudException.JSON_ERROR, e.getMessage()).toString();
        }
    }

    public String parse(ACRCloudException error) {
        if (error == null) {
            return null;
        }
        String r = "";
        try {
            JSONObject jroot = new JSONObject();
            JSONObject jStatus = new JSONObject();
            jStatus.put("code", error.getCode());
            jStatus.put("msg", error.getErrorMsg());
            jStatus.put("version", "1.0");
            jroot.put("status", jStatus);
            return jroot.toString();
        } catch (Exception e) {
            return String.format("{\"status\":{\"code\":%d, \"msg\":\"%s\", \"version\":\"0.1\"}}", new Object[]{Integer.valueOf(error.getCode()), ACRCloudException.getErrorMsg(error.getCode())});
        }
    }
}
