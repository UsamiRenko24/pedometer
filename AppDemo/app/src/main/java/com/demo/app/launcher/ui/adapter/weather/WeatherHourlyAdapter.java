package com.demo.app.launcher.ui.adapter.weather;

import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.demo.app.R;
import com.demo.app.databinding.ItemWeatherHourlyBinding;
import com.demo.app.launcher.ui.activity.weather.WeatherVisuals;
import com.demo.app.launcher.weather.WeatherSnapshot;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/** 首页 24 小时预报。 */
public final class WeatherHourlyAdapter {

    private WeatherHourlyAdapter() {
    }

    public static void bind(@NonNull LinearLayout parent, @NonNull List<WeatherSnapshot.Hour> hours) {
        parent.removeAllViews();
        if (hours.isEmpty()) {
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        String now = parent.getContext().getString(R.string.weather_now);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
        long nowMillis = System.currentTimeMillis();
        for (int index = 0; index < hours.size(); index++) {
            WeatherSnapshot.Hour hour = hours.get(index);
            ItemWeatherHourlyBinding row = ItemWeatherHourlyBinding.inflate(inflater, parent, true);
            boolean current = index == 0 && Math.abs(hour.timeMillis - nowMillis) < 30 * 60 * 1000L;
            row.weatherHourTime.setText(current ? now : format.format(hour.timeMillis));
            row.weatherHourIcon.setImageResource(WeatherVisuals.ofSkycon(hour.skycon).icon);
            row.weatherHourTemp.setText(hour.temperature + "°");
        }
    }
}
