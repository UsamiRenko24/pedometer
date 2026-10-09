package com.demo.app.launcher.workspace;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import m9e778y.LauncherSDK;

/**
 * 默认布局中 OEM 系统应用的统一入口：解析顺序由 {@link OemAppType} 声明顺序决定，
 * HotSeat 与桌面文件夹共用同一套列表。
 */
final class OemDefaultWorkspaceApps {

    /**
     * HotSeat 前若干位预留给 OEM 系统应用（电话 / 短信 / 相机 / 浏览器）。
     */
    static final int HOTSEAT_SYSTEM_APP_SLOTS = 4;

    private static final OemAppType[] HOTSEAT_SYSTEM_APP_TYPES = {
            OemAppType.PHONE, OemAppType.SMS, OemAppType.BROWSER, OemAppType.CAMERA
    };

    @Nullable
    private static volatile LauncherSDK.WorkspaceAppShortcut[] sResolvedOrderedApps;

    private OemDefaultWorkspaceApps() {
    }

    /**
     * 解析 OEM 默认应用（含可启动性校验与 HotSeat shortcutId），同一次首次布局内只应调用一次。
     */
    @NonNull
    static LauncherSDK.WorkspaceAppShortcut[] resolveOrderedLaunchableOnce(
            @NonNull Context context) {
        LauncherSDK.WorkspaceAppShortcut[] cached = sResolvedOrderedApps;
        if (cached != null) {
            return cached;
        }
        synchronized (OemDefaultWorkspaceApps.class) {
            cached = sResolvedOrderedApps;
            if (cached == null) {
                cached = OemPackageLists.resolveOrderedLaunchablePackages(context);
                sResolvedOrderedApps = cached;
            }
            return cached;
        }
    }

    /**
     * 向 HotSeat 固定 rank 0–3 补全电话、短信、相机、浏览器（不覆盖已有图标；未安装则留空）。
     *
     * @param orderedApps 已由 {@link #resolveOrderedLaunchableOnce} 解析的应用列表
     * @return 补全后 HotSeat 上已有的应用包名集合，供文件夹排除使用
     */
    @NonNull
    static Set<String> populateHotSeat(
            @NonNull Context launcherContext,
            @NonNull LauncherSDK.WorkspaceAppShortcut[] orderedApps) {
        int numIcons = LauncherSDK.getNumHotSeatIcons(launcherContext);
        if (numIcons <= 0) {
            return Collections.emptySet();
        }

        Set<String> usedPackages = collectHotSeatPackages(launcherContext, numIcons);
        for (int rank = 0; rank < HOTSEAT_SYSTEM_APP_SLOTS && rank < numIcons; rank++) {
            if (LauncherSDK.isHotSeatAllAppsRank(launcherContext, rank)
                    || LauncherSDK.hasHotSeatShortcutAt(launcherContext, rank)) {
                continue;
            }
            LauncherSDK.WorkspaceAppShortcut app = findAppByShortcutId(
                    orderedApps, HOTSEAT_SYSTEM_APP_TYPES[rank].shortcutId());
            if (app == null || usedPackages.contains(app.packageName)) {
                continue;
            }
            if (LauncherSDK.addHotSeatShortcut(launcherContext, rank, app)) {
                usedPackages.add(app.packageName);
            }
        }
        return usedPackages;
    }

    @Nullable
    static LauncherSDK.WorkspaceAppShortcut findAppByShortcutId(
            @NonNull LauncherSDK.WorkspaceAppShortcut[] orderedApps,
            @NonNull String shortcutId) {
        for (LauncherSDK.WorkspaceAppShortcut app : orderedApps) {
            if (shortcutId.equals(app.shortcutId)) {
                return app;
            }
        }
        return null;
    }

    /**
     * 供桌面「系统应用」文件夹使用的应用列表：保持类型优先级，并排除指定包名。
     */
    @NonNull
    static LauncherSDK.WorkspaceAppShortcut[] appsForFolder(
            @NonNull LauncherSDK.WorkspaceAppShortcut[] orderedApps,
            @NonNull Set<String> excludePackages) {
        if (excludePackages.isEmpty()) {
            return orderedApps;
        }
        List<LauncherSDK.WorkspaceAppShortcut> result = new ArrayList<>(orderedApps.length);
        for (LauncherSDK.WorkspaceAppShortcut app : orderedApps) {
            if (!excludePackages.contains(app.packageName)) {
                result.add(app);
            }
        }
        return result.toArray(new LauncherSDK.WorkspaceAppShortcut[0]);
    }

    /**
     * 「系统应用」文件夹列表：排除 HotSeat 固定槽位（电话 / 短信 / 相机 / 浏览器）对应包名。
     * 与 {@link #populateHotSeat} 意图一致，可在 HotSeat 落库前独立计算，供第二、三组并行使用。
     */
    @NonNull
    static LauncherSDK.WorkspaceAppShortcut[] appsForSystemAppsFolder(
            @NonNull LauncherSDK.WorkspaceAppShortcut[] orderedApps) {
        return appsForFolder(orderedApps, hotSeatSystemAppPackages(orderedApps));
    }

    /**
     * HotSeat 固定系统应用槽位（rank 0–3）在解析结果中对应的包名。
     */
    @NonNull
    static Set<String> hotSeatSystemAppPackages(
            @NonNull LauncherSDK.WorkspaceAppShortcut[] orderedApps) {
        Set<String> packages = new HashSet<>();
        for (OemAppType type : HOTSEAT_SYSTEM_APP_TYPES) {
            LauncherSDK.WorkspaceAppShortcut app = findAppByShortcutId(
                    orderedApps, type.shortcutId());
            if (app != null) {
                packages.add(app.packageName);
            }
        }
        return packages;
    }

    /**
     * 不应再放入谷歌文件夹的包名：HotSeat 固定槽位包名 + 「系统应用」文件夹内 OEM 应用。
     */
    @NonNull
    static Set<String> packagesExcludedFromGoogleFolder(
            @NonNull LauncherSDK.WorkspaceAppShortcut[] orderedApps,
            @NonNull LauncherSDK.WorkspaceAppShortcut[] systemAppsFolderApps) {
        Set<String> exclude = new HashSet<>(hotSeatSystemAppPackages(orderedApps));
        for (LauncherSDK.WorkspaceAppShortcut app : systemAppsFolderApps) {
            exclude.add(app.packageName);
        }
        return exclude;
    }

    @NonNull
    private static Set<String> collectHotSeatPackages(
            @NonNull Context launcherContext, int numIcons) {
        Set<String> packages = new HashSet<>();
        for (int rank = 0; rank < numIcons; rank++) {
            if (LauncherSDK.isHotSeatAllAppsRank(launcherContext, rank)) {
                continue;
            }
            String pkg = LauncherSDK.getHotSeatShortcutPackage(launcherContext, rank);
            if (pkg != null) {
                packages.add(pkg);
            }
        }
        return packages;
    }
}
