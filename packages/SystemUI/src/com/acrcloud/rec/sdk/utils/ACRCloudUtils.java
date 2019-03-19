package com.acrcloud.rec.sdk.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ACRCloudUtils {
    public static double computeDb(byte[] buffer, int len) {
        int size = len >> 3;
        float fDB = 0.0f;
        float mu = 0.0f;
        for (int i = 0; i < size; i++) {
            short retVal = (short) ((buffer[i << 3] & 255) | ((short) (buffer[(i << 3) + 1] << 8)));
            int temp = ((retVal >> 15) ^ retVal) - (retVal >> 15);
            fDB += (float) (temp * temp);
            mu += (float) temp;
        }
        mu /= (float) size;
        return Math.min(Math.log10((double) (((fDB / ((float) size)) - (mu * mu)) + 1.0f)), 8.0d) / 8.0d;
    }

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            NetworkInfo mNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x0058 A:{SYNTHETIC, Splitter:B:36:0x0058} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x005d A:{SYNTHETIC, Splitter:B:39:0x005d} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0042 A:{SYNTHETIC, Splitter:B:25:0x0042} */
    /* JADX WARNING: Removed duplicated region for block: B:57:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0047 A:{SYNTHETIC, Splitter:B:28:0x0047} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0058 A:{SYNTHETIC, Splitter:B:36:0x0058} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x005d A:{SYNTHETIC, Splitter:B:39:0x005d} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0042 A:{SYNTHETIC, Splitter:B:25:0x0042} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0047 A:{SYNTHETIC, Splitter:B:28:0x0047} */
    /* JADX WARNING: Removed duplicated region for block: B:57:? A:{SYNTHETIC, RETURN} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void createFileWithByte(byte[] bytes, String fileName) {
        Exception e;
        Throwable th;
        File file = new File(fileName);
        FileOutputStream outputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream outputStream2 = new FileOutputStream(file);
            try {
                BufferedOutputStream bufferedOutputStream2 = new BufferedOutputStream(outputStream2);
                try {
                    bufferedOutputStream2.write(bytes);
                    bufferedOutputStream2.flush();
                    if (outputStream2 != null) {
                        try {
                            outputStream2.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    if (bufferedOutputStream2 != null) {
                        try {
                            bufferedOutputStream2.close();
                            bufferedOutputStream = bufferedOutputStream2;
                            outputStream = outputStream2;
                            return;
                        } catch (Exception e22) {
                            e22.printStackTrace();
                            bufferedOutputStream = bufferedOutputStream2;
                            outputStream = outputStream2;
                            return;
                        }
                    }
                    outputStream = outputStream2;
                } catch (Exception e3) {
                    e = e3;
                    bufferedOutputStream = bufferedOutputStream2;
                    outputStream = outputStream2;
                    try {
                        e.printStackTrace();
                        if (outputStream != null) {
                        }
                        if (bufferedOutputStream != null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e23) {
                                e23.printStackTrace();
                            }
                        }
                        if (bufferedOutputStream != null) {
                            try {
                                bufferedOutputStream.close();
                            } catch (Exception e222) {
                                e222.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    bufferedOutputStream = bufferedOutputStream2;
                    outputStream = outputStream2;
                    if (outputStream != null) {
                    }
                    if (bufferedOutputStream != null) {
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e = e4;
                outputStream = outputStream2;
                e.printStackTrace();
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e232) {
                        e232.printStackTrace();
                    }
                }
                if (bufferedOutputStream != null) {
                    try {
                        bufferedOutputStream.close();
                    } catch (Exception e2222) {
                        e2222.printStackTrace();
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                outputStream = outputStream2;
                if (outputStream != null) {
                }
                if (bufferedOutputStream != null) {
                }
                throw th;
            }
        } catch (Exception e5) {
            e = e5;
            e.printStackTrace();
            if (outputStream != null) {
            }
            if (bufferedOutputStream != null) {
            }
        }
    }
}
