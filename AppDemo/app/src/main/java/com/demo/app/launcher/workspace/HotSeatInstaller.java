package com.demo.app.launcher.workspace;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import m9e778y.LauncherSDK;

/** 首次默认布局时在 HotSeat 放置电话、短信、浏览器、相机。 */
public final class HotSeatInstaller {
    private static final ExecutorService PREP_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private HotSeatInstaller() {
    }

    public static void run(@NonNull Context launcherContext) {
        final Context appContext = launcherContext.getApplicationContext();
        CompletableFuture<LauncherSDK.WorkspaceAppShortcut[]> orderedFuture =
                CompletableFuture.supplyAsync(
                        () -> OemDefaultWorkspaceApps.resolveOrderedLaunchableOnce(appContext),
                        PREP_EXECUTOR);
        orderedFuture.whenComplete((orderedApps, throwable) -> {
            if (throwable != null || orderedApps == null) {
                return;
            }
            MAIN_HANDLER.post(() -> {
                try {
                    OemDefaultWorkspaceApps.populateHotSeat(launcherContext, orderedApps);
                } catch (Throwable ignored) {
                }
            });
        });
    }
}
