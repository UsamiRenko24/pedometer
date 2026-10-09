package com.demo.app.launcher.widget;

import android.app.Activity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.app.R;
import com.demo.app.launcher.ui.view.weather.Screen0WeatherCardView;

import m9e778y.LauncherSDK;

/** 0 屏天气卡片。数据先用占位，图标用 drawable 里已有的天气图。 */
public final class Screen0WeatherWidget implements LauncherSDK.PinnedDesktopWidgetDelegate {

    private static final String WIDGET_ID = "workspace.widget.screen0.weather";

    @Nullable
    private View hostView;

    @NonNull
    @Override
    public String getWidgetId() {
        return WIDGET_ID;
    }

    @NonNull
    @Override
    public LauncherSDK.CustomAppWidgetSpec createSpec() {
        return new LauncherSDK.CustomAppWidgetSpec(
                "Weather",
                R.drawable.icon_home_app_weather,
                R.drawable.icon_home_app_weather,
                R.layout.widget_screen0_weather,
                4,
                2,
                4,
                2,
                0,
                false);
    }

    @Override
    public void ensurePlaced(@NonNull Activity activity) {
        LauncherSDK.ensurePinnedDesktopWidgetOnScreen0(activity, getWidgetId(), 0, 0, 1);
    }

    @Override
    public void onBound(@NonNull Activity activity, @NonNull View view) {
        hostView = view;
        showCard();
    }

    @Override
    public void onUnbound(@NonNull Activity activity) {
        hostView = null;
    }

    @Override
    public void onResume(@NonNull Activity launcherActivity) {
        showCard();
    }

    @Override
    public void onPause(@NonNull Activity launcherActivity) {
        Screen0WeatherCardView card = findCard();
        if (card != null) {
            card.onPageInvisible();
        }
    }

    @Override
    public void onDestroy(@NonNull Activity launcherActivity) {
        hostView = null;
    }

    private void showCard() {
        Screen0WeatherCardView card = findCard();
        if (card != null) {
            card.onPageVisible();
        }
    }

    @Nullable
    private Screen0WeatherCardView findCard() {
        if (hostView == null) {
            return null;
        }
        View view = hostView.findViewById(R.id.screen0_weather_card_view);
        if (view instanceof Screen0WeatherCardView) {
            return (Screen0WeatherCardView) view;
        }
        return null;
    }
}
