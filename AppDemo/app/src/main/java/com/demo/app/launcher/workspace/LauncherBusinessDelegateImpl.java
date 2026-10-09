package com.demo.app.launcher.workspace;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import com.demo.app.launcher.widget.Screen0PredictedAppsWidget;
import com.demo.app.launcher.widget.Screen0WeatherWidget;

import m9e778y.LauncherSDK;

/** 桌面业务入口：注册 0 屏和钉住的卡片。 */
public class LauncherBusinessDelegateImpl implements LauncherSDK.BusinessDelegate {

    @Override
    public void initialize(@NonNull Application application) {
        WorkspaceShortcutClickHandler.register();
        LauncherSDK.registerPinnedDesktopWidgetDelegate(new Screen0WeatherWidget());
        LauncherSDK.registerPinnedDesktopWidgetDelegate(Screen0PredictedAppsWidget.getInstance());
        LauncherSDK.registerWorkspaceScreenDelegate(WorkspaceScreen0DelegateImpl.getInstance());
        LauncherSDK.registerWorkspaceScreenDelegate(WorkspaceScreen1DelegateImpl.getInstance());
        LauncherSDK.registerAllAppsDelegate(AllAppsDelegateImpl.getInstance());
        LauncherSDK.registerLeftScreenCustomContentDelegate(new LeftScreenCustomContentDelegateImpl());
        LauncherSDK.registerTopPanelDelegate(TopPanelDelegateImpl.getInstance());
    }

    @Override
    public void addDefaultWorkspaceShortcuts(@NonNull Context launcherContext) {
        HotSeatInstaller.run(launcherContext);
    }

    @Override
    public void openHotSeatQsbSearch(@NonNull Context launchContext) {
        LauncherSDK.openTopPanel(true);
    }

    @Override
    public void openAllAppsSearch(@NonNull Context launchContext) {
        LauncherSDK.openTopPanel(true);
    }
}
