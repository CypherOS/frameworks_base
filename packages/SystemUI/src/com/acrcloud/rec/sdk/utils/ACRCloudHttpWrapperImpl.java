package com.acrcloud.rec.sdk.utils;

import com.bumptech.glide.load.Key;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Map;

public class ACRCloudHttpWrapperImpl implements IACRCloudHttpWrapper {
    public static final String BOUNDARY = "--*****2015.03.30.acrcloud.rec.copyright*****\r\n";
    public static final String BOUNDARYSTR = "*****2015.03.30.acrcloud.rec.copyright*****";
    public static final String HTTP_METHOD_GET = "GET";
    public static final String HTTP_METHOD_POST = "POST";
    private static final String TAG = "ACRCloudHttpWrapperImpl";

    /* JADX WARNING: Unknown top exception splitter block from list: {B:22:0x00f3=Splitter:B:22:0x00f3, B:40:0x018e=Splitter:B:40:0x018e} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0106 A:{SYNTHETIC, Splitter:B:27:0x0106} */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x0270 A:{SYNTHETIC, Splitter:B:82:0x0270} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String doPost(String posturl, Map<String, Object> params, int timeout) throws ACRCloudException {
        IOException e;
        Exception e2;
        Throwable th;
        SocketTimeoutException e3;
        String res = "";
        try {
            URL url = new URL(posturl);
            try {
                ACRCloudLogger.m26d(TAG, posturl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);
                conn.setRequestMethod(HTTP_METHOD_POST);
                conn.setDoOutput(true);
                conn.setRequestProperty("Accept-Charset", "utf-8");
                conn.setRequestProperty("Connection", "keep-alive");
                conn.setRequestProperty("Content-type", "multipart/form-data;boundary=*****2015.03.30.acrcloud.rec.copyright*****");
                BufferedOutputStream out = null;
                try {
                    conn.connect();
                    BufferedOutputStream out2 = new BufferedOutputStream(conn.getOutputStream());
                    try {
                        StringBuilder reqData = new StringBuilder();
                        if (params != null) {
                            for (String key : params.keySet()) {
                                Object value = params.get(key);
                                reqData.setLength(0);
                                if ((value instanceof String) || (value instanceof Integer)) {
                                    reqData.append(BOUNDARY);
                                    reqData.append("Content-Disposition:form-data;name=\"");
                                    reqData.append(key);
                                    reqData.append("\"\r\n\r\n");
                                    reqData.append(value);
                                    reqData.append("\r\n");
                                    ACRCloudLogger.m26d(TAG, key + ":" + value);
                                    out2.write(reqData.toString().getBytes());
                                } else if (value instanceof byte[]) {
                                    reqData.append(BOUNDARY);
                                    reqData.append("Content-Disposition:form-data;");
                                    reqData.append("name=\"" + key + "\";");
                                    reqData.append("filename=\"janet.sig\"\r\n");
                                    reqData.append("Content-Type:application/octet-stream");
                                    reqData.append("\r\n\r\n");
                                    out2.write(reqData.toString().getBytes());
                                    out2.write((byte[]) value);
                                    out2.write("\r\n".getBytes());
                                }
                            }
                            out2.write("--*****2015.03.30.acrcloud.rec.copyright*****--\r\n\r\n".getBytes());
                        }
                        if (out2 != null) {
                            try {
                                out2.flush();
                                out2.close();
                            } catch (IOException e4) {
                                throw new ACRCloudException(3000, e4.getMessage());
                            }
                        }
                        BufferedReader reader = null;
                        try {
                            int response = conn.getResponseCode();
                            ACRCloudLogger.m27e(TAG, "" + response);
                            if (response == 200) {
                                BufferedReader reader2 = new BufferedReader(new InputStreamReader(conn.getInputStream(), Key.STRING_CHARSET_NAME));
                                try {
                                    String str = "";
                                    while (true) {
                                        str = reader2.readLine();
                                        if (str == null) {
                                            break;
                                        } else if (str.length() > 0) {
                                            res = res + str;
                                        }
                                    }
                                    ACRCloudLogger.m26d(TAG, res);
                                    if (reader2 != null) {
                                        try {
                                            reader2.close();
                                        } catch (IOException e42) {
                                            throw new ACRCloudException(3000, e42.getMessage());
                                        }
                                    }
                                    return res;
                                } catch (Exception e5) {
                                    e2 = e5;
                                    reader = reader2;
                                    try {
                                        throw new ACRCloudException(3000, e2.getMessage());
                                    } catch (Throwable th2) {
                                        th = th2;
                                        if (reader != null) {
                                        }
                                        throw th;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    reader = reader2;
                                    if (reader != null) {
                                        try {
                                            reader.close();
                                        } catch (IOException e422) {
                                            throw new ACRCloudException(3000, e422.getMessage());
                                        }
                                    }
                                    throw th;
                                }
                            }
                            throw new ACRCloudException(3000, "server response code error, code=" + response);
                        } catch (Exception e6) {
                            e2 = e6;
                            throw new ACRCloudException(3000, e2.getMessage());
                        }
                    } catch (SocketTimeoutException e7) {
                        e3 = e7;
                        out = out2;
                        try {
                            throw new ACRCloudException(ACRCloudException.HTTP_ERROR_TIMEOUT, e3.getMessage());
                        } catch (Throwable th4) {
                            th = th4;
                            if (out != null) {
                                try {
                                    out.flush();
                                    out.close();
                                } catch (IOException e4222) {
                                    throw new ACRCloudException(3000, e4222.getMessage());
                                }
                            }
                            throw th;
                        }
                    } catch (IOException e8) {
                        e4222 = e8;
                        out = out2;
                        throw new ACRCloudException(3000, e4222.getMessage());
                    } catch (Throwable th5) {
                        th = th5;
                        out = out2;
                        if (out != null) {
                        }
                        throw th;
                    }
                } catch (SocketTimeoutException e9) {
                    e3 = e9;
                    throw new ACRCloudException(ACRCloudException.HTTP_ERROR_TIMEOUT, e3.getMessage());
                } catch (IOException e10) {
                    e4222 = e10;
                    throw new ACRCloudException(3000, e4222.getMessage());
                }
            } catch (Exception e11) {
                e2 = e11;
                URL url2 = url;
                throw new ACRCloudException(3000, e2.getMessage());
            }
        } catch (Exception e12) {
            e2 = e12;
            throw new ACRCloudException(3000, e2.getMessage());
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0072 A:{SYNTHETIC, Splitter:B:20:0x0072} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String doGet(String url, int timeout) throws ACRCloudException {
        Exception e;
        Throwable th;
        String result = "";
        BufferedReader in = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setRequestMethod(HTTP_METHOD_GET);
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "close");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.connect();
            BufferedReader in2 = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while (true) {
                try {
                    String line = in2.readLine();
                    if (line == null) {
                        break;
                    }
                    result = result + line;
                } catch (Exception e2) {
                    e = e2;
                    in = in2;
                    try {
                        throw new ACRCloudException(3000, e.getMessage());
                    } catch (Throwable th2) {
                        th = th2;
                        if (in != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    in = in2;
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Exception e22) {
                            e22.printStackTrace();
                        }
                    }
                    throw th;
                }
            }
            if (in2 != null) {
                try {
                    in2.close();
                } catch (Exception e222) {
                    e222.printStackTrace();
                }
            }
            return result;
        } catch (Exception e3) {
            e = e3;
            throw new ACRCloudException(3000, e.getMessage());
        }
    }
}
