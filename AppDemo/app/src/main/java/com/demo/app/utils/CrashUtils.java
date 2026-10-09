package com.demo.app.utils;

import android.os.Handler;
import android.os.Looper;

public final class CrashUtils {
    private static final Handler crashHandler = new Handler(Looper.getMainLooper());

    private static boolean javaCrashRunning = false;
    private static Runnable javaCodeCrashHandlerTask;

    public static void init() {
        try {
            Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                FirebaseUtils.recordException(throwable);

                if (thread == Looper.getMainLooper().getThread()) {
                    mainThreadCrashHandler();
                }
            });

            mainThreadCrashHandler();
        } catch (Throwable ignored) {
        }
    }

    private static void mainThreadCrashHandler() {
        if (javaCrashRunning) return;
        javaCrashRunning = true;
        crashHandler.post(getJavaCodeCrashHandlerTask());
    }

    private static Runnable getJavaCodeCrashHandlerTask() {
        if (javaCodeCrashHandlerTask != null) {
            return javaCodeCrashHandlerTask;
        }

        javaCodeCrashHandlerTask = () -> {
            while (javaCrashRunning) {
                try {
                    if (Looper.myLooper() == null) {
                        Looper.prepare();
                    }

                    Looper.loop();
                } catch (Throwable ignored) {
                }
            }
        };
        return javaCodeCrashHandlerTask;
    }
}