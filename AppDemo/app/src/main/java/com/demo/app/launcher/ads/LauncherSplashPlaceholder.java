package com.demo.app.launcher.ads;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.app.R;

/** RC100 各 {@code *_flSplash} / {@code launcher_flSplash} 开屏容器占位。 */
public final class LauncherSplashPlaceholder {

    private LauncherSplashPlaceholder() {
    }

    public static void show(@NonNull Activity activity, @IdRes int containerId) {
        if (!LauncherAds.shouldShowPlaceholder()) {
            return;
        }
        FrameLayout container = activity.findViewById(containerId);
        if (container == null) {
            return;
        }
        container.removeAllViews();
        LayoutInflater.from(activity).inflate(R.layout.include_overlay_splash_ad, container, true);
        container.setVisibility(View.VISIBLE);
        container.bringToFront();
    }

    public static void dismiss(@NonNull Activity activity, @IdRes int containerId) {
        FrameLayout container = activity.findViewById(containerId);
        if (container == null) {
            return;
        }
        container.setVisibility(View.GONE);
        container.removeAllViews();
    }

    public static void bindRootIfPresent(@NonNull Activity activity) {
        show(activity, R.id.launcher_flSplash);
        show(activity, R.id.pdfLauncher_flSplash);
        show(activity, R.id.splash_flSplash);
        show(activity, R.id.launcherGuide_flSplash);
    }

    public static void dismissRootIfPresent(@NonNull Activity activity) {
        dismiss(activity, R.id.launcher_flSplash);
        dismiss(activity, R.id.pdfLauncher_flSplash);
        dismiss(activity, R.id.splash_flSplash);
        dismiss(activity, R.id.launcherGuide_flSplash);
    }

    @Nullable
    public static FrameLayout findSplashContainer(@NonNull Activity activity, @IdRes int primaryId) {
        FrameLayout container = activity.findViewById(primaryId);
        if (container != null) {
            return container;
        }
        View view = activity.findViewById(R.id.launcher_flSplash);
        if (view instanceof FrameLayout) {
            return (FrameLayout) view;
        }
        return null;
    }
}
