package com.demo.app.launcher.workspace;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.demo.app.launcher.faq.LauncherFaqActivity;

import m9e778y.LauncherSDK;

final class WorkspaceShortcutHelper {

    private WorkspaceShortcutHelper() {
    }

    @Nullable
    static LauncherSDK.WorkspaceShortcut build(
            @NonNull Context appContext,
            @NonNull String shortcutId,
            @NonNull Intent launchIntent,
            @DrawableRes int iconResId,
            @StringRes int titleResId) {
        Bitmap icon = LauncherSDK.workspaceIconFromDrawable(appContext, iconResId);
        if (icon == null) {
            return null;
        }
        return new LauncherSDK.WorkspaceShortcut(
                shortcutId,
                launchIntent,
                icon,
                appContext.getString(titleResId));
    }

    @NonNull
    static Intent placeholderIntent(
            @NonNull Context appContext,
            @StringRes int titleResId,
            @DrawableRes int iconResId) {
        return new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(new ComponentName(appContext, LauncherFaqActivity.class))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
}
