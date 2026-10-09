package com.demo.app.launcher.ui.activity.weather;

import android.content.Intent;
import android.graphics.Color;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.demo.app.R;
import com.demo.app.databinding.ActivityWeatherBinding;
import com.demo.app.launcher.ads.LauncherBackInsertPlaceholder;
import com.demo.app.launcher.ads.LauncherBackPressAds;
import com.demo.app.launcher.ads.LauncherBuiltInAds;
import com.demo.app.launcher.ads.LauncherBottomBannerPlaceholderView;
import com.demo.app.launcher.ui.adapter.weather.WeatherDailyAdapter;
import com.demo.app.launcher.ui.adapter.weather.WeatherHourlyAdapter;
import com.demo.app.launcher.weather.WeatherLocation;
import com.demo.app.launcher.weather.WeatherRepository;
import com.demo.app.launcher.weather.WeatherSnapshot;
import com.demo.app.ui.activity.BaseActivity;
import com.demo.app.utils.SystemUiHelper;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Locale;

/** 天气首页。背景和图标跟随接口返回的天气和昼夜。 */
public class WeatherActivity extends BaseActivity<ActivityWeatherBinding> {

    private boolean lightStatusBar = true;
    private int loadToken;

    @Nullable
    @Override
    protected ActivityWeatherBinding getViewBinding() {
        return ActivityWeatherBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initWindow(View rootView) {
        EdgeToEdge.enable(this, SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        applyImmersive();
        ViewCompat.setOnApplyWindowInsetsListener(binding.weatherHomeScroll, (v, insets) -> {
            Insets bars = insets.getInsets(
                    SystemUiHelper.contentInsetTypes());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    protected void applyImmersive() {
        SystemUiHelper.applyVolumeControlImmersive(getWindow(), lightStatusBar, lightStatusBar);
    }

    @Override
    protected void initViews() {
        applyVisuals(WeatherVisuals.of(WeatherVisuals.Kind.UNKNOWN, WeatherVisuals.isNightNow()));
        LauncherBuiltInAds.show(binding.weatherHomeAd);
        LauncherBottomBannerPlaceholderView banner = binding.weatherBottomBannerView;
        if (banner != null) {
            banner.refreshVisibility();
        }
        LauncherBackInsertPlaceholder.preload(this);
        LauncherBackPressAds.install(this, this::finishAndRemoveTask);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int token = ++loadToken;
        WeatherRepository.load(this, (location, snapshot) -> {
            if (token != loadToken || isFinishing()) {
                return;
            }
            if (location == null || snapshot == null) {
                showUnavailable();
                return;
            }
            bindWeather(location, snapshot);
        });
    }

    private void bindWeather(@NonNull WeatherLocation location, @NonNull WeatherSnapshot snapshot) {
        WeatherVisuals visuals = WeatherVisuals.ofSkycon(snapshot.skycon);
        applyVisuals(visuals);
        binding.weatherHomeLocation.setText(location.displayName());
        binding.weatherHomeDate.setText(getString(R.string.weather_date_today,
                new SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(snapshot.timeMillis)));
        binding.weatherHomeTemp.setText(snapshot.temperature + "°");
        binding.weatherHomeCondition.setText(WeatherVisuals.label(WeatherVisuals.kindOf(snapshot.skycon)));
        binding.weatherHomeFeels.setText(getString(R.string.weather_feels_format, snapshot.temperature));
        binding.weatherHomeHumidity.setText(snapshot.humidityPercent + "%");
        binding.weatherHomeWind.setText(windDirection(snapshot.windDirection));
        binding.weatherHomeWindLevel.setText(getString(R.string.weather_wind_level, windLevel(snapshot.windSpeed)));
        if (!snapshot.days.isEmpty()) {
            WeatherSnapshot.Day today = snapshot.days.get(0);
            binding.weatherHomeHigh.setText(today.high + "°");
            binding.weatherHomeLow.setText(today.low + "°");
        }
        WeatherHourlyAdapter.bind(binding.weatherHourRow, snapshot.hours);
        WeatherDailyAdapter.bind(binding.weatherDayList, snapshot.days);
    }

    private void showUnavailable() {
        applyVisuals(WeatherVisuals.of(WeatherVisuals.Kind.UNKNOWN, WeatherVisuals.isNightNow()));
        binding.weatherHomeLocation.setText(R.string.weather_widget_failed);
        binding.weatherHomeDate.setText(R.string.weather_widget_placeholder);
        binding.weatherHomeTemp.setText(R.string.weather_widget_placeholder);
        binding.weatherHomeCondition.setText(R.string.weather_widget_placeholder);
        binding.weatherHomeFeels.setText(R.string.weather_widget_placeholder);
        binding.weatherHomeHumidity.setText(R.string.weather_widget_placeholder);
        binding.weatherHomeWind.setText(R.string.weather_widget_placeholder);
        binding.weatherHomeWindLevel.setText(R.string.weather_widget_placeholder);
        binding.weatherHomeHigh.setText(R.string.weather_widget_placeholder);
        binding.weatherHomeLow.setText(R.string.weather_widget_placeholder);
        WeatherHourlyAdapter.bind(binding.weatherHourRow, Collections.emptyList());
        WeatherDailyAdapter.bind(binding.weatherDayList, Collections.emptyList());
    }

    private void applyVisuals(@NonNull WeatherVisuals visuals) {
        binding.weatherHomeBg.setImageResource(visuals.background);
        binding.weatherHomeConditionIcon.setImageResource(visuals.icon);
        lightStatusBar = !visuals.darkBackground;
        applyImmersive();
    }

    @NonNull
    private String windDirection(double degrees) {
        int[] directions = {
                R.string.weather_dir_n,
                R.string.weather_dir_ne,
                R.string.weather_dir_e,
                R.string.weather_dir_se,
                R.string.weather_dir_s,
                R.string.weather_dir_sw,
                R.string.weather_dir_w,
                R.string.weather_dir_nw
        };
        int index = (int) Math.round((((degrees % 360) + 360) % 360) / 45.0) % 8;
        return getString(R.string.weather_wind_card, getString(directions[index]));
    }

    /** 接口风速单位是 km/h，按蒲福风力等级换算。 */
    private static int windLevel(int speedKmh) {
        int speed = Math.max(0, speedKmh);
        if (speed < 1) {
            return 0;
        }
        int[] upper = {5, 11, 19, 28, 38, 49, 61, 74, 88, 102, 117};
        for (int i = 0; i < upper.length; i++) {
            if (speed <= upper[i]) {
                return i + 1;
            }
        }
        return 12;
    }

    @Override
    protected void initEvents() {
        binding.weatherHomeManage.setOnClickListener(v ->
                WeatherCityManageActivity.start(this));
    }
}
