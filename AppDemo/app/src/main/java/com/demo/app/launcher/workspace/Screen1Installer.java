package com.demo.app.launcher.workspace;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.app.R;

import m9e778y.LauncherSDK;

/**
 * 1 屏默认布局。
 * 有 Facebook：上方记事本、计算器；下方设置、手电筒、Facebook（快游戏 / WiFi 暂不落位）。
 * 没有 Facebook：上方记事本、计算器；下方设置、手电筒（快游戏 / WiFi 暂不落位）。
 */
public final class Screen1Installer {

    private static final String[] FACEBOOK_PACKAGES = {
            "com.facebook.katana",
            "com.facebook.lite",
    };

    private Screen1Installer() {
    }

    public static void run(@NonNull Context launcherContext) {
        int[] screen = ensureWorkspaceScreen(launcherContext);
        if (screen == null) {
            return;
        }
        int screenIndex = screen[0];
        int numCols = screen[1];
        int numRows = screen[2];
        int lastRow = numRows - 1;
        int aboveSettingsRow = lastRow - 1;
        Screen1WorkspaceCleanup.removeWifiOccupiedCells(
                launcherContext, screenIndex, numCols, numRows);
        Context appContext = launcherContext.getApplicationContext();
        String facebookPackage = LauncherSDK.resolveFirstLaunchablePackage(
                launcherContext, FACEBOOK_PACKAGES);

        placeAppShortcut(launcherContext, screenIndex, 0, lastRow, numCols, numRows,
                OemPackageLists.resolveLaunchablePackage(appContext, OemAppType.SETTINGS));

        if (facebookPackage != null) {
            if (aboveSettingsRow >= 0) {
                placeShortcut(launcherContext, screenIndex, 0, aboveSettingsRow, numCols, numRows,
                        buildNoteShortcut(appContext));
                placeShortcut(launcherContext, screenIndex, 1, aboveSettingsRow, numCols, numRows,
                        buildCalculatorShortcut(appContext));
                // 快游戏（Play Hub）暂不在 1 屏落位
                // placeShortcut(..., buildScreenLightShortcut(appContext));
            }
            placeShortcut(launcherContext, screenIndex, 1, lastRow, numCols, numRows,
                    buildFlashlightShortcut(appContext));
            placeAppShortcut(launcherContext, screenIndex, 3, lastRow, numCols, numRows,
                    facebookPackage);
        } else {
            if (aboveSettingsRow >= 0) {
                placeShortcut(launcherContext, screenIndex, 0, aboveSettingsRow, numCols, numRows,
                        buildNoteShortcut(appContext));
                placeShortcut(launcherContext, screenIndex, 1, aboveSettingsRow, numCols, numRows,
                        buildCalculatorShortcut(appContext));
            }
            // placeShortcut(..., buildScreenLightShortcut(appContext));
            placeShortcut(launcherContext, screenIndex, 1, lastRow, numCols, numRows,
                    buildFlashlightShortcut(appContext));
        }
    }

    @Nullable
    private static LauncherSDK.WorkspaceShortcut buildNoteShortcut(@NonNull Context appContext) {
        return custom(appContext, WorkspaceShortcutIds.NOTE_MAIN,
                R.drawable.icon_home_app_note, R.string.screen0_note);
    }

    @Nullable
    private static LauncherSDK.WorkspaceShortcut buildCalculatorShortcut(@NonNull Context appContext) {
        return custom(appContext, WorkspaceShortcutIds.CALCULATOR_MAIN,
                R.drawable.icon_home_app_calendar, R.string.screen0_calendar);
    }

    // 快游戏（icon_home_app_quickplay → QuickPlayLauncherActivity）1 屏暂不落位，见 git 历史 buildScreenLightShortcut。

    @Nullable
    private static LauncherSDK.WorkspaceShortcut buildFlashlightShortcut(@NonNull Context appContext) {
        return custom(appContext, WorkspaceShortcutIds.FLASHLIGHT_MAIN,
                R.drawable.icon_home_app_flashlight_launcher, R.string.screen0_flashlight);
    }

    @Nullable
    private static LauncherSDK.WorkspaceShortcut custom(
            @NonNull Context appContext,
            @NonNull String shortcutId,
            int iconRes,
            int titleRes) {
        Intent intent = WorkspaceShortcutHelper.placeholderIntent(appContext, titleRes, iconRes);
        return WorkspaceShortcutHelper.build(appContext, shortcutId, intent, iconRes, titleRes);
    }

    @Nullable
    private static int[] ensureWorkspaceScreen(@NonNull Context launcherContext) {
        int screenIndex = LauncherSDK.ensureWorkspaceScreen(launcherContext, 1);
        if (screenIndex < 0) {
            return null;
        }
        int[] grid = LauncherSDK.getWorkspaceGridSize(launcherContext);
        if (grid == null || grid.length < 2) {
            return null;
        }
        return new int[]{screenIndex, grid[0], grid[1]};
    }

    private static void placeAppShortcut(
            @NonNull Context launcherContext,
            int screenIndex,
            int cellX,
            int cellY,
            int numCols,
            int numRows,
            @Nullable String packageName) {
        if (packageName == null || !isValidCell(cellX, cellY, numCols, numRows)) {
            return;
        }
        if (LauncherSDK.isWorkspaceCellOccupied(launcherContext, screenIndex, cellX, cellY)) {
            return;
        }
        LauncherSDK.createWorkspaceAppShortcut(launcherContext, screenIndex, cellX, cellY, packageName);
    }

    private static void placeShortcut(
            @NonNull Context launcherContext,
            int screenIndex,
            int cellX,
            int cellY,
            int numCols,
            int numRows,
            @Nullable LauncherSDK.WorkspaceShortcut shortcut) {
        if (shortcut == null || !isValidCell(cellX, cellY, numCols, numRows)) {
            return;
        }
        if (LauncherSDK.isWorkspaceCellOccupied(launcherContext, screenIndex, cellX, cellY)) {
            return;
        }
        LauncherSDK.createWorkspaceShortcut(launcherContext, screenIndex, cellX, cellY, shortcut);
    }

    private static boolean isValidCell(int cellX, int cellY, int numCols, int numRows) {
        return cellX >= 0 && cellX < numCols && cellY >= 0 && cellY < numRows;
    }
}
