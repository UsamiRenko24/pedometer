package com.demo.app.launcher.ui.view.weather;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.demo.app.R;
import com.demo.app.launcher.ui.activity.weather.WeatherLauncherActivity;
import com.demo.app.launcher.ui.activity.weather.WeatherVisuals;
import com.demo.app.launcher.weather.WeatherLocation;
import com.demo.app.launcher.weather.WeatherRepository;
import com.demo.app.launcher.weather.WeatherSnapshot;

/** 0 屏天气卡片。没有定位权限时请求授权，没有天气数据时按白天和夜间显示占位。 */
public class Screen0WeatherCardView extends FrameLayout {

    private static final int LOCATION_REQUEST = 4101;

    private final FrameLayout card;
    private final android.view.View permission;
    private final android.view.View empty;
    private final ImageView placeIcon;
    private final TextView placeLabel;
    private final ImageView refresh;
    private final TextView temperature;
    private final TextView high;
    private final TextView low;
    private final TextView time;
    private final ImageView statusIcon;
    private final ImageView up;
    private final ImageView down;
    private int loadToken;

    public Screen0WeatherCardView(@NonNull Context context) {
        this(context, null);
    }

    public Screen0WeatherCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Screen0WeatherCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_screen0_weather_card, this, true);
        card = findViewById(R.id.screen0_weather_card);
        permission = findViewById(R.id.screen0_weather_permission);
        empty = findViewById(R.id.screen0_weather_empty);
        placeIcon = findViewById(R.id.screen0_weather_place_icon);
        placeLabel = findViewById(R.id.screen0_weather_place_label);
        refresh = findViewById(R.id.screen0_weather_refresh);
        temperature = findViewById(R.id.screen0_weather_temperature);
        high = findViewById(R.id.screen0_weather_high);
        low = findViewById(R.id.screen0_weather_low);
        time = findViewById(R.id.screen0_weather_time);
        statusIcon = findViewById(R.id.screen0_weather_status_icon);
        up = findViewById(R.id.screen0_weather_up);
        down = findViewById(R.id.screen0_weather_down);
        card.setOnClickListener(v -> WeatherLauncherActivity.start(context));
        findViewById(R.id.screen0_weather_allow).setOnClickListener(v -> requestLocation());
        refresh.setOnClickListener(v -> bindState());
    }

    public void onPageVisible() {
        bindState();
    }

    public void onPageInvisible() {
    }

    private void bindState() {
        if (!hasLocationPermission()) {
            permission.setVisibility(VISIBLE);
            empty.setVisibility(GONE);
            card.setBackgroundResource(android.R.color.transparent);
            return;
        }
        int token = ++loadToken;
        WeatherRepository.load(getContext(), (location, snapshot) -> {
            if (token != loadToken) {
                return;
            }
            if (location == null || snapshot == null) {
                showEmpty();
            } else {
                showWeather(location, snapshot);
            }
        });
    }

    private void showWeather(@NonNull WeatherLocation location, @NonNull WeatherSnapshot snapshot) {
        permission.setVisibility(GONE);
        empty.setVisibility(VISIBLE);
        WeatherVisuals visuals = WeatherVisuals.widgetSkycon(snapshot.skycon);
        card.setBackgroundResource(visuals.background);
        placeIcon.setVisibility(VISIBLE);
        placeIcon.setImageTintList(android.content.res.ColorStateList.valueOf(0xFFFFFFFF));
        refresh.setVisibility(GONE);
        placeLabel.setText(location.displayName());
        placeLabel.setTextColor(0xFFFFFFFF);
        temperature.setText(String.valueOf(snapshot.temperature));
        if (snapshot.days.isEmpty()) {
            high.setText(R.string.weather_widget_placeholder);
            low.setText(R.string.weather_widget_placeholder);
        } else {
            high.setText(snapshot.days.get(0).high + "°");
            low.setText(snapshot.days.get(0).low + "°");
        }
        time.setText(WeatherVisuals.label(WeatherVisuals.kindOf(snapshot.skycon)));
        statusIcon.setImageResource(visuals.icon);
        up.setVisibility(VISIBLE);
        down.setVisibility(VISIBLE);
    }

    private void showEmpty() {
        permission.setVisibility(GONE);
        empty.setVisibility(VISIBLE);
        boolean night = WeatherVisuals.isNightNow();
        WeatherVisuals visuals = WeatherVisuals.widget(WeatherVisuals.Kind.UNKNOWN, night);
        card.setBackgroundResource(visuals.background);
        placeIcon.setVisibility(night ? GONE : VISIBLE);
        placeIcon.setImageTintList(android.content.res.ColorStateList.valueOf(0xFFFF4D4F));
        refresh.setVisibility(night ? VISIBLE : GONE);
        placeLabel.setText(night ? R.string.weather_widget_failed : R.string.weather_widget_placeholder);
        placeLabel.setTextColor(night ? 0xFFFF5A6A : 0xFFFFFFFF);
        temperature.setText(R.string.weather_widget_placeholder);
        high.setText(R.string.weather_widget_placeholder);
        low.setText(R.string.weather_widget_placeholder);
        time.setText(R.string.weather_widget_placeholder);
        statusIcon.setImageResource(visuals.icon);
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocation() {
        Activity activity = findActivity();
        if (activity == null) {
            return;
        }
        activity.requestPermissions(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, LOCATION_REQUEST);
    }

    @Nullable
    private Activity findActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}
