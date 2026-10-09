package com.demo.app.utils;

import android.util.Log;

/** 页面关键路径日志，过滤标签 AppDemo。 */
public final class AppLog {

    private static final String TAG = "AppDemo";

    private AppLog() {
    }

    public static void i(String where, String message) {
        Log.i(TAG, where + " | " + message);
    }
}
