package com.demo.app.utils;

import android.app.Activity;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * 系统栏沉浸式，与 Volume Control（{@link com.demo.app.MainActivity} + {@link com.demo.app.ui.activity.BaseActivity}）一致：
 * 隐藏状态栏与底部导航栏，边缘滑动可暂时唤出。
 */
public final class SystemUiHelper {

    private SystemUiHelper() {
    }

    /** 根布局 padding：隐藏底部导航后不再预留 navigationBar inset。 */
    public static int contentInsetTypes() {
        return WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.displayCutout();
    }

    public static int contentInsetTypesWithIme() {
        return contentInsetTypes() | WindowInsetsCompat.Type.ime();
    }

    public static void applyVolumeControlImmersive(@NonNull Activity activity) {
        applyVolumeControlImmersive(activity.getWindow());
    }

    public static void applyVolumeControlImmersive(@NonNull Window window) {
        applyVolumeControlImmersive(window, false, false);
    }

    public static void applyVolumeControlImmersive(@NonNull Window window,
            boolean lightStatusBars, boolean lightNavigationBars) {
        WindowCompat.setDecorFitsSystemWindows(window, false);
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        controller.setAppearanceLightStatusBars(lightStatusBars);
        controller.setAppearanceLightNavigationBars(lightNavigationBars);
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        controller.hide(WindowInsetsCompat.Type.systemBars());
    }
}
