package com.demo.app.launcher.workspace;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.NonNull;

import com.demo.app.MainActivity;
import com.demo.app.launcher.ui.activity.calendar.CalendarLauncherActivity;
import com.demo.app.launcher.ui.activity.discover.DiscoverLauncherActivity;
import com.demo.app.launcher.ui.activity.flashlight.FlashlightLauncherActivity;
import com.demo.app.launcher.ui.activity.folder.FolderLauncherActivity;
import com.demo.app.launcher.ui.activity.note.NoteLauncherActivity;
import com.demo.app.launcher.ui.activity.quickplay.QuickPlayLauncherActivity;
import com.demo.app.launcher.ui.activity.news.NewsLauncherActivity;
import com.demo.app.launcher.ui.activity.wallpaper.WallpaperLauncherActivity;
import com.demo.app.launcher.ui.activity.weather.WeatherLauncherActivity;

import m9e778y.LauncherSDK;

/** 桌面快捷方式点击。返回 false 表示这里已经处理，不再走默认 Intent。 */
public final class WorkspaceShortcutClickHandler {

    private WorkspaceShortcutClickHandler() {
    }

    public static void register() {
        LauncherSDK.setGlobalWorkspaceShortcutClickHandler(
                WorkspaceShortcutClickHandler::onWorkspaceShortcutClick);
    }

    static boolean onWorkspaceShortcutClick(
            @NonNull Context context,
            @NonNull View view,
            @NonNull Intent launchIntent,
            @NonNull String shortcutId) {
        if (WorkspaceShortcutIds.WIFI_MAIN.equals(shortcutId)
                || Settings.ACTION_WIFI_SETTINGS.equals(launchIntent.getAction())) {
            context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            return false;
        }
        switch (shortcutId) {
            case WorkspaceShortcutIds.VOLUME_MAIN:
                context.startActivity(new Intent(context, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return false;
            case WorkspaceShortcutIds.WEATHER_MAIN:
            case WorkspaceShortcutIds.ALLAPPS_WEATHER:
                WeatherLauncherActivity.start(context);
                return false;
            case WorkspaceShortcutIds.NEWS_MAIN:
                NewsLauncherActivity.start(context);
                return false;
            case WorkspaceShortcutIds.WALLPAPER_MAIN:
                WallpaperLauncherActivity.start(context);
                return false;
            case WorkspaceShortcutIds.DISCOVER_MAIN:
            case WorkspaceShortcutIds.ALLAPPS_DISCOVER:
                DiscoverLauncherActivity.start(context);
                return false;
            case WorkspaceShortcutIds.FOLDER_MAIN:
                FolderLauncherActivity.start(context);
                return false;
            case WorkspaceShortcutIds.NOTE_MAIN:
            case WorkspaceShortcutIds.ALLAPPS_NOTE:
                NoteLauncherActivity.start(context);
                return false;
            case WorkspaceShortcutIds.FLASHLIGHT_MAIN:
            case WorkspaceShortcutIds.ALLAPPS_FLASHLIGHT:
                FlashlightLauncherActivity.start(context);
                return false;
            case WorkspaceShortcutIds.CALCULATOR_MAIN:
            case WorkspaceShortcutIds.ALLAPPS_CALCULATOR:
                CalendarLauncherActivity.start(context);
                return false;
            case WorkspaceShortcutIds.SCREEN_LIGHT_MAIN:
                QuickPlayLauncherActivity.start(context);
                return false;
            default:
                return true;
        }
    }
}
