package com.demo.app.utils;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.demo.app.data.ReportEventConstants;

public final class FirebaseUtils {
    private static boolean isInitialized = false;

    /**
     * 初始化
     */
    public static void init(@NonNull Context context) {
        try {
            FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(true);
            isInitialized = true;
        } catch (Throwable ignored) {
        }
        try {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        } catch (Throwable ignored) {
        }
        try {
            FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        } catch (Throwable ignored) {
        }
    }

    /**
     * 上报事件
     */
    public static void reportEvent(@NonNull String eventName) {
        reportEvent(eventName, null);
    }

    /**
     * 上报事件
     */
    public static void reportEvent(@NonNull String eventName, String value) {
        if (!isInitialized) {
            return;
        }
        try {
            final Context context = Utils.getApp();
            final Bundle bundle = new Bundle();
            if (value != null) {
                bundle.putString(eventName, value);
            }
            FirebaseAnalytics.getInstance(context).logEvent(eventName, bundle);
        } catch (Throwable ignored) {
        }
    }

    /**
     * 上报广告展示信息
     */
    public static void reportAdImpress(@NonNull Bundle bundle) {
        if (!isInitialized) {
            return;
        }
        try {
            final Context context = Utils.getApp();
            FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.AD_IMPRESSION, bundle);
        } catch (Throwable ignored) {
        }
    }

    /**
     * 上报广告收入信息
     */
    public static void reportAdRevenue(@NonNull Bundle bundle) {
        if (!isInitialized) {
            return;
        }
        try {
            final Context context = Utils.getApp();
            FirebaseAnalytics.getInstance(context).logEvent(ReportEventConstants.KEY_AD_REVENUE, bundle);
        } catch (Throwable ignored) {
        }
    }

    /**
     * 上报崩溃日记
     */
    public static void recordException(Throwable throwable) {
        if (!isInitialized) {
            return;
        }
        try {
            if (throwable == null) return;
            FirebaseCrashlytics.getInstance().recordException(throwable);
        } catch (Throwable ignored) {
        }
    }
}