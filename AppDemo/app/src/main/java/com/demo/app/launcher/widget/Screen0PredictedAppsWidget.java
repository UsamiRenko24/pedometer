package com.demo.app.launcher.widget;

import android.app.Activity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.app.R;
import com.demo.app.launcher.workspace.Screen0DesktopMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import m9e778y.LauncherSDK;

/** 0 屏预测应用：第一格是本应用，其余由桌面 SDK 推送。 */
public final class Screen0PredictedAppsWidget implements LauncherSDK.PinnedDesktopWidgetDelegate,
        LauncherSDK.PredictedAppsCallback {

    private static final String WIDGET_ID = "workspace.widget.screen0.predicted.apps";
    private static final int PREDICTED_APPS_LIMIT = Screen0PredictedAppsCardView.PREDICTED_SLOT_COUNT;

    private static final Screen0PredictedAppsWidget INSTANCE = new Screen0PredictedAppsWidget();

    @Nullable
    private View hostView;
    @Nullable
    private List<LauncherSDK.PredictedAppInfo> latestPredictedApps;

    public static Screen0PredictedAppsWidget getInstance() {
        return INSTANCE;
    }

    private Screen0PredictedAppsWidget() {
        LauncherSDK.setPredictedAppsCallback(this, PREDICTED_APPS_LIMIT);
    }

    @NonNull
    @Override
    public String getWidgetId() {
        return WIDGET_ID;
    }

    @NonNull
    @Override
    public LauncherSDK.CustomAppWidgetSpec createSpec() {
        String label = "MRU";
        android.content.Context context = LauncherSDK.getContextOrNull();
        if (context != null) {
            label = context.getString(R.string.screen0_mru);
        }
        return new LauncherSDK.CustomAppWidgetSpec(
                label,
                R.drawable.spalsh_round,
                R.drawable.spalsh_round,
                R.layout.widget_screen0_predicted_apps,
                4,
                1,
                4,
                1,
                0,
                true);
    }

    @Override
    public void ensurePlaced(@NonNull Activity activity) {
        int cellY = 3;
        int[] grid = LauncherSDK.getWorkspaceGridSize(activity);
        if (grid != null && grid.length >= 2 && grid[1] > 0) {
            cellY = Math.max(2, grid[1] - 1);
        }
        LauncherSDK.ensurePinnedDesktopWidgetOnScreen0(activity, getWidgetId(), 0, cellY, 1);
    }

    @Override
    public void onBound(@NonNull Activity activity, @NonNull View view) {
        hostView = view;
        applyPredictedApps(latestOrEmpty());
        view.post(() -> {
            Screen0PredictedAppsCardView card = findCard();
            if (card != null) {
                Screen0DesktopMenu.attachPredictedRow(activity, view, card);
            }
        });
    }

    @Override
    public void onUnbound(@NonNull Activity activity) {
        hostView = null;
    }

    @Override
    public void onResume(@NonNull Activity launcherActivity) {
        List<LauncherSDK.PredictedAppInfo> cached = LauncherSDK.getPredictedApps(PREDICTED_APPS_LIMIT);
        if (cached != null && !cached.isEmpty()) {
            latestPredictedApps = new ArrayList<>(cached);
        }
        applyPredictedApps(latestOrEmpty());
    }

    @Override
    public void onPause(@NonNull Activity launcherActivity) {
    }

    @Override
    public void onDestroy(@NonNull Activity launcherActivity) {
        hostView = null;
    }

    @Override
    public void onPredictedAppsChanged(@NonNull List<LauncherSDK.PredictedAppInfo> apps) {
        latestPredictedApps = new ArrayList<>(apps);
        applyPredictedApps(latestPredictedApps);
    }

    private void applyPredictedApps(@NonNull List<LauncherSDK.PredictedAppInfo> predicted) {
        Screen0PredictedAppsCardView card = findCard();
        if (card != null) {
            card.submitPredicted(predicted);
        }
    }

    @NonNull
    private List<LauncherSDK.PredictedAppInfo> latestOrEmpty() {
        return latestPredictedApps != null ? latestPredictedApps : Collections.emptyList();
    }

    @Nullable
    private Screen0PredictedAppsCardView findCard() {
        if (hostView == null) {
            return null;
        }
        View view = hostView.findViewById(R.id.screen0_predicted_apps_card_view);
        if (view instanceof Screen0PredictedAppsCardView) {
            return (Screen0PredictedAppsCardView) view;
        }
        return null;
    }
}
