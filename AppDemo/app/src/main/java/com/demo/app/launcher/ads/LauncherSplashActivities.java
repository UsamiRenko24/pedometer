package com.demo.app.launcher.ads;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.demo.app.R;

/** 内置应用 Launcher 页统一开屏占位。 */
public final class LauncherSplashActivities {

    private LauncherSplashActivities() {
    }

    public static void onCreate(@NonNull Activity activity) {
        LauncherSplashPlaceholder.show(activity, R.id.launcher_flSplash);
    }

    public static void beforeOpenHome(@NonNull Activity activity) {
        LauncherSplashPlaceholder.dismiss(activity, R.id.launcher_flSplash);
    }
}
