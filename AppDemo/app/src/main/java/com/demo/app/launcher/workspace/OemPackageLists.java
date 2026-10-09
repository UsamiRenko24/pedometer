package com.demo.app.launcher.workspace;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.LinkedHashSet;

import m9e778y.LauncherSDK;

/**
 * 按设备品牌组装候选包名列表：品牌专属包名优先，再接通用（COMMON）列表。
 */
final class OemPackageLists {

    private OemPackageLists() {
    }

    @NonNull
    static LauncherSDK.WorkspaceAppShortcut[] resolveOrderedLaunchablePackages(
            @NonNull Context context) {
        String manufacturer = normalize(Build.MANUFACTURER);
        String brand = normalize(Build.BRAND);
        LinkedHashSet<LauncherSDK.WorkspaceAppShortcut> apps = new LinkedHashSet<>();
        for (OemAppType type : OemAppType.values()) {
            addFirstLaunchable(
                    context, apps, type, type.resolvePackages(manufacturer, brand));
        }
        return apps.toArray(new LauncherSDK.WorkspaceAppShortcut[0]);
    }

    @Nullable
    static String resolveLaunchablePackage(
            @NonNull Context context,
            @NonNull OemAppType type) {
        String manufacturer = normalize(Build.MANUFACTURER);
        String brand = normalize(Build.BRAND);
        return LauncherSDK.resolveFirstLaunchablePackage(
                context, type.resolvePackages(manufacturer, brand));
    }

    private static void addFirstLaunchable(
            @NonNull Context context,
            @NonNull LinkedHashSet<LauncherSDK.WorkspaceAppShortcut> out,
            @NonNull OemAppType type,
            @NonNull String[] candidates) {
        String pkg = LauncherSDK.resolveFirstLaunchablePackage(context, candidates);
        if (pkg != null) {
            out.add(new LauncherSDK.WorkspaceAppShortcut(type.shortcutId(), pkg));
        }
    }

    @NonNull
    private static String normalize(@Nullable String value) {
        return value != null ? value.toLowerCase() : "";
    }
}
