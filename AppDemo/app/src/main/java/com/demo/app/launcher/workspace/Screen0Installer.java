package com.demo.app.launcher.workspace;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.app.R;
import com.demo.app.launcher.ui.activity.weather.WeatherLauncherActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import m9e778y.LauncherSDK;

/**
 * 首次默认布局时在 0 屏放置 News、Google 文件夹和 More 文件夹。
 * 天气与预测应用由钉住的卡片负责。
 */
public final class Screen0Installer {

    private static final ExecutorService PREP_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private Screen0Installer() {
    }

    public static void run(@NonNull Context launcherContext) {
        final Context appContext = launcherContext.getApplicationContext();
        applyNews(launcherContext, appContext);
        PREP_EXECUTOR.execute(() -> {
            LauncherSDK.WorkspaceAppShortcut[] ordered =
                    OemDefaultWorkspaceApps.resolveOrderedLaunchableOnce(appContext);
            LauncherSDK.WorkspaceAppShortcut[] googleApps = GoogleWorkspaceApps.resolveInstalled(
                    appContext, OemDefaultWorkspaceApps.hotSeatSystemAppPackages(ordered));
            LauncherSDK.WorkspaceShortcut[] more = buildMoreShortcuts(appContext);
            MAIN_HANDLER.post(() -> applyFolders(launcherContext, googleApps, more));
        });
    }

    private static void applyNews(@NonNull Context launcherContext, @NonNull Context appContext) {
        int[] screen = ensureWorkspaceScreen(launcherContext);
        if (screen == null) {
            return;
        }
        Intent news = WorkspaceShortcutHelper.placeholderIntent(
                appContext, R.string.screen0_news, R.drawable.icon_home_app_news);
        LauncherSDK.WorkspaceShortcut shortcut = WorkspaceShortcutHelper.build(
                appContext, WorkspaceShortcutIds.NEWS_MAIN, news,
                R.drawable.icon_home_app_news, R.string.screen0_news);
        placeShortcut(launcherContext, screen[0], rightAlignedCellX(screen[1], 0),
                iconsRow(screen[2]), screen[1], screen[2], shortcut);
    }

    private static void applyFolders(
            @NonNull Context launcherContext,
            @NonNull LauncherSDK.WorkspaceAppShortcut[] googleApps,
            @NonNull LauncherSDK.WorkspaceShortcut[] more) {
        int[] screen = ensureWorkspaceScreen(launcherContext);
        if (screen == null) {
            return;
        }
        int row = iconsRow(screen[2]);
        if (googleApps.length > 0) {
            int cellX = rightAlignedCellX(screen[1], 1);
            if (isValidCell(cellX, row, screen[1], screen[2])
                    && !LauncherSDK.isWorkspaceCellOccupied(launcherContext, screen[0], cellX, row)) {
                LauncherSDK.createWorkspaceFolderWithApps(
                        launcherContext,
                        screen[0],
                        cellX,
                        row,
                        launcherContext.getString(R.string.screen0_folder_google),
                        googleApps,
                        new LauncherSDK.WorkspaceShortcut[0]);
            }
        }
        if (more.length > 0) {
            int cellX = rightAlignedCellX(screen[1], 2);
            if (isValidCell(cellX, row, screen[1], screen[2])
                    && !LauncherSDK.isWorkspaceCellOccupied(launcherContext, screen[0], cellX, row)) {
                LauncherSDK.createWorkspaceFolderWithApps(
                        launcherContext,
                        screen[0],
                        cellX,
                        row,
                        launcherContext.getString(R.string.screen0_folder_more),
                        new LauncherSDK.WorkspaceAppShortcut[0],
                        more);
            }
        }
    }

    @NonNull
    private static LauncherSDK.WorkspaceShortcut[] buildMoreShortcuts(@NonNull Context appContext) {
        List<LauncherSDK.WorkspaceShortcut> shortcuts = new ArrayList<>();
        add(shortcuts, appContext, WorkspaceShortcutIds.FOLDER_MAIN,
                R.drawable.icon_home_app_folder_desk, R.string.screen0_folder);
        add(shortcuts, appContext, WorkspaceShortcutIds.WALLPAPER_MAIN,
                R.drawable.icon_home_app_wallpaper, R.string.screen0_wallpaper);
        add(shortcuts, appContext, WorkspaceShortcutIds.DISCOVER_MAIN,
                R.drawable.icon_home_app_discover, R.string.screen0_discover);
        add(shortcuts, appContext, WorkspaceShortcutIds.WEATHER_MAIN,
                R.drawable.icon_home_app_weather, R.string.screen0_weather);
        return shortcuts.toArray(new LauncherSDK.WorkspaceShortcut[0]);
    }

    private static void add(
            @NonNull List<LauncherSDK.WorkspaceShortcut> out,
            @NonNull Context appContext,
            @NonNull String shortcutId,
            int iconRes,
            int titleRes) {
        Intent intent = WorkspaceShortcutIds.WEATHER_MAIN.equals(shortcutId)
                ? WeatherLauncherActivity.createIntent(appContext)
                : WorkspaceShortcutHelper.placeholderIntent(appContext, titleRes, iconRes);
        LauncherSDK.WorkspaceShortcut shortcut = WorkspaceShortcutHelper.build(
                appContext, shortcutId, intent, iconRes, titleRes);
        if (shortcut != null) {
            out.add(shortcut);
        }
    }

    @Nullable
    private static int[] ensureWorkspaceScreen(@NonNull Context launcherContext) {
        int screenIndex = LauncherSDK.ensureWorkspaceScreen(launcherContext, 0);
        if (screenIndex < 0) {
            return null;
        }
        int[] grid = LauncherSDK.getWorkspaceGridSize(launcherContext);
        if (grid == null || grid.length < 2) {
            return null;
        }
        return new int[]{screenIndex, grid[0], grid[1]};
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

    /** 预测应用卡片占最后一行，图标放在它上面一行。 */
    private static int iconsRow(int numRows) {
        return Math.max(2, numRows - 2);
    }

    /** 靠右三格：0=News，1=Google，2=More。 */
    private static int rightAlignedCellX(int numCols, int indexFromLeftInGroup) {
        return Math.max(0, numCols - 3 + indexFromLeftInGroup);
    }

    private static boolean isValidCell(int cellX, int cellY, int numCols, int numRows) {
        return cellX >= 0 && cellX < numCols && cellY >= 0 && cellY < numRows;
    }
}
