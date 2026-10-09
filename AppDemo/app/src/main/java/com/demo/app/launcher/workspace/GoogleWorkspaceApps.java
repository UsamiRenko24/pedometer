package com.demo.app.launcher.workspace;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import m9e778y.LauncherSDK;

/** 已安装的 Google 应用，放进 0 屏的 Google 文件夹。 */
final class GoogleWorkspaceApps {

    private static final String SHORTCUT_ID_PREFIX = "workspace.shortcut.google.";

    private static final String[] PREFERRED_PACKAGES = {
            "com.android.vending",
            "com.google.android.googlequicksearchbox",
            "com.google.android.gm",
            "com.google.android.apps.maps",
            "com.google.android.youtube",
            "com.google.android.apps.photos",
            "com.google.android.chrome",
            "com.google.android.apps.docs",
            "com.google.android.calendar",
            "com.google.android.apps.messaging",
            "com.google.android.dialer",
    };

    private GoogleWorkspaceApps() {
    }

    @NonNull
    static LauncherSDK.WorkspaceAppShortcut[] resolveInstalled(
            @NonNull Context context,
            @NonNull Set<String> excludePackages) {
        Set<String> installed = queryInstalledGooglePackages(context);
        LinkedHashSet<String> ordered = new LinkedHashSet<>();
        for (String pkg : PREFERRED_PACKAGES) {
            if (installed.contains(pkg) && !excludePackages.contains(pkg)) {
                ordered.add(pkg);
            }
        }
        List<String> rest = new ArrayList<>();
        for (String pkg : installed) {
            if (!ordered.contains(pkg) && !excludePackages.contains(pkg)) {
                rest.add(pkg);
            }
        }
        rest.sort(String::compareTo);
        ordered.addAll(rest);

        LauncherSDK.WorkspaceAppShortcut[] shortcuts =
                new LauncherSDK.WorkspaceAppShortcut[ordered.size()];
        int i = 0;
        for (String pkg : ordered) {
            shortcuts[i++] = new LauncherSDK.WorkspaceAppShortcut(
                    SHORTCUT_ID_PREFIX + pkg.toLowerCase(Locale.US), pkg);
        }
        return shortcuts;
    }

    @NonNull
    private static Set<String> queryInstalledGooglePackages(@NonNull Context context) {
        PackageManager pm = context.getPackageManager();
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> activities = pm.queryIntentActivities(launcherIntent, 0);
        Set<String> packages = new HashSet<>();
        for (ResolveInfo info : activities) {
            if (info.activityInfo == null || info.activityInfo.packageName == null) {
                continue;
            }
            String pkg = info.activityInfo.packageName;
            if ((pkg.startsWith("com.google.") || "com.android.vending".equals(pkg))
                    && LauncherSDK.resolveFirstLaunchablePackage(context, new String[]{pkg}) != null) {
                packages.add(pkg);
            }
        }
        return packages;
    }
}
