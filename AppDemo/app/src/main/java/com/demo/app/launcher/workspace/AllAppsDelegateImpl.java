package com.demo.app.launcher.workspace;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.demo.app.R;
import com.demo.app.launcher.ads.LauncherAds;
import com.demo.app.launcher.faq.LauncherFaqActivity;
import com.demo.app.launcher.ui.activity.calendar.CalendarLauncherActivity;
import com.demo.app.launcher.ui.activity.flashlight.FlashlightLauncherActivity;
import com.demo.app.launcher.ui.activity.note.NoteLauncherActivity;
import com.demo.app.launcher.ui.activity.weather.WeatherLauncherActivity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import m9e778y.LauncherSDK;

/** 全部应用列表前面的自定义入口。图标先用 drawable 里已有的首页图标。 */
public final class AllAppsDelegateImpl implements LauncherSDK.AllAppsDelegate {

    private static final AllAppsDelegateImpl INSTANCE = new AllAppsDelegateImpl();

    public static AllAppsDelegateImpl getInstance() {
        return INSTANCE;
    }

    @Nullable
    private View allAppsHeaderView;

    private AllAppsDelegateImpl() {
    }

    @Override
    public void onViewCreated(@NonNull FragmentActivity activity, @NonNull View view) {
        AllAppsOverlay.apply(activity, view);
    }

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public View createHeaderView(@NonNull FragmentActivity activity) {
        allAppsHeaderView = LayoutInflater.from(activity)
                .inflate(R.layout.custom_content_all_apps_header, null);
        updateAppDrawerHeaderVisibility();
        return allAppsHeaderView;
    }

    @Override
    public void onPreparePullUp(@NonNull FragmentActivity activity) {
        AllAppsOverlay.apply(activity, null);
        updateAppDrawerHeaderVisibility();
    }

    @Override
    public void onOpened(@NonNull FragmentActivity activity) {
        AllAppsOverlay.apply(activity, null);
    }

    @Override
    public void onClosed(@NonNull FragmentActivity activity) {
        AllAppsOverlay.restoreWhenClosed(activity);
    }

    @Override
    public void onResume(@NonNull FragmentActivity activity) {
    }

    @Override
    public void onPause(@NonNull FragmentActivity activity) {
    }

    @Override
    public void onDestroy(@NonNull FragmentActivity activity) {
        allAppsHeaderView = null;
    }

    private void updateAppDrawerHeaderVisibility() {
        View header = allAppsHeaderView;
        if (header == null) {
            return;
        }
        int visibility = LauncherAds.shouldShowPlaceholder() ? View.VISIBLE : View.GONE;
        header.setVisibility(visibility);
        View adsRow = header.findViewById(R.id.all_apps_llAds);
        View dividerBottom = header.findViewById(R.id.all_apps_banner_divider_bottom);
        if (adsRow != null) {
            adsRow.setVisibility(visibility);
        }
        if (dividerBottom != null) {
            dividerBottom.setVisibility(visibility);
        }
    }

    @NonNull
    @Override
    public List<LauncherSDK.AllAppsCustomLaunchItem> getAllAppsCustomLaunchItems() {
        android.content.Context app = activityContext();
        if (app == null) {
            return Collections.emptyList();
        }
        String faq = LauncherFaqActivity.class.getName();
        return Collections.unmodifiableList(Arrays.asList(
                new LauncherSDK.AllAppsCustomLaunchItem(
                        WorkspaceShortcutIds.ALLAPPS_FLASHLIGHT,
                        app.getString(R.string.screen0_flashlight),
                        R.drawable.icon_home_app_flashlight_launcher,
                        FlashlightLauncherActivity.class.getName()),
                new LauncherSDK.AllAppsCustomLaunchItem(
                        WorkspaceShortcutIds.ALLAPPS_WEATHER,
                        app.getString(R.string.screen0_weather),
                        R.drawable.icon_home_app_weather,
                        WeatherLauncherActivity.class.getName()),
                new LauncherSDK.AllAppsCustomLaunchItem(
                        WorkspaceShortcutIds.ALLAPPS_CALCULATOR,
                        app.getString(R.string.screen0_calendar),
                        R.drawable.icon_home_app_calendar,
                        CalendarLauncherActivity.class.getName()),
                new LauncherSDK.AllAppsCustomLaunchItem(
                        WorkspaceShortcutIds.ALLAPPS_DISCOVER,
                        app.getString(R.string.screen0_discover),
                        R.drawable.icon_home_app_discover,
                        faq),
                new LauncherSDK.AllAppsCustomLaunchItem(
                        WorkspaceShortcutIds.ALLAPPS_NOTE,
                        app.getString(R.string.screen0_note),
                        R.drawable.icon_home_app_note,
                        NoteLauncherActivity.class.getName())
        ));
    }

    private static android.content.Context activityContext() {
        return LauncherSDK.getContextOrNull();
    }
}
