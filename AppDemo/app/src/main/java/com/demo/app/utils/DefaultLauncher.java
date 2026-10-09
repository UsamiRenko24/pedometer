package com.demo.app.utils;

import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.demo.app.ui.activity.GuideActivity;

import m9e778y.LauncherSDK;

/**
 * 是否已是默认桌面，以及拉起系统的默认桌面设置。
 */
public final class DefaultLauncher {

    private DefaultLauncher() {
    }

    public static boolean isDefault(Context context) {
        RoleManager roleManager = context.getSystemService(RoleManager.class);
        return roleManager != null && roleManager.isRoleHeld(RoleManager.ROLE_HOME);
    }

    /** 已经是默认桌面时返回 null。 */
    @Nullable
    public static Intent requestIntent(Context context) {
        RoleManager roleManager = context.getSystemService(RoleManager.class);
        if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
            if (roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                return null;
            }
            return roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME);
        }
        return new Intent(Settings.ACTION_HOME_SETTINGS);
    }

    /**
     * 引导里申请默认桌面。设完后留在引导页，不进入桌面。
     * 与 RemoteControl100 的 autoNavigateToHome(false) 和 stayOnGuideIntent 一致。
     */
    public static void requestKeepingGuide(@NonNull FragmentActivity activity,
                                           @NonNull LauncherSDK.LauncherResultCallback callback) {
        if (isDefault(activity) || LauncherSDK.isDefaultLauncherCompat(activity)) {
            callback.onResult(true);
            return;
        }
        Intent stay = new Intent(activity, GuideActivity.class);
        stay.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        LauncherSDK.requestDefaultLauncher(activity)
                .forceUseHomeSettings(true)
                .autoNavigateToHome(false)
                .stayOnGuideIntent(stay)
                .onResult(callback)
                .start();
    }
}
