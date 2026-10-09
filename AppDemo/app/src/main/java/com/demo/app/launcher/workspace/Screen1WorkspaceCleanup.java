package com.demo.app.launcher.workspace;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import androidx.annotation.NonNull;

import m9e778y.LauncherSDK;

/**
 * 清理 1 屏历史落位（例如旧版 Wi-Fi 快捷方式占格）。
 * 通过 Launcher AAR 内部 {@code r09o.Sww81} 反射删除，无公开 SDK API。
 */
final class Screen1WorkspaceCleanup {

    private Screen1WorkspaceCleanup() {
    }

    static void removeWifiOccupiedCells(
            @NonNull Context launcherContext,
            int screenIndex,
            int numCols,
            int numRows) {
        try {
            Class<?> holderClass = Class.forName("r09o.Sww81");
            Object holder = holderClass.getConstructor(Context.class).newInstance(launcherContext);
            Object api = holderClass.getMethod("getApi").invoke(holder);
            if (api == null) {
                return;
            }
            Class<?> itemClass = Class.forName("yukh9.F16");
            Object[] itemHolder = (Object[]) java.lang.reflect.Array.newInstance(itemClass, 1);
            java.lang.reflect.Method getItemAt = api.getClass().getMethod(
                    "getItemAt", int.class, int.class, int.class, itemClass.arrayType());
            java.lang.reflect.Method deleteAt = api.getClass().getMethod(
                    "deleteDesktopIcon", int.class, int.class, int.class);

            for (int cellY = 0; cellY < numRows; cellY++) {
                for (int cellX = 0; cellX < numCols; cellX++) {
                    itemHolder[0] = null;
                    Boolean found = (Boolean) getItemAt.invoke(
                            api, screenIndex, cellX, cellY, itemHolder);
                    if (!Boolean.TRUE.equals(found) || itemHolder[0] == null) {
                        continue;
                    }
                    if (isWifiShortcut(itemHolder[0])) {
                        deleteAt.invoke(api, screenIndex, cellX, cellY);
                    }
                }
            }
        } catch (ReflectiveOperationException ignored) {
            // 非 Launcher 上下文或 AAR 版本不匹配时跳过
        }
    }

    private static boolean isWifiShortcut(@NonNull Object itemInfo) throws ReflectiveOperationException {
        java.lang.reflect.Method getIntent =
                itemInfo.getClass().getMethod("getIntent");
        Object intentObj = getIntent.invoke(itemInfo);
        if (!(intentObj instanceof Intent)) {
            return false;
        }
        Intent intent = (Intent) intentObj;
        String shortcutId = intent.getStringExtra(LauncherSDK.EXTRA_WORKSPACE_SHORTCUT_ID);
        if (WorkspaceShortcutIds.WIFI_MAIN.equals(shortcutId)) {
            return true;
        }
        return Settings.ACTION_WIFI_SETTINGS.equals(intent.getAction());
    }
}
