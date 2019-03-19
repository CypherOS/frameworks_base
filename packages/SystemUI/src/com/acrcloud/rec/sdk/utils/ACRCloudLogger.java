package com.acrcloud.rec.sdk.utils;

import android.util.Log;

public class ACRCloudLogger {
    public static boolean print = false;

    /* renamed from: v */
    public static void m29v(String tag, String msg) {
        if (print) {
            try {
                Log.v(tag, msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* renamed from: e */
    public static void m27e(String tag, String msg) {
        if (print) {
            try {
                Log.e(tag, msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* renamed from: i */
    public static void m28i(String tag, String msg) {
        if (print) {
            try {
                Log.i(tag, msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* renamed from: d */
    public static void m26d(String tag, String msg) {
        if (print) {
            try {
                Log.d(tag, msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
